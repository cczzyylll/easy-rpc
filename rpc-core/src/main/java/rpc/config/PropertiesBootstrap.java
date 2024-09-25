package rpc.config;

import java.io.IOException;

import static rpc.common.constants.RpcConstants.*;

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
    public static final String SERVER_CORE_THREAD_POOL="rpc.server.core.thread.pool";
    public static final String SERVER_MAX_THREAD_POOL="rpc.server.max.thread.pool";
    public static final String SERVER_CAPACITY_THREAD_POOL="rpc.server.capacity.thread.pool";
    public static final String SERVER_MAX_CONNECTION = "rpc.server.max.connection";
    public static final String SERVER_MAX_DATA_SIZE = "rpc.server.max.data.size";
    public static final String CLIENT_MAX_DATA_SIZE = "rpc.client.max.data.size";
    public static ServerConfig loadServerConfigFromLocal() {
        try {
            PropertiesLoader.loadProperties();
        } catch (IOException e) {
            throw new RuntimeException("loadServerConfigFromLocal fail,e is {}", e);
        }
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setPort(PropertiesLoader.getPropertiesInteger(SERVER_PORT));
        serverConfig.setApplicationName(PropertiesLoader.getPropertiesStr(APPLICATION_NAME));
        serverConfig.setRegisterAddr(PropertiesLoader.getPropertiesStr(REGISTER_ADDRESS));
        serverConfig.setRegisterType(PropertiesLoader.getPropertiesStr(REGISTER_TYPE));
        serverConfig.setServerSerialize(PropertiesLoader.getPropertiesStrOrDefault(SERVER_SERIALIZE_TYPE, JDK_SERIALIZE_TYPE));
        serverConfig.setCoreThreadPool(PropertiesLoader.getPropertiesIntegerDefault(SERVER_CORE_THREAD_POOL, DEFAULT_CORE_THREAD_NUMS));
        serverConfig.setMaxThreadPool(PropertiesLoader.getPropertiesIntegerDefault(SERVER_MAX_THREAD_POOL, DEFAULT_MAX_THREAD_NUMS));
        serverConfig.setCapacityThreadPool(PropertiesLoader.getPropertiesIntegerDefault(SERVER_CAPACITY_THREAD_POOL,DEFAULT_CAPACITY_THREAD_POOL));
        serverConfig.setMaxConnections(PropertiesLoader.getPropertiesIntegerDefault(SERVER_MAX_CONNECTION, DEFAULT_MAX_CONNECTION_NUMS));
        serverConfig.setMaxServerRequestData(PropertiesLoader.getPropertiesIntegerDefault(SERVER_MAX_DATA_SIZE, SERVER_DEFAULT_MSG_LENGTH));
        return serverConfig;
    }
    public static ClientConfig loadClientConfigFromLocal(){
        try {
            PropertiesLoader.loadProperties();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        ClientConfig clientConfig=new ClientConfig();
        clientConfig.setClientSerialize(PropertiesLoader.getPropertiesStr(CLIENT_SERIALIZE_TYPE));
        clientConfig.setProxyType(PropertiesLoader.getPropertiesStr(PROXY_TYPE));
        clientConfig.setRouterStrategy(PropertiesLoader.getPropertiesStr(ROUTER_TYPE));
        clientConfig.setRegisterAdd(PropertiesLoader.getPropertiesStr(REGISTER_ADDRESS));
        clientConfig.setRegisterType(PropertiesLoader.getPropertiesStr(REGISTER_TYPE));
        clientConfig.setApplicationName(PropertiesLoader.getPropertiesStr(APPLICATION_NAME));
        clientConfig.setClientSerialize(PropertiesLoader.getPropertiesStr(CLIENT_SERIALIZE_TYPE));
        return clientConfig;
    }

}
