package rpc.router;

import lombok.Data;

/**
 *服务名称选择
 */
@Data
public class Selector {
    private String providerServiceName;
    public Selector(){}
    public Selector(String providerServiceName){
        this.providerServiceName=providerServiceName;
    }
}
