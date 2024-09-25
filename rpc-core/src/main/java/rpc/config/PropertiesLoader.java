package rpc.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 配置加载器
 */
public class PropertiesLoader {
    private static Properties properties;

    private static final String DEFAULT_PROPERTIES_FILE = "rpc.properties";
    public static void loadProperties() throws IOException {
        if(properties!=null){
            return;
        }
        properties=new Properties();
        properties.load(PropertiesLoader.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE));
    }
    public static String getPropertiesStr(String key){
        if(properties==null){
            return null;
        }
        if(properties.containsKey(key)){
            return properties.getProperty(key);
        }
        return null;
    }

    public static String getPropertiesStrOrDefault(String key,String defaultValue){
        String value=getPropertiesStr(key);
        return value==null?defaultValue:value;
    }
    public static Integer getPropertiesInteger(String key){
        if (properties==null){
            return null;
        }
        if(properties.containsKey(key)){
            return Integer.valueOf(properties.getProperty(key));
        }
        return null;
    }

    public static Integer getPropertiesIntegerDefault(String key,Integer defaultValue){
        if(properties==null){
            return defaultValue;
        }
        String valStr=properties.getProperty(key);
        return valStr==null?defaultValue:Integer.valueOf(valStr);
    }

}
