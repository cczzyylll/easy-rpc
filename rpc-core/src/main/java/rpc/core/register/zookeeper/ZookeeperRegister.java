package rpc.core.register.zookeeper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import rpc.core.common.event.RpcEvent;
import rpc.core.common.event.RpcListenerLoader;
import rpc.core.common.event.RpcNodeUpdateEvent;
import rpc.core.common.event.RpcUpdateEvent;
import rpc.core.common.event.data.ProviderNodeInfo;
import rpc.core.common.event.data.URLChangeWrapper;
import rpc.core.common.utils.CommonUtil;
import rpc.core.register.AbstractRegister;
import rpc.core.register.RegistryService;
import rpc.core.register.URL;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.core.common.cache.CommonClientCache;
import rpc.core.common.cache.CommonServerCache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author peng
 * @Date 2023/2/27
 * @description: 主要负责的功能是对Zookeeper完成服务注册，服务订阅，服务下线等相关实际操作
 */
public class ZookeeperRegister extends AbstractRegister implements RegistryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperRegister.class);

    private final AbstractZookeeperClient zkClient;

    private final String ROOT = "/easy-rpc";

    public ZookeeperRegister() {
        String registryAddr = CommonClientCache.CLIENT_CONFIG != null ? CommonClientCache.CLIENT_CONFIG.getRegisterAddr() : CommonServerCache.SERVER_CONFIG.getRegisterAddr();
        this.zkClient = new CuratorZookeeperClient(registryAddr);
    }

    private String getProviderPath(URL url) {
        return ROOT + "/" + url.getServiceName() + "/provider/" + url.getParameters().get("host") + ":" + url.getParameters().get("port");
    }

    private String getConsumerPath(URL url) {
        return ROOT + "/" + url.getServiceName() + "/consumer/" + url.getApplicationName() + ":" + url.getParameters().get("host") + ":";
    }

    @Override
    public List<String> getProviderIps(String serviceName) {
        return this.zkClient.getChildrenData(ROOT + "/" + serviceName + "/provider");
    }

    @Override
    public Map<String, String> getServiceWeightMap(String serviceName) {
        List<String> nodeDataList = this.zkClient.getChildrenData(ROOT + "/" + serviceName + "/provider");
        Map<String, String> result = new HashMap<>(16);
        for (String ipAndHost : nodeDataList) {
            String childData = this.zkClient.getNodeData(ROOT + "/" + serviceName + "/provider/" + ipAndHost);
            result.put(ipAndHost, childData);
        }
        return result;
    }

    @Override
    public void register(URL url) {
        if (!zkClient.existNode(ROOT)) {
            zkClient.createPersistentData(ROOT, "");
        }
        String urlStr = URL.buildProviderUrlStr(url);
        if (zkClient.existNode(getProviderPath(url))) {
            zkClient.deleteNode(getProviderPath(url));
        }
        zkClient.createTemporaryData(getProviderPath(url), urlStr);
        super.register(url);
    }

    @Override
    public void unRegister(URL url) {
        zkClient.deleteNode(getProviderPath(url));
        super.unRegister(url);
    }

    @Override
    public void subscribe(URL url) {
        if (!this.zkClient.existNode(ROOT)) {
            zkClient.createPersistentData(ROOT, "");
        }
        String urlStr = URL.buildConsumerUrlStr(url);
        if (zkClient.existNode(getConsumerPath(url))) {
            zkClient.deleteNode(getConsumerPath(url));
        }
        zkClient.createTemporarySeqData(getConsumerPath(url), urlStr);
        super.subscribe(url);
    }

    @Override
    public void doAfterSubscribe(URL url) {
        //监听是否有新的服务注册
        String newServerNodePath = ROOT + "/" + url.getServiceName() + "/provider";
        watchChildNodeData(newServerNodePath);
        //监听节点内部的数据变化
        String providerIpStrJson = url.getParameters().get("providerIps");
        List<String> providerIpList = JSON.parseObject(providerIpStrJson, new TypeReference<List<String>>(){});
        for (String providerIp : providerIpList) {
            this.watchNodeDataChange(newServerNodePath + "/" + providerIp);
        }
    }

    /**
     * 订阅服务子节点的数据变化（key的变化）
     */
    public void watchChildNodeData(String newServerNodePath) {
        zkClient.watchChildNodeData(newServerNodePath, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                String path = watchedEvent.getPath();
                LOGGER.info("[watchChildNodeData] 监听到zk节点下的" + path + "节点数据发生变更");
                List<String> childrenDataList = zkClient.getChildrenData(path);
                URLChangeWrapper urlChangeWrapper = new URLChangeWrapper();
                Map<String, String> nodeDetailInfoMap = new HashMap<>();
                for (String providerAddress : childrenDataList) {
                    String nodeDetailInfo = zkClient.getNodeData(path + "/" + providerAddress);
                    nodeDetailInfoMap.put(providerAddress, nodeDetailInfo);
                }
                urlChangeWrapper.setNodeDataUrl(nodeDetailInfoMap);
                urlChangeWrapper.setProviderUrl(childrenDataList);
                urlChangeWrapper.setServiceName(path.split("/")[2]);
                RpcEvent rpcEvent = new RpcUpdateEvent(urlChangeWrapper);
                RpcListenerLoader.sendEvent(rpcEvent);
                //收到回调之后在注册一次监听，这样能保证一直都收到消息
                watchChildNodeData(path);
            }
        });
    }


    /**
     * 订阅服务节点内部的数据变化（节点对应的内部数据的变化）
     */
    public void watchNodeDataChange(String newServerNodePath) {
        zkClient.watchNodeData(newServerNodePath, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                String path = watchedEvent.getPath();
                LOGGER.info("[watchNodeDataChange]收到子节点" + path + "数据变化");
                String nodeData = zkClient.getNodeData(path);
                if (CommonUtil.isEmpty(nodeData)) {
                    LOGGER.error("{} node data is null", path);
                }else {
                    ProviderNodeInfo providerNodeInfo = URL.buildUrlFromUrlStr(nodeData);
                    RpcEvent rpcEvent = new RpcNodeUpdateEvent(providerNodeInfo);
                    RpcListenerLoader.sendEvent(rpcEvent);
                }
                watchNodeDataChange(newServerNodePath);
            }
        });
    }

    @Override
    public void doBeforeSubscribe(URL url) {

    }

    @Override
    public void doUnSubscribe(URL url) {
        this.zkClient.deleteNode(getConsumerPath(url));
        super.doUnSubscribe(url);
    }

}
