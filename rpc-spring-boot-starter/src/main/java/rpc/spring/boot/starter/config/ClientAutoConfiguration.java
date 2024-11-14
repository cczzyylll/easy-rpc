package rpc.spring.boot.starter.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import rpc.core.client.Client;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import rpc.core.client.RpcReferenceWrapper;
import rpc.spring.boot.starter.common.RpcReference;

import java.lang.reflect.Field;
import java.util.Map;

@Configuration
@ConditionalOnClass(Client.class)
public class ClientAutoConfiguration{
    private static final Logger logger = LogManager.getLogger(ClientAutoConfiguration.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    @ConditionalOnMissingBean
    Client rpcClient() {
        try {
            Client client = new Client();
            client.initClientConfig();
            client.initClientApplication();
            logger.info("rpc client start successfully");
            doSubscribe(client);
            return client;
        }
        catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    private void doSubscribe(Client client) {
        Map<String, Object> beanMap = applicationContext.getBeansOfType(Object.class);
        for (String beanName : beanMap.keySet()) {
            Field[] fields = beanName.getClass().getDeclaredFields();
            for (Field field : fields) {
                logger.info(fields.getClass().getName());
                if (field.isAnnotationPresent(RpcReference.class)) {
                    RpcReference rpcReference = field.getAnnotation(RpcReference.class);
                    try {
                        field.setAccessible(true);
//                        Object refObj = field.get(bean);
                        RpcReferenceWrapper rpcReferenceWrapper = new RpcReferenceWrapper();
                        rpcReferenceWrapper.setAimClass(field.getType());
                        rpcReferenceWrapper.setGroup(rpcReference.group());
                        rpcReferenceWrapper.setServiceToken(rpcReference.serviceToken());
                        rpcReferenceWrapper.setUrl(rpcReference.url());
                        rpcReferenceWrapper.setTimeOut(rpcReference.timeOut());
                        rpcReferenceWrapper.setRetry(rpcReference.retry());
                        rpcReferenceWrapper.setAsync(rpcReference.async());
//                        refObj = rpcReference.get(rpcReferenceWrapper);
//                        field.set(bean, refObj);
                        client.doSubscribeService(field.getType());
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
