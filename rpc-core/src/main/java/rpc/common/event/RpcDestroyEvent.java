package rpc.common.event;

public class RpcDestroyEvent implements RpcEvent{
    private Object data;
    public RpcDestroyEvent(Object data){
        this.data=data;
    }
    @Override
    public Object getData(){
        return this.data;
    }
    @Override
    public RpcEvent setData(Object data){
        this.data=data;
        return this;
    }
}
