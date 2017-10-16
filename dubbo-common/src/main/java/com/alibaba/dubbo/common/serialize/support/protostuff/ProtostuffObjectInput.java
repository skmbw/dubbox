package com.alibaba.dubbo.common.serialize.support.protostuff;

import com.alibaba.dubbo.common.serialize.ObjectInput;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

/**
 * 基于Protostuff的，对象反序列化。
 *
 * @author yinlei
 * @since 2017/10/13 9:28
 */
public class ProtostuffObjectInput implements ObjectInput {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtostuffObjectInput.class);

    private byte[] bytes;

    public ProtostuffObjectInput(InputStream inputStream) {
        try {
            bytes = IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Protostuff convert to byte[] error, msg=[{}].",
                        e.getClass().getName() + e.getMessage());
            }
        }
    }

    @Override
    public boolean readBool() throws IOException {
        return ProtoUtils.fromBytes(bytes);
    }

    @Override
    public byte readByte() throws IOException {
        return ProtoUtils.fromBytes(bytes);
    }

    @Override
    public short readShort() throws IOException {
        return ProtoUtils.fromBytes(bytes);
    }

    @Override
    public int readInt() throws IOException {
        try {
            return ProtoUtils.fromBytes(bytes);
        } catch (ClassCastException e) {
            return 1; // version
        }
    }

    @Override
    public long readLong() throws IOException {
        return ProtoUtils.fromBytes(bytes);
    }

    @Override
    public float readFloat() throws IOException {
        return ProtoUtils.fromBytes(bytes);
    }

    @Override
    public double readDouble() throws IOException {
        return ProtoUtils.fromBytes(bytes);
    }

    @Override
    public String readUTF() throws IOException {
        return ProtoUtils.fromBytes(bytes);
    }

    @Override
    public byte[] readBytes() throws IOException {
        return bytes;
    }

    @Override
    public Object readObject() throws IOException, ClassNotFoundException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return ProtoUtils.fromBytes(bytes);
    }

    @Override
    public <T> T readObject(Class<T> cls) throws IOException, ClassNotFoundException {
        return ProtoUtils.fromBytes(bytes); // 这里会有Map，序列化接口的信息
    }

    @Override
    public <T> T readObject(Class<T> cls, Type type) throws IOException, ClassNotFoundException {
        return readObject(cls);
    }
}
