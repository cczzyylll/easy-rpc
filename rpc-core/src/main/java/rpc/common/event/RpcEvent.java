package rpc.common.event;

/**
 * 抽象事件
 */

public interface RpcEvent {
    Object getData();
    RpcEvent setData(Object data);
}
