package rpc.serialize;

/**
 * 序列化工厂，主要包括序列化与反序列化
 */
public interface SerializeFactory {
    <T> byte[] serialize(T t);
    <T> T deserialize(byte[] data,Class<T> clazz);
}
