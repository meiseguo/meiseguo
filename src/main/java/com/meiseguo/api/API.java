package com.meiseguo.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface API {
    String value() default "";
    boolean visible() default false;
    boolean search() default false;
    boolean readonly() default false;
    boolean remote() default false;
    String type() default "str";//str url date time case bool
    String[] choice() default {};// A:0,B:1
    String source() default "";
}
