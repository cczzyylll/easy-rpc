package rpc.client;


import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import rpc.common.RpcDecoder;
import rpc.common.RpcEncoder;
import rpc.common.RpcInvocation;
import rpc.common.RpcProtocol;
import rpc.common.utils.CommonUtil;
import rpc.config.PropertiesBootstrap;
import rpc.proxy.ProxyFactory;
import rpc.registy.AbstractRegister;
import rpc.registy.RegistyService;
import rpc.registy.URL;
import rpc.router.Router;
import rpc.serialize.SerializeFactory;
import rpc.spi.ExtensionLoader;


import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static rpc.common.cache.CommonCilentCache.*;
import static rpc.common.constants.RpcConstants.DEFAULT_DECODE_CHAR;

public class Client {
    private AbstractRegister abstractRegister;
    private final Bootstrap bootstrap=new Bootstrap();
    public RpcReference initClientApplication() throws InstantiationException, IllegalAccessException, IOException, ClassNotFoundException {
        NioEventLoopGroup clientGroup=new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(clientGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) {
//                ByteBuf delimiter = Unpooled.copiedBuffer(DEFAULT_DECODE_CHAR.getBytes());
//                ch.pipeline().addLast(new DelimiterBasedFrameDecoder(10000, delimiter));
                ch.pipeline().addLast(new RpcEncoder());
                ch.pipeline().addLast(new RpcDecoder());
                ch.pipeline().addLast(new ClientHandle());
            }
        });
        /**
         * 初始化连接器
         */
        ConnectionHandle.setBootstrap(bootstrap);
        /**
         * 初始化监听器
         */
        System.out.println("初始化监听器");
        /**
         *初始化路由策略
         */
        String routerStrategy=CLIENT_CONFIG.getRouterStrategy();
        ExtensionLoader.loadExtension(Router.class);
        LinkedHashMap<String,Class<?>> routerMap=ExtensionLoader.EXTENSION_LOADER_CLASS_CACHE.get(Router.class.getName());
        Class<?> routerImpl=routerMap.get(routerStrategy);
        ROUTER=(Router) routerImpl.newInstance();
        /**
         * 初始化序列器
         */
        String serializeType=CLIENT_CONFIG.getClientSerialize();
        ExtensionLoader.loadExtension(SerializeFactory.class);
        LinkedHashMap<String,Class<?>> serializeMap=ExtensionLoader.EXTENSION_LOADER_CLASS_CACHE.get(SerializeFactory.class.getName());
        CLIENT_SERIALIZE_FACTORY =(SerializeFactory) serializeMap.get(serializeType).newInstance();
        /**
         * 初始化代理工厂
         */
        String proxyType= CLIENT_CONFIG.getProxyType();
        ExtensionLoader.loadExtension(ProxyFactory.class);
        LinkedHashMap<String ,Class<?>> proxyMap=ExtensionLoader.EXTENSION_LOADER_CLASS_CACHE.get(ProxyFactory.class.getName());
        Class<?> proxyFactoryImpl= proxyMap.get(proxyType);
        /**
         * 初始化zookeeper
         */
        if(abstractRegister==null) {
            String registerType=CLIENT_CONFIG.getRegisterType();
            ExtensionLoader.loadExtension(RegistyService.class);
            LinkedHashMap<String ,Class<?>> registerMap=ExtensionLoader.EXTENSION_LOADER_CLASS_CACHE.get(RegistyService.class.getName());
            Class<?> registerClass=registerMap.get(registerType);
            abstractRegister=(AbstractRegister) registerClass.newInstance();
        }
        return new RpcReference((ProxyFactory) proxyFactoryImpl.newInstance());
    }
    public void initClientConfig(){
        CLIENT_CONFIG=PropertiesBootstrap.loadClientConfigFromLocal();
    }
    /**
     * 启动之前订阅服务
     */
    public void subscribeService(Class<?> serviceBean) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        URL url =new URL();
        url.setApplicationName(CLIENT_CONFIG.getApplicationName());
        url.setServiceName(serviceBean.getName());
        url.addParameters("host", CommonUtil.getIpAddress());
        Map<String ,String> result=abstractRegister.getProviderNodeInfo(serviceBean.getName());
        URL_MAP.put(url.getServiceName(),result);
        abstractRegister.subscribe(url);
    }
    /**
     * 和各个provider建立连接
     */
    public void connectServer(){
        for (String serviceName:URL_MAP.keySet()){
            Map<String,String> serverInfoMap=URL_MAP.get(serviceName);
            for (String serverAddress:serverInfoMap.keySet()){
                try {
                    ConnectionHandle.connect(serviceName,serverAddress);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
//        for (URL providerUrl : SUBSCRIBE_SERVICE_LIST) {
//            List<String> providerIps = abstractRegister.getProviderIps(providerUrl.getServiceName());
//            for (String providerIp : providerIps) {
//                try {
//                    ConnectionHandler.connect(providerUrl.getServiceName(), providerIp);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//            URL url = new URL();
//            url.setServiceName(providerUrl.getServiceName());
//            url.addParameter("providerIps", JSON.toJSONString(providerIps));
//            //客户端在此新增一个订阅的功能
//            abstractRegister.doAfterSubscribe(url);
//        }

    }
    public void startClient(){
        Thread asyncSendJob=new Thread(new AsyncSendJob(),"ClientAsyncSendJobThread");
        asyncSendJob.start();
    }

    /**
     * 异步发送消息
     */
    class AsyncSendJob implements Runnable{
        public AsyncSendJob(){}
        @Override
        public void run(){
            while (true){
                try {
                    RpcInvocation data=SEND_QUEUE.take();
                    byte[] serialize= CLIENT_SERIALIZE_FACTORY.serialize(data);
                    RpcProtocol rpcProtocol=new RpcProtocol(serialize);
                    ChannelFuture channelFuture=ConnectionHandle.getChannelFuture(data);
                    channelFuture.channel().writeAndFlush(rpcProtocol);
                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
    }

}
