package rpc.core.register.zookeeper;

import rpc.core.client.Client;
import rpc.core.register.RegisterInfo;
import rpc.core.register.RegisterService;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import rpc.core.common.cache.CommonClientCache;

@AllArgsConstructor
public class LoopWatcher implements Watcher {
    private static final Logger logger = LogManager.getLogger(LoopWatcher.class);
    private static final RegisterService registerService = Client.registerService;

    @Override
    public void process(WatchedEvent watchedEvent) {
        Watcher.Event.KeeperState state = watchedEvent.getState();
        Watcher.Event.EventType type = watchedEvent.getType();
        String path = watchedEvent.getPath();
        if (state == Event.KeeperState.SyncConnected) {
            handleType(type, path);
            reSubScribe(path);
        }
        else {
            logger.error("state :" + state.name());
        }
    }

    private void handleType(Watcher.Event.EventType type, String path) {
        switch (type) {
            case NodeDataChanged:
                handleNodeDataChange(path);
                break;
            case NodeDeleted:
                handleNodeDeleted(path);
                break;
            default:
                break;
        }
    }

    private void handleNodeDataChange(String path) {
        String data = getData(path);
        RegisterInfo registerInfo = RegisterInfo.parseRegister(data);
        CommonClientCache.PROVIDER_INFO_MAP.put(path, registerInfo);
    }

    private String getData(String path) {
        return registerService.getNode(path);
    }

    private void handleNodeDeleted(String path) {
        CommonClientCache.PROVIDER_INFO_MAP.remove(path);
    }

    private void reSubScribe(String path) {
        registerService.subScribe(path, new LoopWatcher());
    }
}