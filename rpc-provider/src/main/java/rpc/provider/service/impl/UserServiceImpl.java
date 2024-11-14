package rpc.provider.service.impl;

import rpc.interfaces.UserService;
import rpc.spring.boot.starter.common.RpcService;

@RpcService
public class UserServiceImpl implements UserService {

    @Override
    public void test() {
        System.out.println("UserServiceImpl : test");
    }
}
