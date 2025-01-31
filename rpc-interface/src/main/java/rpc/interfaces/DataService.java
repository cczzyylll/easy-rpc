package rpc.interfaces;

import java.util.List;

public interface DataService {

    /**
     * 发送数据
     */
    String sendData(String body);

    /**
     * 获取数据
     */
    List<String> getList();


    /**
     * 异常测试方法
     */
    void testError();

    /**
     * 异常测试方法
     */
    String testErrorV2();
}
