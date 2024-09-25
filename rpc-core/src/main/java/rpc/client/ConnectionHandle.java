package rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import rpc.common.ChannelFutureWrapper;
import rpc.common.RpcInvocation;
import rpc.router.Selector;

import java.util.ArrayList;
import java.util.List;

import static rpc.common.cache.CommonCilentCache.*;

/**
 * 单一职责设计原则
 */

public class ConnectionHandle {
    /**
     * 连接的核心处理器
     */
    private static Bootstrap bootstrap;
    public static void setBootstrap(Bootstrap bootstrap){
        ConnectionHandle.bootstrap=bootstrap;
    }

    /**
     * 连接主要做了以下几件事
     * 1.建立与服务器的连接并且获取Netty的ChannelFuture
     * 2.封装ChannelFuture到ChannelFutureWrapper
     * 3.将这个连接信息存储到客户端缓存的CONNECT_MAP中
     * 4.将服务器端的地址（ip：port）存入SERVER_ADDRESS
     * 5.刷新路由
     */
    public static void connect(String providerServiceName,String providerAdd)throws InterruptedException{
        if(bootstrap==null){
            throw new RuntimeException("boostrap can not be null");
        }
        if (!providerAdd.contains(":")){
            return;
        }
        String []providerAddress=providerAdd.split(":");
        String ip=providerAddress[0];
        int port=Integer.parseInt(providerAddress[1]);
        ChannelFuture channelFuture=createChannelFuture(ip,port);
        ChannelFutureWrapper channelFutureWrapper=new ChannelFutureWrapper(ip,port);
        channelFutureWrapper.setChannelFuture(channelFuture);
        SERVER_ADDRESS.add(providerAdd);
        List<ChannelFutureWrapper> channelFutureWrapperList=CONNECT_MAP.getOrDefault(providerServiceName,new ArrayList<>());
        channelFutureWrapperList.add(channelFutureWrapper);
        CONNECT_MAP.put(providerServiceName,channelFutureWrapperList);
        Selector selector=new Selector(providerServiceName);
        ROUTER.refreshRouterArr(selector);
    }
    public static ChannelFuture createChannelFuture(String ip,Integer port) throws InterruptedException {
        return bootstrap.connect(ip,port).sync();
    }
    public static void disConnect(String providerServiceName,String providerAdd){
        SERVER_ADDRESS.remove(providerAdd);
        List<ChannelFutureWrapper> channelFutureWrapperList=CONNECT_MAP.get(providerServiceName);
        if(!channelFutureWrapperList.isEmpty()){
            channelFutureWrapperList.removeIf(channelFutureWrapper -> providerAdd.equals(channelFutureWrapper.getIp()+":"+channelFutureWrapper.getPort()));
        }
    }

    /**
     * TODO过滤
     * @param rpcInvocation
     * @return
     */
    public static ChannelFuture getChannelFuture(RpcInvocation rpcInvocation){
        String providerServiceName=rpcInvocation.getTargetServiceName();
        List<ChannelFutureWrapper> channelFutureWrapperList=CONNECT_MAP.get(providerServiceName);
        if(channelFutureWrapperList.isEmpty()){
            throw  new RuntimeException(("no providerServiceName for"+ providerServiceName));
        }
//        return channelFutureWrapperList.get(0).getChannelFuture();
        return ROUTER.select(channelFutureWrapperList.toArray((new ChannelFutureWrapper[0]))).getChannelFuture();
    }
}
