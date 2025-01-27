package com.meiseguo.api.utils;

import com.meiseguo.api.API;
import com.meiseguo.api.pojo.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PagesUtil {
    public static Class<?>[] classes = {
            Access.class,
            Token.class,
            Bind.class,
            Config.class,
            Safety.class, Setting.class, Asset.class, Account.class, Alarm.class, Alert.class, Action.class, Closed.class, Operator.class, Order.class, Statistic.class
    };

    public static Map<String, Class<?>> headMap = new HashMap<String, Class<?>>() {
        {
            for(Class<?> clazz : classes) {
                Document doc = clazz.getDeclaredAnnotation(Document.class);
                put(doc.value(), clazz);
            }
        }
    };
    public static List<KeyValue> heads = new ArrayList<KeyValue>() {
        {
            for (Class<?> clazz : classes) {
                Document doc = clazz.getDeclaredAnnotation(Document.class);
                API api = clazz.getDeclaredAnnotation(API.class);
                add(new KeyValue(doc.value(), api.value()));
            }
        }
    };

    public static Head build(Class<?> clazz) {
        Head head = new Head();
        System.out.println(clazz);
        Document doc = clazz.getDeclaredAnnotation(Document.class);
        String id = doc.value();
        Field[] fields = clazz.getDeclaredFields();
        head.setName(id);
        List<ApiHead> values = new ArrayList<>();
        for(Field field : fields) {
            ApiHead apiHead = new ApiHead();
            field.setAccessible(true);
            API api = field.getDeclaredAnnotation(API.class);
            String name = field.getName();

            apiHead.setName(name);
            if (api == null) {
                apiHead.setValue(name);
                apiHead.setVisible(false);
                apiHead.setType("str");
            } else {
                String value = api.value().isEmpty() ? name : api.value();
                apiHead.setValue(value);
                apiHead.setVisible(api.visible());
                apiHead.setType(api.type());
                apiHead.setSource(api.source());
                apiHead.setReadonly(api.readonly());
                apiHead.setChoise(choice(api.choice()));
            }
            if("bool".equalsIgnoreCase(apiHead.getType())) {
                apiHead.setName(apiHead.getValue());
            }
            values.add(apiHead);
        }
        head.setValues(values);
        return head;
    }

    public static API getByTitle(String title, Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for(Field field : fields) {
            field.setAccessible(true);
            String name = field.getName();
            if(name.equalsIgnoreCase(title)) {
                return field.getDeclaredAnnotation(API.class);
            }
        }
        return null;
    }
    public static List<KeyValue> choice(String[] options) {
        if(options == null || options.length < 1) {
            return null;
        }
        List<KeyValue> list = new ArrayList<>();
        for(String i:options) {
            KeyValue keyValue = new KeyValue(i);
            list.add(keyValue);
        }
        return list;
    }

    public static Class<?> getClass(String head) {
        return headMap.get(head);
    }

    /**
     * 构建搜索条件
     * @param clazz 被搜索的对象
     * @param search 搜索的关键词
     * @return 查询条件
     */
    public static Criteria search(Class<?> clazz, String search) {
        Criteria criteria = new Criteria();
        Field[] fields = clazz.getDeclaredFields();
        List<Criteria> or = new ArrayList<>();
        for(Field field : fields) {
            field.setAccessible(true);
            String name = field.getName();
            API api = field.getDeclaredAnnotation(API.class);
            if(api != null && api.search()) {
                or.add(Criteria.where(name).regex(search));
            }
        }
        criteria.orOperator(or.toArray(new Criteria[0]));
        return criteria;
    }

//    public static void main(String[] args) {
//        Class<Order> clazz = Order.class;
//        Field[] fields = clazz.getDeclaredFields();
//        for (Field field : fields) {
//            field.setAccessible(true);
//            API api = field.getDeclaredAnnotation(API.class);
//            String name = field.getName();
//            System.out.println("|\t"+name + "\t|\t" + api.type() + "\t|\t否\t|" + (api.value().isEmpty() ? name : api.value())+ "\t|");
//        }
//    }
}
