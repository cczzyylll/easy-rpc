package rpc.config.AutoConfig;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import rpc.common.RpcService;
import rpc.server.Server;
import rpc.server.ServiceWrapper;

import java.io.IOException;
import java.util.Map;
public class ServerAutoConfig implements InitializingBean ,ApplicationContextAware{
    private ApplicationContext applicationContext;
    @Override
    public void afterPropertiesSet() throws InterruptedException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        Server server=null;
        Map<String ,Object> beanMap=applicationContext.getBeansWithAnnotation(RpcService.class);
        server=new Server();
        /**
         * 服务器初始化配置
         */
        server.initServerConfig();
        System.out.println("服务器配置完成");
        /**
         * 遍历被标记为希望暴露的服务Bean
         */
        for(String beanName:beanMap.keySet()){
            Object bean=beanMap.get(beanName);
            RpcService rpcService=bean.getClass().getAnnotation(RpcService.class);
            ServiceWrapper serviceWrapper=new ServiceWrapper(bean);
            serviceWrapper.setServiceToken(rpcService.serviceToken());
            serviceWrapper.setWeight(rpcService.weight());
            serviceWrapper.setLimit(rpcService.limit());
            server.registyService(serviceWrapper);
        }
        /**
         * 启动服务器
         */
        server.startServerApplication();
    }

    /**
     * 获取容器上下文
     * @param applicationContext
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext){
        this.applicationContext=applicationContext;
    }

}
