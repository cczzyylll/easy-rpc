package com.shaogezhu.easy.rpc.core.register;

import org.apache.zookeeper.Watcher;

public interface RegisterServer {
    void register(RegisterInfo registerInfo);

    void unRegister(RegisterInfo registerInfo);

    void subScribe(RegisterInfo registerInfo, Watcher watcher);

    void unSunScribe(RegisterInfo registerInfo);
}
