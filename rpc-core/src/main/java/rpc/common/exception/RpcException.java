package rpc.common.exception;

import lombok.Data;
import rpc.common.RpcInvocation;
@Data
public class RpcException extends RuntimeException{
    private RpcInvocation rpcInvocation;
    public RpcException(RpcInvocation rpcInvocation){
        this.rpcInvocation=rpcInvocation;
    }
}
