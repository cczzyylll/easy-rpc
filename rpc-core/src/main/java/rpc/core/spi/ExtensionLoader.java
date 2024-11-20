package rpc.core.spi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rpc.core.client.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ExtensionLoader {
    private static final Logger logger = LogManager.getLogger(ExtensionLoader.class);
    public static String EXTENSION_LOADER_DIR_PREFIX = "META-INF/easy-rpc/";
    public static Map<String, LinkedHashMap<String, Class<?>>> EXTENSION_LOADER_CLASS_CACHE = new ConcurrentHashMap<>();

    public void loadExtension(Class<?> clazz) throws IOException, ClassNotFoundException {
        if (clazz == null) {
            throw new IllegalArgumentException("class is null!");
        }
        String spiFilePath = EXTENSION_LOADER_DIR_PREFIX + clazz.getName();
        logger.info(spiFilePath);
        ClassLoader classLoader = this.getClass().getClassLoader();
        Enumeration<URL> enumeration = classLoader.getResources(spiFilePath);
        while (enumeration.hasMoreElements()) {
            URL url = enumeration.nextElement();
            InputStreamReader inputStreamReader = new InputStreamReader(url.openStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            LinkedHashMap<String, Class<?>> classMap = new LinkedHashMap<>();
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                String[] lineArr = line.split("=");
                String implClassName = lineArr[0];
                String interfaceName = lineArr[1];
                logger.info(implClassName);
                logger.info(interfaceName);
                classMap.put(implClassName, Class.forName(interfaceName));
            }
            if (EXTENSION_LOADER_CLASS_CACHE.containsKey(clazz.getName())) {
                EXTENSION_LOADER_CLASS_CACHE.get(clazz.getName()).putAll(classMap);
            } else {
                EXTENSION_LOADER_CLASS_CACHE.put(clazz.getName(), classMap);
            }
        }
    }

}
