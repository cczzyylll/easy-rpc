package rpc.core.common.config;

import rpc.core.common.constants.RpcConstants;

import java.io.IOException;

public class PropertiesBootstrap {

    private volatile boolean configIsReady;

    public static final String SERVER_PORT = "rpc.serverPort";
    public static final String REGISTER_ADDRESS = "rpc.registerAddr";
    public static final String REGISTER_TYPE = "rpc.registerType";
    public static final String APPLICATION_NAME = "rpc.applicationName";
    public static final String PROXY_TYPE = "rpc.proxyType";
    public static final String ROUTER_TYPE = "rpc.router";
    public static final String SERVER_SERIALIZE_TYPE = "rpc.serverSerialize";
    public static final String CLIENT_SERIALIZE_TYPE = "rpc.clientSerialize";
    public static final String CLIENT_DEFAULT_TIME_OUT = "rpc.client.default.timeout";
    public static final String SERVER_BIZ_THREAD_NUMS = "rpc.server.biz.thread.nums";
    public static final String SERVER_QUEUE_SIZE = "rpc.server.queue.size";
    public static final String SERVER_MAX_CONNECTION = "rpc.server.max.connection";
    public static final String SERVER_MAX_DATA_SIZE = "rpc.server.max.data.size";
    public static final String CLIENT_MAX_DATA_SIZE = "rpc.client.max.data.size";

    /**
     * 加载服务端专属配置
     */
    public static ServerConfig loadServerConfigFromLocal() {
        try {
            PropertiesLoader.loadConfiguration();
        } catch (IOException e) {
            throw new RuntimeException("loadServerConfigFromLocal fail,e is {}", e);
        }
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setPort(PropertiesLoader.getPropertiesInteger(SERVER_PORT));
        serverConfig.setApplicationName(PropertiesLoader.getPropertiesStr(APPLICATION_NAME));
        serverConfig.setRegisterAddr(PropertiesLoader.getPropertiesStr(REGISTER_ADDRESS));
        serverConfig.setRegisterType(PropertiesLoader.getPropertiesStr(REGISTER_TYPE));
        serverConfig.setServerSerialize(PropertiesLoader.getPropertiesStrDefault(SERVER_SERIALIZE_TYPE, RpcConstants.JDK_SERIALIZE_TYPE));
        serverConfig.setServerBizThreadNums(PropertiesLoader.getPropertiesIntegerDefault(SERVER_BIZ_THREAD_NUMS, RpcConstants.DEFAULT_THREAD_NUMS));
        serverConfig.setServerQueueSize(PropertiesLoader.getPropertiesIntegerDefault(SERVER_QUEUE_SIZE, RpcConstants.DEFAULT_QUEUE_SIZE));
        serverConfig.setMaxConnections(PropertiesLoader.getPropertiesIntegerDefault(SERVER_MAX_CONNECTION, RpcConstants.DEFAULT_MAX_CONNECTION_NUMS));
        serverConfig.setMaxServerRequestData(PropertiesLoader.getPropertiesIntegerDefault(SERVER_MAX_DATA_SIZE, RpcConstants.SERVER_DEFAULT_MSG_LENGTH));
        return serverConfig;
    }

    /**
     * 加载客户端专属配置
     */
    public static ClientConfig loadClientConfigFromLocal() {
        try {
            PropertiesLoader.loadConfiguration();
        } catch (IOException e) {
            throw new RuntimeException("loadClientConfigFromLocal fail,e is {}", e);
        }
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setApplicationName(PropertiesLoader.getPropertiesNotBlank(APPLICATION_NAME));
        clientConfig.setRegisterAddr(PropertiesLoader.getPropertiesNotBlank(REGISTER_ADDRESS));
        clientConfig.setRegisterType(PropertiesLoader.getPropertiesNotBlank(REGISTER_TYPE));
        clientConfig.setProxyType(PropertiesLoader.getPropertiesStrDefault(PROXY_TYPE, RpcConstants.JDK_PROXY_TYPE));
        clientConfig.setRouterStrategy(PropertiesLoader.getPropertiesStrDefault(ROUTER_TYPE, RpcConstants.RANDOM_ROUTER_TYPE));
        clientConfig.setSerialize(PropertiesLoader.getPropertiesStrDefault(CLIENT_SERIALIZE_TYPE, RpcConstants.JDK_SERIALIZE_TYPE));
        clientConfig.setTimeOut(PropertiesLoader.getPropertiesIntegerDefault(CLIENT_DEFAULT_TIME_OUT, RpcConstants.DEFAULT_TIMEOUT));
        clientConfig.setMaxServerRespDataSize(PropertiesLoader.getPropertiesIntegerDefault(CLIENT_MAX_DATA_SIZE, RpcConstants.CLIENT_DEFAULT_MSG_LENGTH));
        return clientConfig;
    }


}