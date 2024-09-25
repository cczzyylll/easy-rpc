package rpc.common.cache;


import rpc.common.ChannelFutureWrapper;
import rpc.common.RpcInvocation;
import rpc.config.ClientConfig;
import rpc.registy.URL;
import rpc.router.Router;
import rpc.serialize.SerializeFactory;
import rpc.spi.ExtensionLoader;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class CommonCilentCache {
    /**
     * 当前订阅者订阅的服务
     */
    public static List<URL> SUBSCRIBE_SERVICE_LIST=new ArrayList<>();
    /**
     * spi加载组件
     */
    public static ExtensionLoader EXTENSION_LOADER = new ExtensionLoader();
    /**
     * 保存返回的结果
     */
    public static Map<String,Object> RESP_MAP=new ConcurrentHashMap<>();
    /**
     * 发送队列
     */
    public static BlockingQueue<RpcInvocation> SEND_QUEUE=new ArrayBlockingQueue<>(100);
    /**
     * 客户端序列化工厂
     */
    public static SerializeFactory CLIENT_SERIALIZE_FACTORY;
    //com.test.service -> <<ip:host,urlString>,<ip:host,urlString>,<ip:host,urlString>>
    public static Map<String, Map<String,String>> URL_MAP = new ConcurrentHashMap<>();
    /**
     * 记录所有服务提供者的ip和端口
     */
    public static Set<String> SERVER_ADDRESS=new HashSet<>();
    /**
     * 保存通道信息
     */
    public static Map<String,List<ChannelFutureWrapper>> CONNECT_MAP=new ConcurrentHashMap<>();
    /**
     * 路由选择后的通道信息
     */
    public static Map<String,ChannelFutureWrapper[]> SERVICE_ROUTE_MAP=new ConcurrentHashMap<>();
    /**
     * 路由组件
     */
    public static Router ROUTER;
    /**
     * 客户端配置
     */
    public static ClientConfig CLIENT_CONFIG;

}
