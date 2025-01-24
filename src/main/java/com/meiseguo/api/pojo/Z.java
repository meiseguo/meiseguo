package com.meiseguo.api.pojo;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 数据字典
 */
public enum Z {

    LIST("列表"), SET("集合"),
    access("授权码"),
    openid("唯一id"),
    is_bot("机器人"),
    username("用户名"),
    version("版本"),
    appid("appid"),
    appsecret("appsecret"),
    description("详情描述"),
    host("网站地址"),
    name("名称：姓名，文件名，"),
    time("时间：2020-09-22 11:55:01"),
    sn("流水号：ObjectId"),
    img("图片地址"),
    size("大小：最小单位"),
    deleted("软删除 0:正常 1:已删除"),
    expireAt("过期时间点：ms"),
    createtime("创建时间"),
    updatetime("修改时间"),
    source("来源"),
    localid("本地id：例如IMEI，MAC"),
    orderid("订单ID：UOI12345678"),
    type("类型"),
    status("状态")
    ;

    String desc;
    Class clazz;

    Z(String is) {
        this.desc = is;
    }

    Z(String is, Class c) {
        this.desc = is;
        this.clazz = c;
    }

    public static Map<String, Object> create() {
        return new HashMap<>();
    }

    public static void destroy(Map<String, Object> data){
        data.clear();
    }

    public static <T> T get(Map data, Class<T> dst) {
        try {
            T obj =  dst.newInstance();
            get(data, obj);
            return obj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String get(String field, Object dst) {
        Class clazz = dst.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for(Field f : fields) {
            f.setAccessible(true);
            String name;
            Dict dict = f.getAnnotation(Dict.class);
            if (dict == null) {
                name = f.getName();
            } else {
                Z u = dict.value();
                name = u.name();
            }
            if (field.equalsIgnoreCase(name)) {
                try {
                    Object value = f.get(dst);
                    return String.valueOf(value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static void set(String head, Object dst, Object value) {
        Class clazz = dst.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for(Field f : fields) {
            f.setAccessible(true);
            String name;
            Dict dict = f.getAnnotation(Dict.class);
            if (dict == null) {
                name = f.getName();
            } else {
                Z u = dict.value();
                name = u.name();
            }
            if (head.equalsIgnoreCase(name)) {
                try {
                    String ftype = f.getType().getCanonicalName();
                    String val = String.valueOf(value);
                    System.out.println(ftype + " == " + val);
                    if (ftype.equals("int"))
                    {
                        f.setInt(dst, Integer.parseInt(val));
                    } else if (ftype.equals("long"))
                    {
                        f.setLong(dst, Long.parseLong(val));
                    }else if (ftype.equals("double"))
                    {
                        f.setDouble(dst, Double.parseDouble(val));
                    } else if (ftype.equals("float"))
                    {
                        f.setFloat(dst, Float.parseFloat(val));
                    } else if (ftype.equals("boolean"))
                    {
                        f.setBoolean(dst, Boolean.parseBoolean(val));
                    } else if (ftype.contains("BigDecimal"))
                    {
                        f.set(dst, new BigDecimal(val));
                    } else {
                        f.set(dst, value);
                    }
                } catch (Exception e) {
                    System.out.println("取值异常，尝试转化类型: " + f.getType().getCanonicalName() + " " + value.getClass());
                }
            }

        }
    }

    public static void get(Map data, Object dst) {
        Class clazz = dst.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for(Field f : fields) {
            f.setAccessible(true);
            String name;
            Dict dict = f.getAnnotation(Dict.class);
            if(dict == null) {
                name = f.getName();
            } else {
                Z u = dict.value();
                name = u.name();
            }
            Object value = data.get(name);
            if(value == null) {
                name = name.toLowerCase(Locale.ENGLISH);
                if(data.containsKey(name)) {
                    value = data.get(name);
                } else {
                    continue;
                }
            }

            try {
                String ftype = f.getType().getCanonicalName();
                String val = String.valueOf(value);
                System.out.println(ftype + " == " + val);
                if (ftype.equals("int"))
                {
                    f.setInt(dst, Integer.parseInt(val));
                } else if (ftype.equals("long"))
                {
                    f.setLong(dst, Long.parseLong(val));
                }else if (ftype.equals("double"))
                {
                    f.setDouble(dst, Double.parseDouble(val));
                } else if (ftype.equals("float"))
                {
                    f.setFloat(dst, Float.parseFloat(val));
                } else if (ftype.equals("boolean"))
                {
                    f.setBoolean(dst, Boolean.parseBoolean(val));
                } else if (ftype.contains("BigDecimal"))
                {
                    f.set(dst, new BigDecimal(val));
                } else {
                    f.set(dst, value);
                }
            } catch (Exception e) {
                System.out.println("取值异常，尝试转化类型: " + f.getType().getCanonicalName() + " " + value.getClass());
            }
        }
    }
}
