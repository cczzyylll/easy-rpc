package rpc.core.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.common.value.qual.ArrayLen;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcReferenceWrapper<T> {
    private static String RETRY = "retry";
    private static String TIME_OUT = "timeOut";
    private static String ASYNC = "async";
    private static String URL = "url";
    private static String SERVICE_TOKEN = "serviceToken";
    private static String GROUP = "group";

    private Class<T> aimClass;
    private Map<String, Object> callSettings = new ConcurrentHashMap<>();

    public int getRetry() {
        return (int) callSettings.getOrDefault(RETRY, 0);
    }

    public void setRetry(int retry) {
        callSettings.put(RETRY, retry);
    }

    public void setTimeOut(int timeOut) {
        callSettings.put(TIME_OUT, timeOut);
    }

    public String getTimeOut() {
        return String.valueOf(callSettings.getOrDefault(TIME_OUT, ""));
    }

    public boolean isAsync() {
        return Boolean.parseBoolean(String.valueOf(callSettings.get(ASYNC)));
    }

    public void setAsync(boolean async) {
        this.callSettings.put(ASYNC, async);
    }

    public String getUrl() {
        return String.valueOf(callSettings.get(URL));
    }

    public void setUrl(String url) {
        callSettings.put(URL, url);
    }

    public String getServiceToken() {
        return String.valueOf(callSettings.get(SERVICE_TOKEN));
    }

    public void setServiceToken(String serviceToken) {
        callSettings.put(SERVICE_TOKEN, serviceToken);
    }

    public String getGroup() {
        return String.valueOf(callSettings.get(GROUP));
    }

    public void setGroup(String group) {
        callSettings.put(GROUP, group);
    }

}