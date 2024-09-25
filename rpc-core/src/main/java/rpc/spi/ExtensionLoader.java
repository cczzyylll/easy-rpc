package rpc.spi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExtensionLoader {
    public static String EXTENSION_LOADER_DIR_PREFIX = "META-INF/config/";
    /**
     * key：interface
     * value:<type,implClass>
     */
    public static Map<String , LinkedHashMap<String,Class<?>>> EXTENSION_LOADER_CLASS_CACHE=new ConcurrentHashMap<>();
    public static void loadExtension(Class<?> clazz) throws IOException, ClassNotFoundException {
        if(clazz==null){
            throw new IllegalArgumentException("class is null!");
        }
        String spiFilePath=EXTENSION_LOADER_DIR_PREFIX+clazz.getName();
        System.out.println(spiFilePath);
        ClassLoader classLoader=ExtensionLoader.class.getClassLoader();
        Enumeration<URL> enumeration=classLoader.getResources(spiFilePath);
        while (enumeration.hasMoreElements()){
            URL url=enumeration.nextElement();
            InputStreamReader inputStreamReader=new InputStreamReader(url.openStream());
            BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
            String line;
            LinkedHashMap<String, Class<?>> classMap = new LinkedHashMap<>();
            while ((line = bufferedReader.readLine()) != null) {
                //如果配置中加入了#开头则表示忽略该类无需进行加载
                if (line.startsWith("#")) {
                    continue;
                }
                String[] lineArr = line.split("=");
                /**
                 * 配置文件中的格式：zooke
                 * eper=com.shaogezhu.easy.rpc.core.registy.zookeeper.ZookeeperRegister
                 * 因此前面是名字，后面是实现的类
                 */
                String type = lineArr[0];
                String implClassName = lineArr[1];
                classMap.put(type, Class.forName(implClassName));
            }
            if (EXTENSION_LOADER_CLASS_CACHE.containsKey(clazz.getName())) {
                //支持开发者自定义配置
                EXTENSION_LOADER_CLASS_CACHE.get(clazz.getName()).putAll(classMap);
            } else {
                EXTENSION_LOADER_CLASS_CACHE.put(clazz.getName(), classMap);
            }
        }
    }
}
