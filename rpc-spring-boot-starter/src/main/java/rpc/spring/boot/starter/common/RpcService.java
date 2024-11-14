package rpc.spring.boot.starter.common;

import org.springframework.stereotype.Component;

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
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface RpcService {
    int limit() default 0;
    int weight() default 100;
    String group() default "default";
    String serviceToken() default "";
}
