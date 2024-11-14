package rpc.core.router;

import rpc.core.common.ChannelFutureWrapper;
import rpc.core.register.URL;
import rpc.core.common.cache.CommonClientCache;

import java.util.List;

/**
 * @Author peng
 * @Date 2023/3/3
 * @description:  * @Author peng
 */
public class RotateRouterImpl implements Router {


    @Override
    public void refreshRouterArr(Selector selector) {
        List<ChannelFutureWrapper> channelFutureWrappers = CommonClientCache.CONNECT_MAP.get(selector.getProviderServiceName());
        ChannelFutureWrapper[] arr = new ChannelFutureWrapper[channelFutureWrappers.size()];
        for (int i=0;i<channelFutureWrappers.size();i++) {
            arr[i]=channelFutureWrappers.get(i);
        }
        CommonClientCache.SERVICE_ROUTER_MAP.put(selector.getProviderServiceName(),arr);
    }

    @Override
    public ChannelFutureWrapper select(ChannelFutureWrapper[] channelFutureWrappers) {
        return CommonClientCache.CHANNEL_FUTURE_POLLING_REF.getChannelFutureWrapper(channelFutureWrappers);
    }

    @Override
    public void updateWeight(URL url) {

    }
}
