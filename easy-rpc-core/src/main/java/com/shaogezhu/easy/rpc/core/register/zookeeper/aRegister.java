package com.shaogezhu.easy.rpc.core.register.zookeeper;

import com.shaogezhu.easy.rpc.core.register.RegisterInfo;
import com.shaogezhu.easy.rpc.core.register.RegisterServer;
import org.apache.zookeeper.Watcher;

public class aRegister implements RegisterServer {
    private ZookeeperClient client;

    public aRegister(String zkAddress) {
        client = new ZookeeperClient(zkAddress);
    }

    public aRegister(String address, int baseSleepTimes, int maxRetries) {
        client = new ZookeeperClient(address, baseSleepTimes, maxRetries);
    }

    @Override
    public void register(RegisterInfo registerInfo) {
        String path = registerInfo.buildProviderPath();
        if (client.existNode(path)) {
            client.deleteNode(path);
        }
        String data = registerInfo.buildData();
        client.createTemporaryNode(path, data);
    }

    @Override
    public void unRegister(RegisterInfo registerInfo) {
        String path = registerInfo.buildProviderPath();
        if (client.existNode(path)) {
            client.deleteNode(path);
        }
    }

    @Override
    public void subScribe(RegisterInfo registerInfo, Watcher watcher) {
        String path = registerInfo.buildProviderPath();
        if (client.existNode(path)){
            client.watchNode(path, watcher);
        }
    }

    @Override
    public void unSunScribe(RegisterInfo registerInfo) {
        String path = registerInfo.buildProviderPath();
        //TODO删除Watcher
    }
}
