//package rpc.core.proxy.javassist;
//
//import rpc.core.client.RpcReferenceWrapper;
//import rpc.core.proxy.ProxyFactory;
//
///**
// * @Author peng
// * @Date 2023/2/25
// * @description:
// */
//public class JavassistProxyFactory implements ProxyFactory {
//
//    @Override
//    public <T> T getProxy(RpcReferenceWrapper<T> rpcReferenceWrapper) throws Throwable {
//        return (T) ProxyGenerator.newProxyInstance(Thread.currentThread().getContextClassLoader(),
//                rpcReferenceWrapper.getAimClass(), new JavassistInvocationHandler(rpcReferenceWrapper));
//    }
//}
