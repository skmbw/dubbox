package com.alibaba.dubbo.common.serialize.support.protostuff;

import com.alibaba.dubbo.common.compiler.support.ClassUtils;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import io.protostuff.MessageCollectionSchema;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.StringMapSchema;
import io.protostuff.runtime.RuntimeSchema;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * 基于ByteBuffer读取输入流。
 *
 * @author yinlei
 * @since 2017/10/18 12:57
 */
public class ProtoInput implements ObjectInput {

    private byte[] bytes;
    private ByteBuffer byteBuffer;

    public ProtoInput(InputStream inputStream) throws IOException {
        bytes = IOUtils.toByteArray(inputStream);
        byteBuffer = ByteBuffer.wrap(bytes);
    }

    @Override
    public boolean readBool() throws IOException {
        byteBuffer.get();
        return byteBuffer.get() != 0;
    }

    @Override
    public byte readByte() throws IOException {
        byteBuffer.get();
        return byteBuffer.get();
    }

    @Override
    public short readShort() throws IOException {
        byteBuffer.get();
        return byteBuffer.getShort();
    }

    @Override
    public int readInt() throws IOException {
        byteBuffer.get();
        return byteBuffer.getInt();
    }

    @Override
    public long readLong() throws IOException {
        byteBuffer.get();
        return byteBuffer.getInt();
    }

    @Override
    public float readFloat() throws IOException {
        byteBuffer.get();
        return byteBuffer.getFloat();
    }

    @Override
    public double readDouble() throws IOException {
        byteBuffer.get();
        return byteBuffer.getDouble();
    }

    @Override
    public String readUTF() throws IOException {
        byte type = byteBuffer.get();
        if (type != 12) { // 不是string类型
            return null;
        }

        return readString();
    }

    private String readString() throws IOException {
        int length = byteBuffer.getInt();
        if (length != 0) {
            byte[] data = new byte[length];
            byteBuffer.get(data);
            return new String(data);
        }
        return null;
    }

    @Override
    public byte[] readBytes() throws IOException {
        byte type = byteBuffer.get();
        if (type != 14) { // 不是byte[]
            return new byte[0];
        }
        return getBytes();
    }

    private byte[] getBytes() throws IOException {
        int length = byteBuffer.getInt();
        if (length != 0) {
            byte[] data = new byte[length];
            byteBuffer.get(data);
            return data;
        }
        return new byte[0];
    }

    @Override
    public Object readObject() throws IOException, ClassNotFoundException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        // 这个是心跳的
        if (bytes.length == 2 && bytes[0] == 13) {
            if (bytes[1] == 1) {
                return true;
            }
            return false;
        }

        byte type = byteBuffer.get();
        switch (type) {
            case 0:
                int totalLength = byteBuffer.getInt();
                int nameLength = byteBuffer.getInt();
                byte[] nameBytes = new byte[nameLength];
                String className = new String(nameBytes);
                Class clazz = ClassUtils.forName(className);
                Schema schema = RuntimeSchema.getSchema(clazz);

                Object object = newInstance(clazz);

                byte[] dataBytes = new byte[totalLength - nameLength - 9];
                byteBuffer.get(dataBytes);
                ProtostuffIOUtil.mergeFrom(dataBytes, object, schema);
                return object;
            case 1:
                totalLength = byteBuffer.getInt();
                nameLength = byteBuffer.getInt();
                nameBytes = new byte[nameLength];
                className = new String(nameBytes);
                clazz = ClassUtils.forName(className);
                schema = RuntimeSchema.getSchema(clazz);

                MessageCollectionSchema collectionSchema = new MessageCollectionSchema(schema);
                List list = new ArrayList();

                dataBytes = new byte[totalLength - nameLength - 9];
                byteBuffer.get(dataBytes);
                ProtostuffIOUtil.mergeFrom(dataBytes, list, collectionSchema);
                return list;
            case 2:
                totalLength = byteBuffer.getInt();
                nameLength = byteBuffer.getInt();
                nameBytes = new byte[nameLength];
                className = new String(nameBytes);
                clazz = ClassUtils.forName(className);
                schema = RuntimeSchema.getSchema(clazz);

                collectionSchema = new MessageCollectionSchema(schema);
                Set set = new HashSet();

                dataBytes = new byte[totalLength - nameLength - 9];
                byteBuffer.get(dataBytes);
                ProtostuffIOUtil.mergeFrom(dataBytes, set, collectionSchema);
                return set;
            case 3:
                totalLength = byteBuffer.getInt();
                nameLength = byteBuffer.getInt();
                nameBytes = new byte[nameLength];
                className = new String(nameBytes);
                clazz = ClassUtils.forName(className);
                schema = RuntimeSchema.getSchema(clazz);

                StringMapSchema stringSchema = new StringMapSchema(schema);
                Map map = new HashMap();

                dataBytes = new byte[totalLength - nameLength - 9];
                byteBuffer.get(dataBytes);
                ProtostuffIOUtil.mergeFrom(dataBytes, map, stringSchema);
                return map;
            case 4:
                return byteBuffer.getInt();
//                return readInt();
            case 5:
//                return readLong();
                return byteBuffer.getLong();
            case 6:
//                return readDouble();
                return byteBuffer.getDouble();
            case 7:
                String s = readString();
                return new BigInteger(s);
            case 8:
                s = readString(); // 因为前面调用了get()获取了type，不想再去mark & reset
                return new BigDecimal(s);
            case 9:
//                return readByte();
                return byteBuffer.get();
            case 10:
//                return readFloat();
                return byteBuffer.getFloat();
            case 11:
//                return readShort();
                return byteBuffer.getShort();
            case 12:
                return readString();
            case 13:
//                return readBool();
                return byteBuffer.get() != 0;
            case 14:
//                return readBytes();
                return getBytes();
            default:

        }
        return null;
    }

    @Override
    public <T> T readObject(Class<T> cls) throws IOException, ClassNotFoundException {
        return (T) readObject();
    }

    @Override
    public <T> T readObject(Class<T> cls, Type type) throws IOException, ClassNotFoundException {
        return (T) readObject();
    }

    private Object newInstance(Class clazz) {
        Object entity;
        try {
            entity = clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return entity;
    }
}
