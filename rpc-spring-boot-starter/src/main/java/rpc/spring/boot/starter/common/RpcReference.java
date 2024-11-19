package rpc.spring.boot.starter.common;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author peng
 * @Date 2023/3/11
 * @description:
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcReference {
    String url() default "";
    String group() default "default";
    String serviceToken() default "";
    int timeOut() default 3000;
    int retry() default 1;
    boolean async() default false;
}
