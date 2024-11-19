package rpc.core.client;

import rpc.core.common.RpcInvocation;
import rpc.core.common.RpcProtocol;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import rpc.core.common.cache.CommonClientCache;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        RpcProtocol rpcProtocol = (RpcProtocol) msg;
        RpcInvocation rpcInvocation = CommonClientCache.CLIENT_SERIALIZE_FACTORY.deserialize(rpcProtocol.getContent(), RpcInvocation.class);
        if (rpcInvocation.getE() != null) {
            rpcInvocation.getE().printStackTrace();
        }

        if(!CommonClientCache.RESP_MAP.containsKey(rpcInvocation.getUuid())){
            throw new IllegalArgumentException("server response is error!");
        }

        CommonClientCache.RESP_MAP.put(rpcInvocation.getUuid(),rpcInvocation);
        ReferenceCountUtil.release(msg);
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
