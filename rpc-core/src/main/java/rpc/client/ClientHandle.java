package rpc.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http2.InboundHttp2ToHttpAdapter;
import rpc.common.RpcInvocation;
import rpc.common.RpcProtocol;
import rpc.serialize.SerializeFactory;

import static rpc.common.cache.CommonCilentCache.CLIENT_SERIALIZE_FACTORY;
import static rpc.common.cache.CommonCilentCache.RESP_MAP;

public class ClientHandle extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx,Object msg) throws Exception{
        System.out.println("hhhhhhhhhhhhhhhhhhhhhh");
        RpcProtocol rpcProtocol=(RpcProtocol) msg;
        RpcInvocation rpcInvocation=CLIENT_SERIALIZE_FACTORY.deserialize(rpcProtocol.getContent(),RpcInvocation.class);
        System.out.println(rpcInvocation.getTargetServiceName());
        if(rpcInvocation.getE()!=null){
            rpcInvocation.getE().printStackTrace();
        }
        if(!RESP_MAP.containsKey(rpcInvocation.getUuid())){
            throw new IllegalArgumentException("server response is error!");
        }
        RESP_MAP.put(rpcInvocation.getUuid(),rpcInvocation);
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        Channel channel = ctx.channel();
        if(channel.isActive()){
            ctx.close();
        }
    }
}
