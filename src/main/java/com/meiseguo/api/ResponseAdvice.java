package com.meiseguo.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.meiseguo.api.pojo.Reply;
import org.bson.types.ObjectId;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class ResponseAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        return true;
    }

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Reply errorHandler(Exception ex) {
        if(ex instanceof MethodArgumentNotValidException){
            String error = ((MethodArgumentNotValidException) ex).getBindingResult().getAllErrors().get(0).getDefaultMessage();
            return Reply.fail("Error:" + error);
        }
        return Reply.fail("Exception:"+ex.getMessage());
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        if(!(body instanceof Reply)) {
            return body;
        }
        SerializeConfig config = new SerializeConfig();
        config.put(ObjectId.class, new ObjectIdJsonSerializer());
        return JSONObject.parseObject(JSON.toJSONString(body, config));
    }
}
