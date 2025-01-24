package com.meiseguo.api;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import org.bson.types.ObjectId;

import java.lang.reflect.Type;

public class ObjectIdJsonSerializer implements ObjectSerializer {

    @Override
    public void write(JSONSerializer serializer, Object object, Object o1, Type type, int i) {
        SerializeWriter out = serializer.getWriter();
        if (object == null) {
            serializer.getWriter().writeNull();
            return;
        }
        out.write("\"" + ((ObjectId) object).toString() + "\"");
    }
}