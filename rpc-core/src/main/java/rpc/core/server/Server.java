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
import rpc.core.register.zookeeper.ZookeeperRegister;
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
    private static final int QUEUE_SIZE = 1024;
    private static final boolean IS_NAGLE = true;
    private static final int SND_BUF_SIZE = 16 * 1024;
    private static final int RCV_BUF_SIZE = 16 * 1024;
    private static final boolean IS_KEEPALIVE = true;


    public void startServerApplication() throws InterruptedException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        initNioEventLoopGroup();
        //初始化监听器
        initListener();

        //初始化序列化器
        initSerialize();

        //初始化过滤链
        initFilterChain();


        //初始化请求分发器
        CommonServerCache.SERVER_CHANNEL_DISPATCHER.init(CommonServerCache.SERVER_CONFIG.getServerQueueSize(), CommonServerCache.SERVER_CONFIG.getServerBizThreadNums());
        CommonServerCache.SERVER_CHANNEL_DISPATCHER.startDataConsume();
    }

    public void initServerConfig() {
        CommonServerCache.SERVER_CONFIG = PropertiesBootstrap.loadServerConfigFromLocal();
    }

    private void initNioEventLoopGroup() throws InterruptedException {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.option(ChannelOption.TCP_NODELAY, IS_NAGLE);
        bootstrap.option(ChannelOption.SO_BACKLOG, QUEUE_SIZE);
        bootstrap.option(ChannelOption.SO_SNDBUF, SND_BUF_SIZE)
                .option(ChannelOption.SO_RCVBUF, RCV_BUF_SIZE)
                .option(ChannelOption.SO_KEEPALIVE, IS_KEEPALIVE);

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
        bootstrap.bind(CommonServerCache.SERVER_CONFIG.getPort()).sync();
    }


    private void initListener() {
        RpcListenerLoader rpcListenerLoader = new RpcListenerLoader();
        rpcListenerLoader.init();
    }

    private void initSerialize() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String serverSerialize = CommonServerCache.SERVER_CONFIG.getServerSerialize();
        CommonClientCache.EXTENSION_LOADER.loadExtension(SerializeFactory.class);
        LinkedHashMap<String, Class<?>> serializeMap = ExtensionLoader.EXTENSION_LOADER_CLASS_CACHE.get(SerializeFactory.class.getName());
        Class<?> serializeClass = serializeMap.get(serverSerialize);
        if (serializeClass == null) {
            throw new RuntimeException("no match serializeClass for " + serverSerialize);
        }
        CommonServerCache.SERVER_SERIALIZE_FACTORY = (SerializeFactory) serializeClass.newInstance();
    }

    private void initFilterChain() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
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
                //TODO SPI机制
                CommonServerCache.REGISTER_SERVICE = new ZookeeperRegister("152.136.150.223:2181");
            } catch (Exception e) {
                throw new RuntimeException("registryServiceType unKnow,error is ", e);
            }
        }
    }

}
