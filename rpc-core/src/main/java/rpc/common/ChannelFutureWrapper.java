package rpc.common;

import io.netty.channel.ChannelFuture;
import lombok.Data;

/**
 *
 */
@Data
public class ChannelFutureWrapper {
    private String ip;
    private int port;
//    private Integer weight;
//    private String group;
    private ChannelFuture channelFuture;
    public ChannelFutureWrapper(){}
    public ChannelFutureWrapper(String ip,int port){
        this.ip=ip;
        this.port=port;
    }
}
