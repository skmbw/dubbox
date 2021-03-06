package com.alibaba.dubbo.common.serialize.support.protostuff;

import com.alibaba.dubbo.common.compiler.support.ClassUtils;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import io.protostuff.*;
import io.protostuff.runtime.RuntimeSchema;
import org.apache.commons.io.IOUtils;
import org.objenesis.ObjenesisHelper;

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

    public static final Schema<WrapArray> ARRAY_SCHEMA = RuntimeSchema.getSchema(WrapArray.class);

    private byte[] bytes;
    private ByteBuffer byteBuffer;

    public ProtoInput(InputStream inputStream) throws IOException {
        bytes = IOUtils.toByteArray(inputStream);
        byteBuffer = ByteBuffer.wrap(bytes);
    }

    @Override
    public boolean readBool() throws IOException {
        byteBuffer.get(); // 这个是数据类型，pass，当然也可以验证（其实没有必要）
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
            return new String(data, "UTF-8");
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
    @SuppressWarnings("unchecked")
    public Object readObject() throws IOException, ClassNotFoundException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        // 获取基本类型，因为dubbo中有调用readObject方法获取基本类型的，所以要处理
        byte type = byteBuffer.get();
        // 基本类型和复合类型在一起，导致获取数据长度有问题
        switch (type) {
            case 4:
                return byteBuffer.getInt();
            case 5:
                return byteBuffer.getLong();
            case 6:
                return byteBuffer.getDouble();
            case 7:
                String s = readString();
                return new BigInteger(s == null ? "0" : s);
            case 8:
                s = readString(); // 因为前面调用了get()获取了type，不想再去mark & reset
                return new BigDecimal(s == null ? "0" : s);
            case 9:
                // 标志位 不想 再重置了
                return byteBuffer.get();
            case 10:
                return byteBuffer.getFloat();
            case 11:
                return byteBuffer.getShort();
            case 12:
                return readString();
            case 13:
                return byteBuffer.get() != 0;
            case 14:
                return getBytes();
            case 15: // 数组
                int length = byteBuffer.getInt();
                byte[] data = new byte[length - 5]; // 去掉类型和长度
                byteBuffer.get(data);

                WrapArray wrapArray = new WrapArray();
                ProtostuffIOUtil.mergeFrom(data, wrapArray, ARRAY_SCHEMA);
                return wrapArray.getArray();
            case 16: // 异常，Exception中的clause是循环引用，直接序列化会OOE
                int totalLength = byteBuffer.getInt();
                int nameLength = byteBuffer.getInt();
                byte[] classNameBytes = new byte[nameLength];
                byteBuffer.get(classNameBytes);
                String message = "";
                if (totalLength > nameLength) {
                    int messageLength = totalLength - nameLength;
                    byte[] messageBytes = new byte[messageLength];
                    byteBuffer.get(messageBytes);
                    message = new String(messageBytes, "UTF-8");
                }
                String className = new String(classNameBytes, "UTF-8");
                return new RuntimeException(className + ";message=" + message);
        }

        // 集合和对象类型和基本类型分开，代码更整洁
        int totalLength = byteBuffer.getInt();
        if (totalLength == 0) {
            switch (type) {
                case 0: // 对象
                    return null;
                case 1:
                    return Collections.emptyList();
                case 2:
                    return Collections.emptySet();
                case 3:
                    return Collections.emptyMap();
            }
        }
        int nameLength = byteBuffer.getInt();
        byte[] nameBytes = new byte[nameLength];
        byteBuffer.get(nameBytes);
        String className = new String(nameBytes, "UTF-8");
        Class clazz = ClassUtils.forName(className);
        Schema schema = RuntimeSchema.getSchema(clazz);

        byte[] dataBytes = new byte[totalLength - nameLength - 9];
        byteBuffer.get(dataBytes);
        switch (type) {
            case 0:
                // 这个不调用构造函数，有一个副作用，就是基本类型的默认值没有了，数值类都是0
                Object object = ObjenesisHelper.newInstance(clazz); // newInstance(clazz);
                ProtostuffIOUtil.mergeFrom(dataBytes, object, schema);
                return object;
            case 1:
                MessageCollectionSchema collectionSchema = new MessageCollectionSchema(schema);
                List list = new ArrayList();
                ProtostuffIOUtil.mergeFrom(dataBytes, list, collectionSchema);
                return list;
            case 2:
                collectionSchema = new MessageCollectionSchema(schema);
                Set set = new HashSet();
                ProtostuffIOUtil.mergeFrom(dataBytes, set, collectionSchema);
                return set;
            case 3:
                StringMapSchema stringSchema = new StringMapSchema(schema);
                Map map = new HashMap();
                ProtostuffIOUtil.mergeFrom(dataBytes, map, stringSchema);
                return map;
            default:

        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T readObject(Class<T> cls) throws IOException, ClassNotFoundException {
        return (T) readObject();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T readObject(Class<T> cls, Type type) throws IOException, ClassNotFoundException {
        return (T) readObject();
    }

    private Object newInstance(Class clazz) {
        Object entity;
        try {
            entity = clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("实例化对象的异常", e);
        }
        return entity;
    }
}
