package rpc.common.event.listener;

/**
 * 监听器接口
 */
public interface RpcListener <T>{
    void callback(Object o);
}
