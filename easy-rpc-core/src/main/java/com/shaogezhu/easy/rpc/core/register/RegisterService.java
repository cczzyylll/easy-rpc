package com.shaogezhu.easy.rpc.core.register;

import org.apache.zookeeper.Watcher;

import java.util.List;

public interface RegisterService {
    void register(RegisterInfo registerInfo);

    void unRegister(RegisterInfo registerInfo);

    void subScribe(String path, Watcher watcher);

    void unSunScribe(String path);

    String getNode(String path);

    List<String> getChildren(String path);

}
