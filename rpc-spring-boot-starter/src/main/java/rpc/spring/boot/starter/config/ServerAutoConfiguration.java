package rpc.spring.boot.starter.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rpc.core.register.zookeeper.LoopWatcher;
import rpc.core.server.Server;
import rpc.core.server.ServiceWrapper;
import rpc.spring.boot.starter.common.RpcService;

import java.io.IOException;
import java.util.Map;

@Configuration
@ConditionalOnClass(Server.class)
public class ServerAutoConfiguration {
    private static final Logger logger = LogManager.getLogger(ServerAutoConfiguration.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    @ConditionalOnMissingBean
    public Server rpcServer(){
        Server server = new Server();
        server.initServerConfig();
        registerService(server);
        try {
            server.startServerApplication();
        }
        catch (Exception e) {
            logger.error(e);
            return server;
        }
        logger.info("rpc server start successfully");
        return server;
    }

    private void registerService(Server server) {
        Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        for (String beanName : beanMap.keySet()) {
            Object bean = beanMap.get(beanName);
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            ServiceWrapper serviceWrapper = ServiceWrapper.builder()
                    .serviceBean(bean)
                    .limit(rpcService.limit())
                    .weight(rpcService.weight())
                    .group(rpcService.group())
                    .serviceToken(rpcService.serviceToken())
                    .build();
            server.registerService(serviceWrapper);
        }
    }
}
