package rpc.server;

import io.netty.channel.*;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@ChannelHandler.Sharable
public class MaxConnectionLimitHandler extends ChannelInboundHandlerAdapter {
    private final int maxConnectionNum;
    private final AtomicInteger numConnection=new AtomicInteger(0);
    private final Set<Channel> childChannel= Collections.newSetFromMap(new ConcurrentHashMap<>());
    public MaxConnectionLimitHandler(int maxConnectionNum){
        this.maxConnectionNum=maxConnectionNum;
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx,Object msg) throws Exception {
        Channel channel=(Channel) msg;
        int conn=numConnection.incrementAndGet();
        if(conn>0&&conn<=maxConnectionNum){
            this.childChannel.add(channel);
            channel.closeFuture().addListener(future -> {
                childChannel.remove(channel);
                numConnection.decrementAndGet();
            });
            super.channelRead(ctx,msg);
        }
        else {
//            numConnection.decrementAndGet();
//            //避免产生大量的time_wait连接
//            channel.config().setOption(ChannelOption.SO_LINGER, 0);
//            channel.unsafe().closeForcibly();
//            numDroppedConnections.increment();
//            //这里加入一道cas可以减少一些并发请求的压力,定期地执行一些日志打印
//            if (loggingScheduled.compareAndSet(false, true)) {
//                ctx.executor().schedule(this::writeNumDroppedConnectionLog, 1, TimeUnit.SECONDS);
//            }
        }
    }
}
