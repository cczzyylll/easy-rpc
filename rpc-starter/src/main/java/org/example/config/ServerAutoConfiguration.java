package org.example.config;

import org.example.common.RpcService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import rpc.server.Server;
import rpc.server.ServiceWrapper;

import java.io.IOException;
import java.util.Map;

/**
 * 服务端自动配置
 */
@Component
public class ServerAutoConfiguration implements InitializingBean, ApplicationContextAware {
    private ApplicationContext applicationContext;
    @Override
    public void afterPropertiesSet() throws IOException, InterruptedException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        Server server=null;
        server=new Server();
        server.initServerConfig();
        Map<String,Object> beanMap=applicationContext.getBeansWithAnnotation(RpcService.class);
        if(beanMap.isEmpty()){
            return;
        }
        for(String beanName:beanMap.keySet()){
            Object bean=beanMap.get(beanName);
            ServiceWrapper serviceWrapper=new ServiceWrapper(bean);
            RpcService rpcService=bean.getClass().getAnnotation(RpcService.class);
            serviceWrapper.setLimit(rpcService.limit());
            serviceWrapper.setServiceToken(rpcService.serviceToken());
            serviceWrapper.setWeight(rpcService.weight());
            serviceWrapper.setGroup(rpcService.group());
            server.registyService(serviceWrapper);
        }
        server.startServerApplication();
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext){
        this.applicationContext=applicationContext;
    }
}
