package com.shaogezhu.easy.rpc.core.register;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterInfo {
    private final static String ROOT = "easy-rpc";
    private final static String PROVIDER = "PROVIDER";
    private final static String CONSUMER = "consumer";
    private final static String IP = "ip";
    private final static String PORT = "port";
    private final static String GROUP = "group";
    private final static String WEIGHT = "weight";
    private String application;
    private String serviceName;
    private Map<String, String> parameters = new HashMap<>();

    public String buildData() {
        return "application" + ":" + application + "/" +
                "serviceName" + ":" + serviceName + "/" +
                "ip" + ":" + parameters.getOrDefault(IP, null) +
                "port" + ":" + parameters.getOrDefault(PORT, null) +
                "group" + ":" + parameters.getOrDefault(GROUP, null) +
                "weight" + ":" + parameters.getOrDefault(WEIGHT, null);
    }

    public static RegisterInfo parseRegister(String data) {
        RegisterInfo registerInfo = new RegisterInfo();
        String[] parts = data.split("/");
        for (String part : parts) {
            String[] keyValue = part.split(":");
            if (keyValue.length == 2) {
                String key = keyValue[0];
                String value = keyValue[1];
                switch (key) {
                    case "application":
                        registerInfo.setApplication(value);
                        break;
                    case "serviceName":
                        registerInfo.setServiceName(value);
                        break;
                    case "ip":
                        registerInfo.getParameters().put(IP, value);
                        break;
                    case "port":
                        registerInfo.getParameters().put(PORT, value);
                        break;
                    case "group":
                        registerInfo.getParameters().put(GROUP, value);
                        break;
                    case "weight":
                        registerInfo.getParameters().put(WEIGHT, value);
                        break;
                    default:
                        break;
                }
            }
        }
        return registerInfo;
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

}
