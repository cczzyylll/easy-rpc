package rpc.core.register;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterInfo {
    public final static String ROOT = "easy-rpc";
    public final static String PROVIDER = "provider";
    public final static String CONSUMER = "consumer";
    public final static String IP = "ip";
    public final static String PORT = "port";
    public final static String GROUP = "group";
    public final static String WEIGHT = "weight";

    private String application;
    private String serviceName;
    private Map<String, String> parameters = new HashMap<>();

    public String buildData() {
        return JSON.toJSONString(this);
    }

    public static RegisterInfo parseRegister(String data) {
        return JSON.parseObject(data, RegisterInfo.class);
    }

    public String buildProviderPath() {
        return buildAbsolutePath(PROVIDER);
    }

    public String buildConsumerPath() {
        return buildAbsolutePath(CONSUMER);
    }


    private String buildAbsolutePath(String role) {
        return "/" + ROOT + "/" + application + "/" + serviceName + "/" + role + "/" + buildPath();
    }

    private String buildPath() {
        return getIp() + ":" + getPort();
    }

    private String getIp() {
        return parameters.getOrDefault(IP, null);
    }

    private String getPort() {
        return parameters.getOrDefault(PORT, null);
    }

    public String buildParentProviderPath() {
        return "/" + ROOT + "/" + application + "/" + serviceName + "/" + PROVIDER + "/";
    }

}
