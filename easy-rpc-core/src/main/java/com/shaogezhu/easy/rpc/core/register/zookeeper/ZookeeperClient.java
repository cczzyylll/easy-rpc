package com.shaogezhu.easy.rpc.core.register.zookeeper;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;

import static com.shaogezhu.easy.rpc.core.register.zookeeper.ZookeeperClientConfig.DEFAULT_BASE_SLEEP_TIMES;
import static com.shaogezhu.easy.rpc.core.register.zookeeper.ZookeeperClientConfig.DEFAULT_MAX_RETRIES;

public class ZookeeperClient {
    private static final Logger logger = LogManager.getLogger(ZookeeperClient.class);
    private CuratorFramework client;
    private final ZookeeperClientConfig zookeeperClientConfig;

    public ZookeeperClient(String address) {
        this(address, DEFAULT_BASE_SLEEP_TIMES, DEFAULT_MAX_RETRIES);
    }

    public ZookeeperClient(String address, int baseSleepTimes, int maxRetries) {
        zookeeperClientConfig = new ZookeeperClientConfig(address, baseSleepTimes, maxRetries);
        initClient();
        client.start();
    }

    private void initClient() {
        client = CuratorFrameworkFactory.newClient(zookeeperClientConfig.getAddress()
                , new ExponentialBackoffRetry(zookeeperClientConfig.getBaseSleepTimes(), zookeeperClientConfig.getMaxRetries()));
    }

    public void updateNode(String address, String data) {
        try {
            client.setData().forPath(address, data.getBytes());
        } catch (Exception e) {
            logger.error("updateNode error", e);
        }
    }

    public String getNode(String address) {
        try {
            byte[] result = client.getData().forPath(address);
            if (result != null) {
                return new String(result);
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }

    public List<String> getChildNode(String path) {
        try {
            return client.getChildren().forPath(path);
        } catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }

    public void createPersistentNode(String path, String data) {
        try {
            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, data.getBytes());
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void createPersistentSeqNode(String path, String data) {
        try {
            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(path, data.getBytes());
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void createTemporarySeqNode(String path, String data) {
        try {
            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path, data.getBytes());
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void createTemporaryNode(String path, String data) {
        try {
            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path, data.getBytes());
        } catch (KeeperException.NoChildrenForEphemeralsException e) {
            try {
                client.setData().forPath(path, data.getBytes());
            } catch (Exception ex) {
                throw new IllegalStateException(ex.getMessage(), ex);
            }
        } catch (Exception ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    public void setTemporaryNode(String path, String data) {
        try {
            client.setData().forPath(path, data.getBytes());
        } catch (Exception ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    public void destroy() {
        client.close();
    }

    public List<String> listNode(String path) {
        try {
            return client.getChildren().forPath(path);
        } catch (Exception e) {
            logger.error(e);
        }
        return Collections.emptyList();
    }

    public boolean deleteNode(String path) {
        try {
            client.delete().forPath(path);
            return true;
        } catch (Exception e) {
            logger.error(e);
        }
        return false;
    }

    public boolean existNode(String path) {
        try {
            Stat stat = client.checkExists().forPath(path);
            return stat != null;
        } catch (Exception e) {
            logger.error(e);
        }
        return false;
    }

    public void watchNode(String path, Watcher watcher) {
        try {
            client.getData().usingWatcher(watcher).forPath(path);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void watchChildNode(String path, Watcher watcher) {
        try {
            client.getChildren().usingWatcher(watcher).forPath(path);
        } catch (Exception e) {
            logger.error(e);
        }
    }
}
