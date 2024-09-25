package rpc.common.event.listener;

import rpc.common.event.RpcDestroyEvent;
import rpc.registy.URL;

import java.util.Iterator;

import static rpc.common.cache.CommonServerCache.PROVIDER_URL_SET;
import static rpc.common.cache.CommonServerCache.REGISTRY_SERVICE;

public class ServiceDestroyListener implements RpcListener<RpcDestroyEvent> {
    @Override
    public void callback(Object object){
        Iterator<URL> urlIterator = PROVIDER_URL_SET.iterator();
        while (urlIterator.hasNext()) {
            URL url = urlIterator.next();
            urlIterator.remove();
            REGISTRY_SERVICE.unRegister(url);
        }
    }
}
