package org.example.config;

import org.example.common.RpcReference;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import rpc.client.Client;
import rpc.client.RpcReferenceWrapper;

import java.lang.reflect.Field;

@Component
public class ClientAutoConfiguration implements BeanPostProcessor, ApplicationListener<ApplicationReadyEvent> {
    private volatile boolean hasInitClientConfig=false;
    private static rpc.client.RpcReference rpcReference=null;
    private static Client client=null;
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException{
        Field[] fields=bean.getClass().getDeclaredFields();
        for(Field field:fields){
            if(field.isAnnotationPresent(RpcReference.class)){
                System.out.println(field.getType());
                if(!hasInitClientConfig){
                    client=new Client();
                    try{
                        client.initClientConfig();
                        rpcReference=client.initClientApplication();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    hasInitClientConfig=true;
                }
                RpcReference rpcReference1=field.getAnnotation(RpcReference.class);
                try {
                    field.setAccessible(true);
                    Object refObj=field.get(bean);
                    RpcReferenceWrapper rpcReferenceWrapper=new RpcReferenceWrapper();
                    rpcReferenceWrapper.setAimClass(field.getType());
                    rpcReferenceWrapper.setGroup(rpcReference1.group());
                    rpcReferenceWrapper.setServiceToken(rpcReference1.serviceToken());
                    rpcReferenceWrapper.setUrl(rpcReference1.url());
                    rpcReferenceWrapper.setTimeOut(rpcReference1.timeOut());
                    rpcReferenceWrapper.setRetry(rpcReference1.retry());
                    rpcReferenceWrapper.setAsync(rpcReference1.async());
                    try {
                        refObj=rpcReference.getProxyObject(rpcReferenceWrapper);
                    }
                    catch (Throwable throwable){
                        throwable.printStackTrace();
                    }
                    field.set(bean,refObj);
                    client.subscribeService(field.getType());
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }
    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent){
        if(hasInitClientConfig&&client!=null){
            client.connectServer();
            client.startClient();
        }
    }
    }

