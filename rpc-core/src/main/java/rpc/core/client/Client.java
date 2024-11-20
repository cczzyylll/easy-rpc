package rpc.core.client;

import com.alibaba.fastjson.JSON;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rpc.core.common.RpcDecoder;
import rpc.core.common.RpcEncoder;
import rpc.core.common.RpcInvocation;
import rpc.core.common.RpcProtocol;
import rpc.core.common.cache.CommonServerCache;
import rpc.core.common.config.PropertiesBootstrap;
import rpc.core.common.event.RpcListenerLoader;
import rpc.core.filter.ClientFilter;
import rpc.core.filter.client.ClientFilterChain;
import rpc.core.proxy.ProxyFactory;
import rpc.core.register.AbstractRegister;
import rpc.core.register.RegisterInfo;
import rpc.core.register.RegisterService;
import rpc.core.register.URL;
import rpc.core.register.zookeeper.LoopWatcher;
import rpc.core.register.zookeeper.ZRigister;
import rpc.core.router.Router;
import rpc.core.serialize.SerializeFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import rpc.core.common.cache.CommonClientCache;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static rpc.core.common.constants.RpcConstants.DEFAULT_DECODE_CHAR;
import static rpc.core.spi.ExtensionLoader.EXTENSION_LOADER_CLASS_CACHE;

/**
 * @Author peng
 * @Date 2023/2/23 22:48
 */
public class Client {
    private static final Logger logger = LogManager.getLogger(Client.class);


    private AbstractRegister abstractRegister;

    private final Bootstrap bootstrap = new Bootstrap();

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public void initClient() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        initRegister();
        NioEventLoopGroup clientGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(clientGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                //初始化管道，包含了编解码器和客户端响应类
                ByteBuf delimiter = Unpooled.copiedBuffer(DEFAULT_DECODE_CHAR.getBytes());
                ch.pipeline().addLast(new DelimiterBasedFrameDecoder(CommonClientCache.CLIENT_CONFIG.getMaxServerRespDataSize(), delimiter));
                ch.pipeline().addLast(new RpcEncoder());
                ch.pipeline().addLast(new RpcDecoder());
                ch.pipeline().addLast(new ClientHandler());
            }
        });

        //初始化连接器
        ConnectionHandler.setBootstrap(bootstrap);

        //初始化监听器
//        RpcListenerLoader rpcListenerLoader = new RpcListenerLoader();
//        rpcListenerLoader.init();

        //初始化路由策略
//        String routerStrategy = CommonClientCache.CLIENT_CONFIG.getRouterStrategy();
//        CommonClientCache.EXTENSION_LOADER.loadExtension(Router.class);
//        LinkedHashMap<String, Class<?>> routerMap = EXTENSION_LOADER_CLASS_CACHE.get(Router.class.getName());
//        Class<?> routerClass = routerMap.get(routerStrategy);
//        if (routerClass == null) {
//            throw new RuntimeException("no match routerStrategyClass for " + routerStrategy);
//        }
//        CommonClientCache.ROUTER = (Router) routerClass.newInstance();
        initSerializedFactory();
//
//        //初始化过滤链
//        ClientFilterChain clientFilterChain = new ClientFilterChain();
//        CommonClientCache.EXTENSION_LOADER.loadExtension(ClientFilter.class);
//        LinkedHashMap<String, Class<?>> filterChainMap = EXTENSION_LOADER_CLASS_CACHE.get(ClientFilter.class.getName());
//        for (Map.Entry<String, Class<?>> filterChainEntry : filterChainMap.entrySet()) {
//            String filterChainKey = filterChainEntry.getKey();
//            Class<?> filterChainImpl = filterChainEntry.getValue();
//            if (filterChainImpl == null) {
//                throw new RuntimeException("no match filterChainImpl for " + filterChainKey);
//            }
//            clientFilterChain.addClientFilter((ClientFilter) filterChainImpl.newInstance());
//        }
//        CommonClientCache.CLIENT_FILTER_CHAIN = clientFilterChain;
//
        initProxyFactory();
    }

    public void loadClientConfig() {
        CommonClientCache.CLIENT_CONFIG = PropertiesBootstrap.loadClientConfigFromLocal();
    }

    public void doSubscribeService(Class<?> serviceBean) {
        RegisterInfo registerInfo = RegisterInfo.builder()
                .application(CommonClientCache.CLIENT_CONFIG.getApplicationName())
                .serviceName(serviceBean.getName())
                .build();
        String path = registerInfo.buildParentProviderPath();
        List<String> childNodeNames = CommonClientCache.REGISTER_SERVICE.getChildren(path);
            List<String> childDatas = childNodeNames
                    .stream().map(childPath -> CommonClientCache.REGISTER_SERVICE.getNode(path + "/" + childPath))
                            .collect(Collectors.toList());
        List<RegisterInfo> registerInfoList = childDatas
                .stream()
                .map(RegisterInfo::parseRegister)
                .collect(Collectors.toList());
        registerInfoList.forEach(info -> {
            CommonClientCache.REGISTER_SERVICE.subScribe(info.buildProviderPath(), new LoopWatcher());
            CommonClientCache.PROVIDER_INFO_MAP.put(info.buildProviderPath(), info);
        });
    }

    public void doConnectServer() {
        for (URL providerUrl : CommonClientCache.SUBSCRIBE_SERVICE_LIST) {
            List<String> providerIps = abstractRegister.getProviderIps(providerUrl.getServiceName());
            for (String providerIp : providerIps) {
                try {
                    ConnectionHandler.connect(providerUrl.getServiceName(), providerIp);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            URL url = new URL();
            url.setServiceName(providerUrl.getServiceName());
            url.addParameter("providerIps", JSON.toJSONString(providerIps));
            //客户端在此新增一个订阅的功能
            abstractRegister.doAfterSubscribe(url);
        }
    }

    public void startClient() {
        Thread asyncSendJob = new Thread(new AsyncSendJob(), "ClientAsyncSendJobThread");
        asyncSendJob.start();
    }

    static class AsyncSendJob implements Runnable {

        public AsyncSendJob() { }

        @Override
        public void run() {
            while (true) {
                try {
                    RpcInvocation data = CommonClientCache.SEND_QUEUE.take();
                    byte[] serialize = CommonClientCache.CLIENT_SERIALIZE_FACTORY.serialize(data);
                    RpcProtocol rpcProtocol = new RpcProtocol(serialize);
                    ChannelFuture channelFuture = ConnectionHandler.getChannelFuture(data);
                    channelFuture.channel().writeAndFlush(rpcProtocol);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initRegister() {
        if (CommonClientCache.REGISTER_SERVICE == null) {
            try {
                CommonClientCache.REGISTER_SERVICE = new ZRigister(CommonClientCache.CLIENT_CONFIG.getRegisterAddr());
            } catch (Exception e) {
                throw new RuntimeException("registryServiceType unKnow,error is ", e);
            }
        }
    }

    private void initProxyFactory() throws InstantiationException, IllegalAccessException, IOException, ClassNotFoundException {
        String proxyType = CommonClientCache.CLIENT_CONFIG.getProxyType();
        CommonClientCache.EXTENSION_LOADER.loadExtension(ProxyFactory.class);
        LinkedHashMap<String, Class<?>> proxyTypeMap = EXTENSION_LOADER_CLASS_CACHE.get(ProxyFactory.class.getName());
        Class<?> proxyTypeClass = proxyTypeMap.get(proxyType);
        if (proxyTypeClass == null) {
            throw new RuntimeException("no match proxyTypeClass for " + proxyType);
        }
        CommonClientCache.PROXY_FACTORY = (ProxyFactory) proxyTypeClass.newInstance();
    }

    private void initSerializedFactory() throws InstantiationException, IllegalAccessException, IOException, ClassNotFoundException {
        String serialize = CommonClientCache.CLIENT_CONFIG.getSerialize();
        CommonClientCache.EXTENSION_LOADER.loadExtension(SerializeFactory.class);
        LinkedHashMap<String, Class<?>> serializeMap = EXTENSION_LOADER_CLASS_CACHE.get(SerializeFactory.class.getName());
        Class<?> serializeClass = serializeMap.get(serialize);
        if (serializeClass == null) {
            throw new RuntimeException("no match serialize for " + serialize);
        }
        CommonClientCache.CLIENT_SERIALIZE_FACTORY = (SerializeFactory) serializeClass.newInstance();
    }
}
