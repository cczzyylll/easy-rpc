package rpc.registy.zookeeper;

import lombok.Data;
import org.apache.zookeeper.Watcher;

import java.util.List;

@Data
public abstract class AbstractZookeeperClient {
    /**
     * 注册地址:ip:port
     */
    private String zkAddress;
    private int baseSleepTimes;
    private int maxRetries;

    public AbstractZookeeperClient(String zkAddress) {
        this.zkAddress = zkAddress;
        this.baseSleepTimes = 1000;
        this.maxRetries = 3;
    }

    public AbstractZookeeperClient(String zkAddress, Integer baseSleepTimes, Integer maxRetries) {
        this.zkAddress = zkAddress;
        if (baseSleepTimes == null) {
            baseSleepTimes = 1000;
        } else {
            this.baseSleepTimes = baseSleepTimes;
        }
        if (maxRetries == null) {
            maxRetries = 3;
        } else {
            this.maxRetries = maxRetries;
        }
    }
    public abstract void updateNodeData(String path,String data);
    public abstract Object getClient();
    public abstract String getNodeData(String path);
    public abstract List<String> getChildrenData(String path);
    public abstract void createPersistentData(String path,String data);
    public abstract void createPersistentWithSeqData(String path,String data);
    public abstract void createTemporaryData(String path,String data);
    public abstract void createTemporarySeqData(String path,String data);
    public abstract void setTemporaryData(String path,String data);
    public abstract void destroy();
    public abstract  List<String > listNode(String path);
    public abstract void deleteNode(String  path);
    public abstract boolean existNode(String path);
    public abstract void watchNodeData(String path, Watcher watcher);
    public abstract void watchChildNodeData(String path,Watcher watcher);
}
