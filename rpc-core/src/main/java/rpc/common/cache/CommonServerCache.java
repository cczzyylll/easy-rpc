package rpc.common.cache;

import rpc.config.ServerConfig;
import rpc.dispatcher.ServerChannelDispatcher;
import rpc.filter.server.ServerAfterFilterChain;
import rpc.filter.server.ServerBeforeFilterChain;
import rpc.registy.RegistyService;
import rpc.registy.URL;
import rpc.serialize.SerializeFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 缓存 存储服务的公共信息
 */

public class CommonServerCache {
    /**
     * 需要注册的对象
     */
    public static final Map<String,Object> PROVIDER_CLASS_MAP=new HashMap<>();
    /**
     * 服务提供者提供的URL
     */
    public static final Set<URL> PROVIDER_URL_SET=new HashSet<>();
    /**
     * 注册中心
     */
    public static RegistyService REGISTRY_SERVICE;

    /**
     * 服务器配置类
     */
    public static ServerConfig SERVER_CONFIG;
    /**
     * 请求分发器
     */
    public static ServerChannelDispatcher SERVER_CHANNEL_DISPATCHER = new ServerChannelDispatcher();
    /**
     * 服务器序列化方式
     */
    public static SerializeFactory SERVER_SERIALIZE_FACTORY;
    /**
     * 服务器过滤链
     */
    public static ServerBeforeFilterChain SERVER_BEFORE_FILTER_CHAIN;
    public static ServerAfterFilterChain SERVER_AFTER_FILTER_CHAIN;

}
