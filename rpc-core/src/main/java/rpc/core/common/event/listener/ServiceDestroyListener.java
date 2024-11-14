package rpc.core.common.event.listener;

import rpc.core.common.event.RpcDestroyEvent;
import rpc.core.register.URL;
import rpc.core.common.cache.CommonServerCache;

import java.util.Iterator;

/**
 * @Author peng
 * @Date 2023/3/3
 * @description:
 */
public class ServiceDestroyListener implements RpcListener<RpcDestroyEvent> {

    @Override
    public void callBack(Object t) {
        Iterator<URL> urlIterator = CommonServerCache.PROVIDER_URL_SET.iterator();
        while (urlIterator.hasNext()) {
            URL url = urlIterator.next();
            urlIterator.remove();
            CommonServerCache.REGISTRY_SERVICE.unRegister(url);
        }
    }
}
