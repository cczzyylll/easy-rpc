package rpc.core.router;

import rpc.core.common.ChannelFutureWrapper;
import rpc.core.register.URL;

/**
 * @Author peng
 * @Date 2023/3/3
 * @description: 路由接口
 */
public interface Router {

    /**
     * 刷新路由数组
     *
     * @param selector
     */
    void refreshRouterArr(Selector selector);

    /**
     * 获取到请求的连接通道
     *
     * @param channelFutureWrappers
     * @return
     */
    ChannelFutureWrapper select(ChannelFutureWrapper[] channelFutureWrappers);

    /**
     * 更新权重信息
     *
     * @param url
     */
    void updateWeight(URL url);
}