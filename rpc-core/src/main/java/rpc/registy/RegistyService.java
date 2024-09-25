package rpc.registy;

public interface RegistyService {
    //注册服务
    void register(URL url);
    //下线服务
    void unRegister(URL url);
    //订阅服务
    void subscribe(URL url);
    //取消订阅
    void unSubscribe(URL url);
}
