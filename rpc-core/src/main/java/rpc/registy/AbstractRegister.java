package rpc.registy;

import java.util.List;
import java.util.Map;

import static rpc.common.cache.CommonCilentCache.SUBSCRIBE_SERVICE_LIST;
import static rpc.common.cache.CommonServerCache.PROVIDER_URL_SET;

public  abstract class AbstractRegister implements RegistyService{
    @Override
    public void register(URL url){
        PROVIDER_URL_SET.add(url);
    }
    @Override
    public void unRegister(URL url){
        PROVIDER_URL_SET.remove(url);
    }
    @Override
    public void subscribe(URL url){
        SUBSCRIBE_SERVICE_LIST.add(url);
    }
    @Override
    public void unSubscribe(URL url){
        SUBSCRIBE_SERVICE_LIST.remove(url);
    }

    /**
     * 获取一个服务接口的ip:port
     * @param serviceName
     * @return
     */
    public abstract Map<String ,String> getProviderNodeInfo(String serviceName);

    /**
     * 监听某个订阅后的结点
     * @param url
     */
    public abstract void watch(URL url);
}
