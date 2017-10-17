package com.alibaba.dubbo.common.serialize.support.protostuff;

import com.alibaba.dubbo.common.serialize.ObjectInput;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.HashMap;

import static com.alibaba.dubbo.common.serialize.support.protostuff.ProtoUtils.fromBytes;

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
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("数据长度是=[{}].", bytes.length);
            }
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Protostuff convert to byte[] error, msg=[{}].",
                        e.getClass().getName() + e.getMessage());
            }
        }
    }

    @Override
    public boolean readBool() throws IOException {
        return fromBytes(bytes);
    }

    @Override
    public byte readByte() throws IOException {
        byte b = ProtoUtils.fromBytes(bytes);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("readByte数据是=[{}].", b);
        }
        return b;
    }

    @Override
    public short readShort() throws IOException {
        short s = fromBytes(bytes);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("readShort数据是=[{}].", s);
        }
        return s;
    }

    @Override
    public int readInt() throws IOException {
        int i;
        try {
            i = fromBytes(bytes);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("readInt数据是=[{}].", i);
            }
        } catch (ClassCastException e) {
            LOGGER.error("readInt转型错误，返回1。", e);
            i = 1; // version
        }
        return i;
    }

    @Override
    public long readLong() throws IOException {
        long l = fromBytes(bytes);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("readLong数据是=[{}].", l);
        }
        return l;
    }

    @Override
    public float readFloat() throws IOException {
        float f = fromBytes(bytes);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("readFloat数据是=[{}].", f);
        }
        return f;
    }

    @Override
    public double readDouble() throws IOException {
        double d = fromBytes(bytes);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("readDouble数据是=[{}].", d);
        }
        return d;
    }

    @Override
    public String readUTF() throws IOException {
        String s = fromBytes(bytes);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("readUTF数据是=[{}].", s);
        }
        return s;
    }

    @Override
    public byte[] readBytes() throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("readBytes，数据原样返回.");
        }
        return bytes;
    }

    @Override
    public Object readObject() throws IOException, ClassNotFoundException {
        if (bytes == null || bytes.length == 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("readObject，数据为空，返回null.");
            }
            return null;
        }
        Object o = fromBytes(bytes);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("readObject，数据类型是=[{}].", o.getClass().getName());
            if (o instanceof HashMap) {
                LOGGER.debug("readObject，HashMap数据是=[{}].", o);
            }
        }
        return o;
    }

    @Override
    public <T> T readObject(Class<T> cls) throws IOException, ClassNotFoundException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("readObject(class)，数据类型是=[{}].", cls.getName());
        }
        T t = fromBytes(bytes); // 这里会有Map，序列化接口的信息
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("readObject(class)，数据是=[{}].", t);
            if (t instanceof HashMap) {
                LOGGER.debug("readObject(class)，HashMap数据是=[{}].", t);
            }
        }
        return t;
    }

    @Override
    public <T> T readObject(Class<T> cls, Type type) throws IOException, ClassNotFoundException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("readObject(class, type)，数据类型type是=[{}].", type.getTypeName());
        }
        return readObject(cls);
    }
}
