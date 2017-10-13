package com.alibaba.dubbo.common.serialize.support.protostuff;

import com.alibaba.dubbo.common.serialize.ObjectInput;
import io.protostuff.ByteArrayInput;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

/**
 * 基于protostuff的，对象反序列化。
 *
 * @author yinlei
 * @since 2017/10/13 9:28
 */
public class ProtostuffObjectInput implements ObjectInput {

    private ByteArrayInput codedInput;
    private byte[] bytes;

    public ProtostuffObjectInput(InputStream inputStream) {
        try {
            bytes = IOUtils.toByteArray(inputStream);
        } catch (IOException e) {

        }
        codedInput = new ByteArrayInput(bytes, false);
    }

    @Override
    public boolean readBool() throws IOException {
        return codedInput.readBool();
    }

    @Override
    public byte readByte() throws IOException {
        return (byte) codedInput.readInt32();
    }

    @Override
    public short readShort() throws IOException {
        return (short) codedInput.readInt32();
    }

    @Override
    public int readInt() throws IOException {
        return codedInput.readInt32();
    }

    @Override
    public long readLong() throws IOException {
        return codedInput.readInt64();
    }

    @Override
    public float readFloat() throws IOException {
        return codedInput.readFloat();
    }

    @Override
    public double readDouble() throws IOException {
        return codedInput.readDouble();
    }

    @Override
    public String readUTF() throws IOException {
        return codedInput.readString();
    }

    @Override
    public byte[] readBytes() throws IOException {
        return codedInput.readByteArray();
    }

    @Override
    public Object readObject() throws IOException, ClassNotFoundException {
        throw new IllegalStateException("not support operation.");
    }

    @Override
    public <T> T readObject(Class<T> cls) throws IOException, ClassNotFoundException {
        try {
            T object = cls.newInstance();
            Schema<T> schema = RuntimeSchema.getSchema(cls);
            ProtostuffIOUtil.mergeFrom(bytes, object, schema);
            return object;
        } catch (Exception e) {
            throw new IOException("class newInstance error, class=" + cls.getName(), e);
        }
    }

    @Override
    public <T> T readObject(Class<T> cls, Type type) throws IOException, ClassNotFoundException {
        return readObject(cls);
    }
}
