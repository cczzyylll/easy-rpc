package org.example.serviceImpl;

import org.example.common.RpcService;
import org.example.interfaces.TestService;
@RpcService
public class TestServiceImpl implements TestService {
    @Override
    public String test(){
        System.out.println("服务器端的test");
        return "hello";
    }
}
