package rpc.registy.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import rpc.common.event.RpcEvent;
import rpc.common.event.data.URLChangeWrapper;
import rpc.registy.AbstractRegister;
import rpc.registy.RegistyService;
import rpc.registy.URL;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZookeeperRegister extends AbstractRegister implements RegistyService {
    private final AbstractZookeeperClient zkClient;
    private final String ROOT = "/czy";

    public ZookeeperRegister() {
        String registryAddr = "152.136.150.223:2181";
        this.zkClient = new CuratorZookeeperClient(registryAddr);
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
        zkClient.deleteNode(URL.buildProviderUrlStr(url));
        super.unRegister(url);
    }

    @Override
    public void subscribe(URL url) {
        if (!zkClient.existNode(ROOT)) {
            zkClient.createPersistentData(ROOT, "");
        }
        String urlStr = URL.buildConsumerUrlStr(url);
        if (zkClient.existNode(getConsumerPath(url))) {
            zkClient.deleteNode(getConsumerPath(url));
        }
        zkClient.createTemporaryData(getConsumerPath(url),urlStr);
//        zkClient.createTemporarySeqData(getConsumerPath(url), urlStr);
        super.subscribe(url);
    }
    @Override
    public void watch(URL url){
    String node=getProviderPath(url);
    wat
    }
    @Override
    public void unSubscribe(URL url) {
        this.zkClient.deleteNode(getConsumerPath(url));
        super.unSubscribe(url);
    }

    private String getProviderPath(URL url) {
        return ROOT + "/" + url.getServiceName() + "/provider/" + url.getParameters().get("host") + ":" + url.getParameters().get("port");
    }

    private String getConsumerPath(URL url) {
        return ROOT + "/" + url.getServiceName() + "/consumer/" + url.getParameters().get("host") ;
    }

    @Override
    public Map<String, String> getProviderNodeInfo(String serviceName) {
        List<String> nodeList = this.zkClient.getChildrenData(ROOT + "/" + serviceName + "/provider");
        Map<String, String> result = new HashMap<>(16);
        for (String node : nodeList) {
            String childData =this.zkClient.getNodeData(ROOT + "/" + serviceName + "/provider/"+node);
            result.put(node, childData);
        }
        return result;
    }

    public void watchChildNodeData(String provider) {
        zkClient.watchChildNodeData(provider, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                String path = watchedEvent.getPath();
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
}
