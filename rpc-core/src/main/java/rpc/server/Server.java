package rpc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import rpc.common.RpcDecoder;
import rpc.common.RpcEncoder;
import rpc.common.annotations.SPI;
import rpc.common.utils.CommonUtil;
import rpc.config.PropertiesBootstrap;
import rpc.config.PropertiesLoader;
import rpc.config.ServerConfig;
import rpc.filter.ServerFilter;
import rpc.filter.server.ServerAfterFilterChain;
import rpc.filter.server.ServerBeforeFilterChain;
import rpc.registy.AbstractRegister;
import rpc.registy.RegistyService;
import rpc.registy.URL;
import rpc.serialize.SerializeFactory;
import rpc.spi.ExtensionLoader;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.Map;


import static rpc.common.cache.CommonCilentCache.EXTENSION_LOADER;
import static rpc.common.cache.CommonServerCache.*;
import static rpc.common.constants.RpcConstants.DEFAULT_DECODE_CHAR;
import static rpc.spi.ExtensionLoader.EXTENSION_LOADER_CLASS_CACHE;

public class Server {



    public void startServerApplication() throws InterruptedException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        NioEventLoopGroup workGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(workGroup, bossGroup);
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.option(ChannelOption.TCP_NODELAY, true);
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        serverBootstrap.option(ChannelOption.SO_SNDBUF, 16 * 1024)
                .option(ChannelOption.SO_RCVBUF, 16 * 1024)
                .option(ChannelOption.SO_KEEPALIVE, true);
//        serverBootstrap.handler(new MaxConnectionLimitHandler(SERVER_CONFIG.getMaxConnections()));
        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
//                ByteBuf delimiter = Unpooled.copiedBuffer(DEFAULT_DECODE_CHAR.getBytes());
//                ch.pipeline().addLast(new DelimiterBasedFrameDecoder(10000, delimiter));
                ch.pipeline().addLast(new RpcEncoder());
                ch.pipeline().addLast(new RpcDecoder());
                ch.pipeline().addLast(new ServerHandle());
            }
        });
        /**
         * 初始化zookeeper
         */
        if (REGISTRY_SERVICE == null) {
            try {
                ExtensionLoader.loadExtension(RegistyService.class);
                Map<String,Class<?>> registryClassMap=EXTENSION_LOADER_CLASS_CACHE.get(RegistyService.class.getName());
                Class<?> registryClass=registryClassMap.get(SERVER_CONFIG.getRegisterType());
                REGISTRY_SERVICE =(AbstractRegister) registryClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("registryServiceType unKnow,error is ", e);
            }
        }
        /**
         * 初始化序列化器
         */
        String serverSerialize=SERVER_CONFIG.getServerSerialize();
        ExtensionLoader.loadExtension(SerializeFactory.class);
        Class<?> serializerClass= EXTENSION_LOADER_CLASS_CACHE.get(SerializeFactory.class.getName()).get(serverSerialize);
        if(serializerClass==null){
            throw new RuntimeException("no match serializeClass for "+serverSerialize);
        }
        SERVER_SERIALIZE_FACTORY=(SerializeFactory) serializerClass.newInstance();
        /**
         * 初始化过滤链
         */
        ServerBeforeFilterChain serverBeforeFilterChain=new ServerBeforeFilterChain();
        ServerAfterFilterChain serverAfterFilterChain=new ServerAfterFilterChain();
        ExtensionLoader.loadExtension(ServerFilter.class);
        LinkedHashMap<String,Class<?>> filterChainMap=EXTENSION_LOADER_CLASS_CACHE.get(ServerFilter.class.getName());
        for(String filterType:filterChainMap.keySet()){
            Class<?> filterChainImpl=filterChainMap.get(filterType);
            SPI  spi=(SPI) filterChainImpl.getAnnotation(SPI.class);
            if(spi!=null&&spi.value().equals("before")){
                serverBeforeFilterChain.addServerFilter((ServerFilter) filterChainImpl.newInstance());
            }
            else if(spi!=null&&spi.value().equals("after")){
                serverAfterFilterChain.add((ServerFilter) filterChainImpl.newInstance());
            }
        }
        SERVER_BEFORE_FILTER_CHAIN=serverBeforeFilterChain;
        SERVER_AFTER_FILTER_CHAIN=serverAfterFilterChain;
        /**
         * 初始化请求分发器
         */
        SERVER_CHANNEL_DISPATCHER.init(SERVER_CONFIG.getCoreThreadPool(),SERVER_CONFIG.getMaxThreadPool(),SERVER_CONFIG.getCoreThreadPool());
        SERVER_CHANNEL_DISPATCHER.startDataConsume();
        /**
         * 注册服务到注册中心
         */
        batchExportUrl();
        //绑定端口
        serverBootstrap.bind(SERVER_CONFIG.getPort()).sync();
    }

    public void initServerConfig() {
        SERVER_CONFIG = PropertiesBootstrap.loadServerConfigFromLocal();
    }

    /**
     * 将具体的服务放到注册中心
//     */
    public void batchExportUrl() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                for (URL url : PROVIDER_URL_SET) {
                    REGISTRY_SERVICE.register(url);
                }
            }
        });
        thread.start();
    }

    /**
     * 注册服务
     */
    public void registyService(ServiceWrapper serviceWrapper) {
        Object serviceBean = serviceWrapper.getServiceBean();
        /**
         * 没有接口
         */
        if (serviceBean.getClass().getInterfaces().length == 0) {
            throw new RuntimeException("service must only had one interfaces!");
        }
        Class<?>[] classes = serviceBean.getClass().getInterfaces();
        if (classes.length > 1) {
            throw new RuntimeException("service must only had one interfaces!");
        }
        Class<?> interfaceClass =classes[0];
        PROVIDER_CLASS_MAP.put(interfaceClass.getName(),serviceBean);
        URL url=new URL();
        url.setServiceName(interfaceClass.getName());
        url.setApplicationName(SERVER_CONFIG.getApplicationName());
        url.addParameters("host", CommonUtil.getIpAddress());
        url.addParameters("port",String.valueOf(SERVER_CONFIG.getPort()));
        url.addParameters("group",serviceWrapper.getGroup());
        url.addParameters("limit",String.valueOf(serviceWrapper.getLimit()));
        url.addParameters("weight",String.valueOf(serviceWrapper.getWeight()));
        /**
         * 放入需要注册的服务列表
         */
        PROVIDER_URL_SET.add(url);
    }

}
