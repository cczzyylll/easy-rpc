package rpc.core.register;

import rpc.core.common.cache.CommonClientCache;
import rpc.core.common.cache.CommonServerCache;

import java.util.List;
import java.util.Map;

/**
 * @Author peng
 * @Date 2023/2/27
 * @description: 注册中心抽象类，对一些注册数据做统一的处理，假设日后需要考虑支持多种类型的注册中心。
 */
public abstract class AbstractRegister  implements RegistryService {


    @Override
    public void register(URL url) {
        CommonServerCache.PROVIDER_URL_SET.add(url);
    }

    @Override
    public void unRegister(URL url) {
        CommonServerCache.PROVIDER_URL_SET.remove(url);
    }

    @Override
    public void subscribe(URL url) {
        CommonClientCache.SUBSCRIBE_SERVICE_LIST.add(url);
    }

    @Override
    public void doUnSubscribe(URL url) {
        CommonClientCache.SUBSCRIBE_SERVICE_LIST.remove(url);
    }

    /**
     * 留给子类扩展
     * 订阅操作执行之前需要执行的逻辑
     * @param url
     */
    public abstract void doBeforeSubscribe(URL url);

    /**
     * 留给子类扩展
     * 订阅操作执行之后需要执行的逻辑
     * @param url
     */
    public abstract void doAfterSubscribe(URL url);

    /**
     * 留给子类扩展
     * 获取服务提供者的ip
     * @param serviceName
     * @return
     */
    public abstract List<String> getProviderIps(String serviceName);

    /**
     * 获取服务的权重信息
     *
     * @param serviceName
     * @return <ip:port --> urlString>,<ip:port --> urlString>,<ip:port --> urlString>,<ip:port --> urlString>
     */
    public abstract Map<String, String> getServiceWeightMap(String serviceName);

}