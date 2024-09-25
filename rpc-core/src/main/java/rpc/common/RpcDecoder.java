package rpc.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import static rpc.common.constants.RpcConstants.MAGIC_NUMBER;

public class RpcDecoder extends ByteToMessageDecoder{
    public final static int BASE_LENGTH = 2 + 4;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) {
        if (byteBuf.readableBytes() >= BASE_LENGTH) {
            /**
             * 魔数开头
             */
            if(byteBuf.readShort()!=MAGIC_NUMBER){
                ctx.close();
                return;
            }
            int length=byteBuf.readInt();
            /**
             * 可读区域的大小小于数据长度，说明数据不完整
             */
            if(byteBuf.readableBytes()<length){
                ctx.close();
                return;
            }
            byte[]data=new byte[length];
            byteBuf.readBytes(data);
            RpcProtocol rpcProtocol=new RpcProtocol(data);
            out.add(rpcProtocol);
        }


    }
}
