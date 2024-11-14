package rpc.core.server;

import com.google.common.collect.ImmutableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rpc.core.common.RpcDecoder;
import rpc.core.common.RpcEncoder;
import rpc.core.common.ServerServiceSemaphoreWrapper;
import rpc.core.common.annotations.SPI;
import rpc.core.common.config.PropertiesBootstrap;
import rpc.core.common.event.RpcListenerLoader;
import rpc.core.common.utils.CommonUtil;
import rpc.core.filter.ServerFilter;
import rpc.core.filter.server.ServerAfterFilterChain;
import rpc.core.filter.server.ServerBeforeFilterChain;
import rpc.core.register.RegisterInfo;
import rpc.core.register.URL;
import rpc.core.register.zookeeper.ZRigister;
import rpc.core.serialize.SerializeFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import rpc.core.common.cache.CommonClientCache;
import rpc.core.common.cache.CommonServerCache;
import rpc.core.spi.ExtensionLoader;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static rpc.core.common.constants.RpcConstants.DEFAULT_DECODE_CHAR;
import static rpc.core.register.RegisterInfo.*;

public class Server {
    private static final Logger logger = LogManager.getLogger(Server.class);

    public void startServerApplication() throws InterruptedException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        bootstrap.option(ChannelOption.SO_SNDBUF, 16 * 1024)
                .option(ChannelOption.SO_RCVBUF, 16 * 1024)
                .option(ChannelOption.SO_KEEPALIVE, true);

        //服务端采用单一长连接的模式，这里所支持的最大连接数和机器本身的性能有关
        //连接防护的handler应该绑定在Main-Reactor上
        bootstrap.handler(new MaxConnectionLimitHandler(CommonServerCache.SERVER_CONFIG.getMaxConnections()));
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ByteBuf delimiter = Unpooled.copiedBuffer(DEFAULT_DECODE_CHAR.getBytes());
                ch.pipeline().addLast(new DelimiterBasedFrameDecoder(CommonServerCache.SERVER_CONFIG.getMaxServerRequestData(), delimiter));
                ch.pipeline().addLast(new RpcEncoder());
                ch.pipeline().addLast(new RpcDecoder());
                ch.pipeline().addLast(new ServerHandler());
            }
        });

        //初始化监听器
        RpcListenerLoader rpcListenerLoader = new RpcListenerLoader();
        rpcListenerLoader.init();

        //初始化序列化器
        String serverSerialize = CommonServerCache.SERVER_CONFIG.getServerSerialize();
        CommonClientCache.EXTENSION_LOADER.loadExtension(SerializeFactory.class);
        LinkedHashMap<String, Class<?>> serializeMap = ExtensionLoader.EXTENSION_LOADER_CLASS_CACHE.get(SerializeFactory.class.getName());
        Class<?> serializeClass = serializeMap.get(serverSerialize);
        if (serializeClass == null) {
            throw new RuntimeException("no match serializeClass for " + serverSerialize);
        }
        CommonServerCache.SERVER_SERIALIZE_FACTORY = (SerializeFactory) serializeClass.newInstance();


        //初始化过滤链
        ServerBeforeFilterChain serverBeforeFilterChain = new ServerBeforeFilterChain();
        ServerAfterFilterChain serverAfterFilterChain = new ServerAfterFilterChain();
        CommonClientCache.EXTENSION_LOADER.loadExtension(ServerFilter.class);
        LinkedHashMap<String, Class<?>> filterChainMap = ExtensionLoader.EXTENSION_LOADER_CLASS_CACHE.get(ServerFilter.class.getName());
        for (Map.Entry<String, Class<?>> filterChainEntry : filterChainMap.entrySet()) {
            String filterChainKey = filterChainEntry.getKey();
            Class<?> filterChainImpl = filterChainEntry.getValue();
            if (filterChainImpl == null) {
                throw new RuntimeException("no match filterChainImpl for " + filterChainKey);
            }
            SPI spi = (SPI) filterChainImpl.getDeclaredAnnotation(SPI.class);
            if (spi != null && "before".equalsIgnoreCase(spi.value())) {
                serverBeforeFilterChain.addServerFilter((ServerFilter) filterChainImpl.newInstance());
            } else if (spi != null && "after".equalsIgnoreCase(spi.value())) {
                serverAfterFilterChain.addServerFilter((ServerFilter) filterChainImpl.newInstance());
            }
        }
        CommonServerCache.SERVER_BEFORE_FILTER_CHAIN = serverBeforeFilterChain;
        CommonServerCache.SERVER_AFTER_FILTER_CHAIN = serverAfterFilterChain;

        //初始化请求分发器
        CommonServerCache.SERVER_CHANNEL_DISPATCHER.init(CommonServerCache.SERVER_CONFIG.getServerQueueSize(), CommonServerCache.SERVER_CONFIG.getServerBizThreadNums());
        CommonServerCache.SERVER_CHANNEL_DISPATCHER.startDataConsume();

        //暴露服务端url
        this.batchExportUrl();
        bootstrap.bind(CommonServerCache.SERVER_CONFIG.getPort()).sync();
    }

    public void initServerConfig() {
        CommonServerCache.SERVER_CONFIG = PropertiesBootstrap.loadServerConfigFromLocal();
    }

    /**
     * 将服务端的具体服务都暴露到注册中心
     */
    public void batchExportUrl() {
        Thread task = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (URL url : CommonServerCache.PROVIDER_URL_SET) {
                    CommonServerCache.REGISTRY_SERVICE.register(url);
                }
            }
        });
        task.start();
    }


    public void registerService(ServiceWrapper serviceWrapper) {
        Object serviceBean = serviceWrapper.getServiceBean();
        if (serviceBean.getClass().getInterfaces().length == 0) {
            throw new RuntimeException("service must had interfaces!");
        }
        Class<?>[] classes = serviceBean.getClass().getInterfaces();
        if (classes.length > 1) {
            throw new RuntimeException("service must only had one interfaces!");
        }
        if (CommonServerCache.REGISTER_SERVICE == null) {
            initRegister();
        }
        for (Class<?> interfaceClass : classes) {
//            CommonServerCache.PROVIDER_CLASS_MAP.put(interfaceClass.getName(), serviceBean);
            RegisterInfo registerInfo = RegisterInfo.builder()
                    .application(CommonServerCache.SERVER_CONFIG.getApplicationName())
                    .serviceName(interfaceClass.getName())
                    .parameters(ImmutableMap.of(IP, CommonUtil.getIpAddress(),
                            PORT, String.valueOf(CommonServerCache.SERVER_CONFIG.getPort()),
                            GROUP, serviceWrapper.getGroup(),
                            WEIGHT, String.valueOf(serviceWrapper.getWeight())))
                    .build();
            logger.info(registerInfo);
            CommonServerCache.REGISTER_SERVICE.register(registerInfo);
        }

    }

    private void initRegister() {
        if (CommonServerCache.REGISTER_SERVICE == null) {
            try {
                CommonServerCache.REGISTER_SERVICE = new ZRigister("152.136.150.223:2181");
            } catch (Exception e) {
                throw new RuntimeException("registryServiceType unKnow,error is ", e);
            }
        }
    }

}
