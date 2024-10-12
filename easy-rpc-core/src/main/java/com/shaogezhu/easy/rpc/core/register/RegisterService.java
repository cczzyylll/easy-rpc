package com.shaogezhu.easy.rpc.core.register;

import org.apache.zookeeper.Watcher;

public interface RegisterService {
    void register(RegisterInfo registerInfo);

    void unRegister(RegisterInfo registerInfo);

    void subScribe(String path, Watcher watcher);

    void unSunScribe(String path);

    String getNode(String path);
}
