package rpc.core.common.cache;

import rpc.core.common.ChannelFuturePollingRef;
import rpc.core.common.ChannelFutureWrapper;
import rpc.core.common.RpcInvocation;
import rpc.core.common.config.ClientConfig;
import rpc.core.filter.client.ClientFilterChain;
import rpc.core.proxy.ProxyFactory;
import rpc.core.register.RegisterInfo;
import rpc.core.register.RegisterService;
import rpc.core.register.URL;
import rpc.core.router.Router;
import rpc.core.serialize.SerializeFactory;
import rpc.core.spi.ExtensionLoader;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class CommonClientCache {
    public static ClientConfig CLIENT_CONFIG;
    public static RegisterService REGISTER_SERVICE;
    public static ProxyFactory PROXY_FACTORY;
    public static BlockingQueue<RpcInvocation> SEND_QUEUE = new ArrayBlockingQueue<>(100);
    public static SerializeFactory SERIALIZE_FACTORY;

    public static Map<String, RegisterInfo> PROVIDER_INFO_MAP = new HashMap<>();

    public static Map<String,Object> RESP_MAP = new ConcurrentHashMap<>();

    //当前Client订阅了哪些服务serviceName->URL
    public static List<URL> SUBSCRIBE_SERVICE_LIST = new ArrayList<>();
    //com.test.service -> <<ip:host,urlString>,<ip:host,urlString>,<ip:host,urlString>>
    public static Map<String, Map<String,String>> URL_MAP = new ConcurrentHashMap<>();
    //记录所有服务提供者的ip和端口
    public static Set<String> SERVER_ADDRESS = new HashSet<>();

    //保存服务端的路由
    public static Map<String, List<ChannelFutureWrapper>> CONNECT_MAP = new ConcurrentHashMap<>();
    //每次进行远程调用的时候都是从这里面去选择服务提供者
    public static Map<String, ChannelFutureWrapper[]> SERVICE_ROUTER_MAP = new ConcurrentHashMap<>();
    public static ChannelFuturePollingRef CHANNEL_FUTURE_POLLING_REF = new ChannelFuturePollingRef();
    //路由组件
    public static Router ROUTER;
    //客户端序列化工厂
    public static SerializeFactory CLIENT_SERIALIZE_FACTORY;
    //客户但过滤链
    public static ClientFilterChain CLIENT_FILTER_CHAIN;
    //客户端配置类

    public static ExtensionLoader EXTENSION_LOADER = new ExtensionLoader();
}
