package rpc.core.register.zookeeper;

import rpc.core.register.RegisterInfo;
import rpc.core.register.RegisterService;
import org.apache.zookeeper.Watcher;

import java.util.List;

public class ZRigister implements RegisterService {
    private ZookeeperClient client;

    public ZRigister(String zkAddress) {
        client = new ZookeeperClient(zkAddress);
    }

    public ZRigister(String address, int baseSleepTimes, int maxRetries) {
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
    public void subScribe(String path, Watcher watcher) {
        if (client.existNode(path)){
            client.watchNode(path, watcher);
        }
    }

    @Override
    public void unSunScribe(String path) {
        //TODO删除Watcher
    }

    @Override
    public String getNode(String path) {
        return client.getNode(path);
    }

    @Override
    public List<String> getChildren(String path) {
        return client.getChildren(path);
    }
}
