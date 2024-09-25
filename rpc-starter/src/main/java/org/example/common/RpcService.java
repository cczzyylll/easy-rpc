package org.example.common;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface RpcService {
    int limit() default 100;
    int weight() default 100;
    String group() default "default";
    String serviceToken() default"";
}
