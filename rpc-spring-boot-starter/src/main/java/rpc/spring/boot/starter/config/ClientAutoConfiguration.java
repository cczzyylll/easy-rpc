package rpc.spring.boot.starter.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import rpc.core.client.Client;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import rpc.core.client.RpcReferenceWrapper;
import rpc.core.common.cache.CommonClientCache;
import rpc.spring.boot.starter.common.RpcReference;

import java.lang.reflect.Field;
import java.util.Map;

@Configuration
@ConditionalOnClass(Client.class)
public class ClientAutoConfiguration {
    private static final Logger logger = LogManager.getLogger(ClientAutoConfiguration.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    @ConditionalOnMissingBean
    Client rpcClient() {
        try {
            Client client = new Client();
            client.loadClientConfig();
            client.initClient();
            logger.info("rpc client start successfully");
            doSubscribeAndProxy(client);
            return client;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    private void doSubscribeAndProxy(Client client) {
        Map<String, Object> beanMap = applicationContext.getBeansOfType(Object.class);
        for (String beanName : beanMap.keySet()) {
            Field[] fields = beanMap.get(beanName).getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(RpcReference.class)) {
                    field.setAccessible(true);
                    try {
                        client.doSubscribeService(field.getType());
                        doProxy(field);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void doProxy(Field field) throws Throwable {
        RpcReference rpcReference = field.getAnnotation(RpcReference.class);
        RpcReferenceWrapper rpcReferenceWrapper = new RpcReferenceWrapper();
        rpcReferenceWrapper.setAimClass(field.getType());
        rpcReferenceWrapper.setGroup(rpcReference.group());
        rpcReferenceWrapper.setServiceToken(rpcReference.serviceToken());
        rpcReferenceWrapper.setUrl(rpcReference.url());
        rpcReferenceWrapper.setTimeOut(rpcReference.timeOut());
        rpcReferenceWrapper.setRetry(rpcReference.retry());
        rpcReferenceWrapper.setAsync(rpcReference.async());
        Object proxyObject = CommonClientCache.PROXY_FACTORY.getProxy(rpcReferenceWrapper);
        field.set(field.getDeclaringClass(), proxyObject);
    }

}
