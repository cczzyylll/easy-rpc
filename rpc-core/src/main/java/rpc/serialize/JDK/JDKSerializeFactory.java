package rpc.serialize.JDK;

import rpc.serialize.SerializeFactory;

import java.io.*;

public class JDKSerializeFactory implements SerializeFactory {
    @Override
    public <T> byte[] serialize(T t){
        byte[] data=null;
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(os);
            output.writeObject(t);
            output.flush();
            output.close();
            data = os.toByteArray();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return data;

    }
    public <T> T deserialize(byte[] data,Class<T> clazz){
        ByteArrayInputStream is = new ByteArrayInputStream(data);
        try {
            ObjectInputStream input = new ObjectInputStream(is);
            Object result = input.readObject();
            return ((T) result);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
