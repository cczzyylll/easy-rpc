package rpc.server;

import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import rpc.common.RpcProtocol;

@Data
public class ServerChannelReadData {
private RpcProtocol rpcProtocol;
private ChannelHandlerContext ctx;
}
