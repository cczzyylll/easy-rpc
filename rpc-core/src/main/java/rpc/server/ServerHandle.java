package rpc.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import rpc.common.RpcProtocol;

import static rpc.common.cache.CommonServerCache.SERVER_CHANNEL_DISPATCHER;

public class ServerHandle extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx,Object msg){
        ServerChannelReadData serverChannelReadData=new ServerChannelReadData();
        serverChannelReadData.setCtx(ctx);
        serverChannelReadData.setRpcProtocol((RpcProtocol) msg);
        System.out.println("asdsaddasasdads");
        //放入channel分发器
        SERVER_CHANNEL_DISPATCHER.add(serverChannelReadData);

        SERVER_CHANNEL_DISPATCHER.startDataConsume();
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause) throws Exception{
        cause.printStackTrace();
        Channel channel=ctx.channel();
        if(channel.isActive()){
            ctx.close();
        }
    }

}
