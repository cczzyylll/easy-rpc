package rpc.core.client;

import rpc.core.common.ChannelFutureWrapper;
import rpc.core.common.RpcInvocation;
import rpc.core.common.event.data.ProviderNodeInfo;
import rpc.core.common.utils.CommonUtil;
import rpc.core.register.URL;
import rpc.core.router.Selector;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.core.common.cache.CommonClientCache;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author peng
 * @Date 2023/2/28
 * @description: 按照单一职责的设计原则，将与连接(建立、断开)有关的功能都统一封装在了一起.
 */
public class ConnectionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionHandler.class);

    /**
     * 核心的连接处理器
     * 专门用于负责和服务端构建连接通信
     */
    private static Bootstrap bootstrap;

    public static void setBootstrap(Bootstrap bootstrap) {
        ConnectionHandler.bootstrap = bootstrap;
    }

    /**
     * 构建单个连接通道 元操作，既要处理连接，还要统一将连接进行内存存储管理
     *
     * @param providerIp
     * @return
     * @throws InterruptedException
     */
    public static void connect(String providerServiceName, String providerIp) throws InterruptedException {
        if (bootstrap == null) {
            throw new RuntimeException("bootstrap can not be null");
        }
        //格式错误类型的信息
        if(!providerIp.contains(":")){
            return;
        }
        String[] providerAddress = providerIp.split(":");
        String ip = providerAddress[0];
        int port = Integer.parseInt(providerAddress[1]);
        //到底这个channelFuture里面是什么
        ChannelFuture channelFuture = bootstrap.connect(ip, port).sync();
        String providerUrlInfo = CommonClientCache.URL_MAP.get(providerServiceName).get(providerIp);
        ProviderNodeInfo providerNodeInfo = URL.buildUrlFromUrlStr(providerUrlInfo);
        LOGGER.info("与[providerUrlInfo]建立连接"+providerUrlInfo);
        ChannelFutureWrapper channelFutureWrapper = new ChannelFutureWrapper();
        channelFutureWrapper.setChannelFuture(channelFuture);
        channelFutureWrapper.setHost(ip);
        channelFutureWrapper.setPort(port);
        channelFutureWrapper.setGroup(providerNodeInfo.getGroup());
        channelFutureWrapper.setWeight(providerNodeInfo.getWeight());
        CommonClientCache.SERVER_ADDRESS.add(providerIp);
        List<ChannelFutureWrapper> channelFutureWrappers = CommonClientCache.CONNECT_MAP.getOrDefault(providerServiceName, new ArrayList<>());
        channelFutureWrappers.add(channelFutureWrapper);
        //key是服务的名字，value是对应的channel通道的List集合
        CommonClientCache.CONNECT_MAP.put(providerServiceName, channelFutureWrappers);
        Selector selector = new Selector();
        selector.setProviderServiceName(providerServiceName);
        CommonClientCache.ROUTER.refreshRouterArr(selector);
    }

    /**
     * 构建ChannelFuture
     * @param ip
     * @param port
     * @return
     * @throws InterruptedException
     */
    public static ChannelFuture createChannelFuture(String ip,Integer port) throws InterruptedException {
        return bootstrap.connect(ip, port).sync();
    }

    /**
     * 断开连接
     *
     * @param providerServiceName
     * @param providerIp
     */
    public static void disConnect(String providerServiceName, String providerIp) {
        CommonClientCache.SERVER_ADDRESS.remove(providerIp);
        List<ChannelFutureWrapper> channelFutureWrappers = CommonClientCache.CONNECT_MAP.get(providerServiceName);
        if (CommonUtil.isNotEmptyList(channelFutureWrappers)) {
            channelFutureWrappers.removeIf(channelFutureWrapper ->
                    providerIp.equals(channelFutureWrapper.getHost() + ":" + channelFutureWrapper.getPort()));
        }
    }

    /**
     * 默认走随机策略获取ChannelFuture
     *
     * @param rpcInvocation
     * @return
     */
    public static ChannelFuture getChannelFuture(RpcInvocation rpcInvocation) {
        String providerServiceName = rpcInvocation.getTargetServiceName();
        List<ChannelFutureWrapper> channelFutureWrapperList = CommonClientCache.CONNECT_MAP.get(providerServiceName);
        if (CommonUtil.isEmptyList(channelFutureWrapperList)) {
            throw new RuntimeException("no provider exist for " + providerServiceName);
        }
        CommonClientCache.CLIENT_FILTER_CHAIN.doFilter(channelFutureWrapperList, rpcInvocation);
        //过滤掉不符合条件的通道
        ChannelFutureWrapper[] allChannelFutureWrappers = CommonClientCache.SERVICE_ROUTER_MAP.get(providerServiceName);
        List<ChannelFutureWrapper> channelFutureWrappers = new ArrayList<>();
        for (ChannelFutureWrapper channelFutureWrapper : allChannelFutureWrappers) {
            if (channelFutureWrapperList.contains(channelFutureWrapper)){
                channelFutureWrappers.add(channelFutureWrapper);
            }
        }

        return CommonClientCache.ROUTER.select(channelFutureWrappers.toArray(new ChannelFutureWrapper[0])).getChannelFuture();
    }


}
