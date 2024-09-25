package rpc.server;

import lombok.Data;

/**
 * 注册的接口服务
 */
@Data
public class ServiceWrapper {
    private Object serviceBean;
    private String group = "default";
    private String serviceToken = "";
    private Integer limit = -1;
    private Integer weight = 100;

    public ServiceWrapper(Object serviceBean) {
        this.serviceBean = serviceBean;
    }

}
