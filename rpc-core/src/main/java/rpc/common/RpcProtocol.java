package rpc.common;

import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;

import static rpc.common.constants.RpcConstants.MAGIC_NUMBER;

/**
 * 传输协议，解决网络中拆包和粘包问题
 */
@Data
public class RpcProtocol implements Serializable {
    private static final long serialVersionUID = 2669293150219020249L;
    /**
     * 魔法数
     */
    private short magicNumber = MAGIC_NUMBER;
    /**
     * 传输核心数据的长度
     */
    private int contentLength;
    /**
     * 核心数据
     */
    private byte[] content;
    public RpcProtocol(byte[] content){
        this.contentLength=content.length;
        this.content=content;
    }
    @Override
    public String toString() {
        return "RpcProtocol{" +
                "contentLength=" + contentLength +
                ", content=" + Arrays.toString(content) +
                '}';
    }
}
