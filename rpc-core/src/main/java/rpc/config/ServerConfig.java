package rpc.config;

import lombok.Data;

@Data
public class ServerConfig {
    /**
     * 服务器暴露端口
     */
    private Integer port;
    private String registerAddr;
    private String registerType;
    private String applicationName;
    /**
     * 服务器最大连接数
     */
    private Integer maxConnections;
    /**
     * 限制服务端最大所能接受的数据包体积
     */
    private Integer maxServerRequestData;
    /**
     * 服务端线程池核心线程数
     */
    private Integer coreThreadPool;
    /**
     * 客户端线程池最大线程数
     */
    private Integer maxThreadPool;
    /**
     * 客户端线程池任务队列人容量
     */
    private Integer capacityThreadPool;
    /**
     * 服务端序列化方式
     */
    private String serverSerialize;

}
