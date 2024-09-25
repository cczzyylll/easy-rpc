package rpc.common.event.data;

import lombok.Data;
import rpc.registy.URL;

import java.util.List;
import java.util.Map;
@Data
public class URLChangeWrapper {
    private String serviceName;
    private List<URL> providerUrl;
    private Map<String,String> nodeDataUrl;
    @Override
    public String toString() {
        return "URLChangeWrapper{" +
                "serviceName='" + serviceName + '\'' +
                ", providerUrl=" + providerUrl +
                ", nodeDataUrl=" + nodeDataUrl +
                '}';
    }
}
