package rpc.router;

import rpc.common.ChannelFutureWrapper;
import rpc.registy.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static rpc.common.cache.CommonCilentCache.CONNECT_MAP;
import static rpc.common.cache.CommonCilentCache.SERVICE_ROUTE_MAP;

public class RandomRouterImpl implements Router {
    @Override
    public void refreshRouterArr(Selector selector){
        //获取服务提供者的数目
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(selector.getProviderServiceName());
        ChannelFutureWrapper[] arr = new ChannelFutureWrapper[channelFutureWrappers.size()];
        //提前生成调用先后顺序的随机数组
        Integer[] result = createRandomIndex(arr.length);
        //生成对应服务集群的每台机器的调用顺序
        for (int i = 0; i < result.length; i++) {
            arr[i] = channelFutureWrappers.get(result[i]);
        }
        SERVICE_ROUTE_MAP.put(selector.getProviderServiceName(), arr);
        URL url=new URL();
        url.setServiceName(selector.getProviderServiceName());
//        //更新权重
//        ROUTER.updateWeight(url);
    }
    @Override
    public ChannelFutureWrapper select(ChannelFutureWrapper[] channelFutureWrappers){
        return channelFutureWrappers[0];
    }
    private Integer[] createRandomIndex(int len) {
        Random random = new Random();
        ArrayList<Integer> list = new ArrayList<>(len);
        int index = 0;
        while (index < len) {
            int num = random.nextInt(len);
            //如果不包含这个元素则赋值给集合数组
            if (!list.contains(num)) {
                list.add(index++, num);
            }
        }
        return list.toArray(new Integer[0]);
    }
}
