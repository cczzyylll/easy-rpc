package rpc.dispatcher;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import rpc.common.RpcInvocation;
import rpc.common.RpcProtocol;
import rpc.common.exception.RpcException;
import rpc.server.ServerChannelReadData;

import java.lang.reflect.Method;
import java.util.concurrent.*;

import static rpc.common.cache.CommonServerCache.*;

/**
 * 请求分发器,用于处理客户端传来的数据
 */
public class ServerChannelDispatcher {
    private ExecutorService executorService;
    /**
     * 存放传输过来的Rpc协议内容
     */
    private BlockingQueue<ServerChannelReadData> PRC_DATA_QUEUE;

    /**
     * 初始化线程池
     *
     * @param corePoolSize
     * @param maxPoolSize
     */
    public void init(int corePoolSize, int maxPoolSize, int capacitySize) {
        PRC_DATA_QUEUE = new ArrayBlockingQueue<>(capacitySize);
        executorService = new ThreadPoolExecutor(corePoolSize,
                maxPoolSize,
                3L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(capacitySize),
                new ThreadFactoryBuilder().setNameFormat("rpc-pool").build());
    }

    public void add(ServerChannelReadData serverChannelReadData) {
        PRC_DATA_QUEUE.add(serverChannelReadData);
    }

    class ServerJobCoreHandle implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    ServerChannelReadData serverChannelReadData = PRC_DATA_QUEUE.take();
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                RpcProtocol rpcProtocol=serverChannelReadData.getRpcProtocol();
                                RpcInvocation rpcInvocation=SERVER_SERIALIZE_FACTORY.deserialize(rpcProtocol.getContent(),RpcInvocation.class);
                                try {
                                    SERVER_BEFORE_FILTER_CHAIN.doFilter(rpcInvocation);
                                } catch (Exception e) {
                                    rpcInvocation.setE(e);
                                }
                                Object aimObject=PROVIDER_CLASS_MAP.get(rpcInvocation.getTargetServiceName());
                                Method[] methods=aimObject.getClass().getDeclaredMethods();
                                Object result = null;
                                for (Method method:methods){
                                    if(method.getName().equals(rpcInvocation.getTargetMethod())){
                                        if(method.getReturnType().equals(Void.TYPE)){
                                            try {
                                                method.invoke(aimObject,rpcInvocation.getArgs());
                                            }
                                            catch (Exception e){
                                                rpcInvocation.setE(e);
                                            }
                                        }
                                        else {
                                            try {
                                                result=method.invoke(aimObject,rpcInvocation.getArgs());
                                            }
                                            catch (Exception e){
                                                rpcInvocation.setE(e);
                                            }

                                        }
                                        break;
                                    }
                                }
                                rpcInvocation.setResponse(result);
                                RpcProtocol rpcProtocol1=new RpcProtocol(SERVER_SERIALIZE_FACTORY.serialize(rpcInvocation));
                                serverChannelReadData.getCtx().writeAndFlush(rpcProtocol1);
//                                SERVER_AFTER_FILTER_CHAIN.doFilter(rpcInvocation);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void startDataConsume() {
        Thread thread = new Thread(new ServerJobCoreHandle());
        thread.start();
    }
}
