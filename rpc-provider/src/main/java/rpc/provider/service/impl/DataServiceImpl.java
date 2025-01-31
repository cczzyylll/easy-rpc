package rpc.provider.service.impl;
import rpc.interfaces.DataService;
import rpc.spring.boot.starter.common.RpcService;
import java.util.ArrayList;
import java.util.List;

@RpcService(serviceToken = "data-token",group = "data-group",limit = 2)
public class DataServiceImpl implements DataService {

    @Override
    public String sendData(String body) {
        System.out.println("这里是服务提供者，body is " + body);
        return "success from server";
    }

    @Override
    public List<String> getList() {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("idea1");
        arrayList.add("idea2");
        arrayList.add("idea3");
        return arrayList;
    }

    @Override
    public void testError() {
        System.out.println(1 / 0);
    }

    @Override
    public String testErrorV2() {
        throw new RuntimeException("测试异常");
    }

}
