package com.shaogezhu.easy.rpc.core.register.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

public class LoopWatcher implements Watcher {

    @Override
    public void process(WatchedEvent watchedEvent) {
        watchedEvent.getWrapper().getPath();
    }
}
