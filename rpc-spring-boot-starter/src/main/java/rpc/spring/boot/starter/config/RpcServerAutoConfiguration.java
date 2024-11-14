//package rpc.spring.boot.starter.config;
//
//import rpc.core.server.Server;
//import rpc.core.server.ServerShutdownHook;
//import rpc.core.server.ServiceWrapper;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.InitializingBean;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//import rpc.spring.boot.starter.common.RpcService;
//
//import java.util.Map;
//
//import static rpc.core.common.cache.CommonServerCache.SERVER_CONFIG;
//
//public class RpcServerAutoConfiguration implements InitializingBean, ApplicationContextAware {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServerAutoConfiguration.class);
//
//    private ApplicationContext applicationContext;
//
//    @Override
//    public void afterPropertiesSet() throws Exception {
//        Server server = null;
//        Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(EasyRpcService.class);
//        if (beanMap.size() == 0) {
//            //说明当前应用内部不需要对外暴露服务
//            return;
//        }
//        long begin = System.currentTimeMillis();
//        server = new Server();
//        server.initServerConfig();
//        for (String beanName : beanMap.keySet()) {
//            Object bean = beanMap.get(beanName);
//            RpcService easyRpcService = bean.getClass().getAnnotation(EasyRpcService.class);
//            ServiceWrapper dataServiceServiceWrapper = new ServiceWrapper(bean, easyRpcService.group());
//            dataServiceServiceWrapper.setServiceToken(easyRpcService.serviceToken());
//            dataServiceServiceWrapper.setLimit(easyRpcService.limit());
//            dataServiceServiceWrapper.setWeight(easyRpcService.weight());
//            server.registerService(dataServiceServiceWrapper);
//        }
//        ServerShutdownHook.registryShutdownHook();
//        server.startServerApplication();
//        long end = System.currentTimeMillis();
//        LOGGER.info(" ================== [{}] started success in {}s ================== ", SERVER_CONFIG.getApplicationName(), ((double) end - (double) begin) / 1000);
//    }
//
//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        this.applicationContext = applicationContext;
//    }
//}
