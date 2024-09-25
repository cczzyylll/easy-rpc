package rpc.config;

import lombok.Data;

/**
 * 客户端配置类
 */
@Data
public class ClientConfig {
    private String registerAdd;
    private String registerType;
    private String applicationName;
    private String proxyType;
    private String routerStrategy;
    private String clientSerialize;
}
