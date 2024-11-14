package rpc.core.common.event.listener;

import rpc.core.common.ChannelFutureWrapper;
import rpc.core.common.event.RpcNodeUpdateEvent;
import rpc.core.register.URL;
import rpc.core.common.event.data.ProviderNodeInfo;
import rpc.core.common.cache.CommonClientCache;

import java.util.List;

/**
 * @Author peng
 * @Date 2023/3/3
 * @description:
 */
public class ProviderNodeUpdateListener implements RpcListener<RpcNodeUpdateEvent> {

    @Override
    public void callBack(Object t) {
        ProviderNodeInfo providerNodeInfo = ((ProviderNodeInfo) t);
        List<ChannelFutureWrapper> channelFutureWrappers =  CommonClientCache.CONNECT_MAP.get(providerNodeInfo.getServiceName());
        for (ChannelFutureWrapper channelFutureWrapper : channelFutureWrappers) {
            String address = channelFutureWrapper.getHost()+":"+channelFutureWrapper.getPort();
            if(address.equals(providerNodeInfo.getAddress())){
                //修改权重
                channelFutureWrapper.setWeight(providerNodeInfo.getWeight());
                URL url = new URL();
                url.setServiceName(providerNodeInfo.getServiceName());
                //更新权重
                CommonClientCache.ROUTER.updateWeight(url);
                break;
            }
        }
    }

}
