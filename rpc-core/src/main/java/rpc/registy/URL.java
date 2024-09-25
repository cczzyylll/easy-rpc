package rpc.registy;

import lombok.Data;
import org.apache.commons.io.output.AppendableOutputStream;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

//URL配置类
@Data
public class URL {
    //应用名字
    private String applicationName;
    //服务名字
    private String serviceName;
    //自定义拓展，主要有host port weight group
    private Map<String ,String > parameters=new HashMap<>();
    public static String buildProviderUrlStr(URL url){
        String host=url.getParameters().get("host");
        String port=url.getParameters().get("port");
        String group=url.getParameters().get("group");
        String weight=url.getParameters().get("weight");
        return new String(( host + ":" + port + ";" + System.currentTimeMillis() + ";" + weight + ";" + group).getBytes(), StandardCharsets.UTF_8);
    }
    public static String buildConsumerUrlStr(URL url){
        String host=url.getParameters().get("host");
        return new String(( host + ";" + System.currentTimeMillis()).getBytes(), StandardCharsets.UTF_8);
    }
    public void addParameters(String key,String value){
        this.parameters.putIfAbsent(key,value);
    }
}
