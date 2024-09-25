package org.example.common;

import java.lang.annotation.*;

/**
 * rpc远程调用注解
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcReference {
    String url() default "";
    String group() default "";
    String serviceToken() default "";
    int timeOut() default 3000;
    int retry() default 1;
    boolean async() default false;
}
