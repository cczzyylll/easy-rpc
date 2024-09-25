package rpc.common;



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
@Target(ElementType.TYPE)//元注解 可以应用的目标元素类型  ElementType.TYPE表示可以应用到类、接口、枚举或者注解类型
@Retention(RetentionPolicy.RUNTIME)//元注解 在运行时保留，可以运用反射机制
@Documented//标记注解，指示编译器将注解信息包含在生成的文档中
@Component//Spring框架提供的注解，Spring可以自动扫描并且标识带有该注解的类，将其加入容器中
public @interface RpcService {
    //限流
    int limit() default 0;
    //服务权重[100的倍数] (该参数和路由策略有关，只有随机策略才会使用)
    int weight() default 100;
    //服务分组
    String group() default "default";
    //令牌校验
    String serviceToken() default "";
}
