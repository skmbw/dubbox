package com.alibaba.dubbo.common.serialize.support.protostuff;

import com.alibaba.dubbo.common.serialize.ObjectInput;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.HashMap;

import static com.alibaba.dubbo.common.serialize.support.protostuff.ProtoUtils.fromBytes;

/**
 * 基于Protostuff的，对象反序列化。主要的问题是，dubbo一次把所有的数据都传进来了，而protostuff又没有提供
 * 类似直接读取基本类型的数据（ByteArrayInput做不到），所以要使用ByteBuffer自己去截取。
 *
 * @author yinlei
 * @since 2017/10/13 9:28
 */
public class ProtostuffObjectInput implements ObjectInput {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtostuffObjectInput.class);

    private byte[] bytes;
    private ByteBuffer byteBuffer;

    public ProtostuffObjectInput(InputStream inputStream) {
        try {
            bytes = IOUtils.toByteArray(inputStream);
            byteBuffer = ByteBuffer.wrap(bytes);

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
        boolean b = bytes != null && bytes[0] != 0;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("readBool数据是=[{}].", b);
        }
        return b;
    }

    @Override
    public byte readByte() throws IOException {
        byte b = Byte.parseByte(readString());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("readByte数据是=[{}].", b);
        }
        return b;
    }

    @Override
    public short readShort() throws IOException {
        short s = Short.parseShort(readString());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("readShort数据是=[{}].", s);
        }
        return s;
    }

    @Override
    public int readInt() throws IOException {
        int i = Integer.parseInt(readString());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("readInt数据是=[{}].", i);
        }
        return i;
    }

    @Override
    public long readLong() throws IOException {
        long l = Long.parseLong(readString());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("readLong数据是=[{}].", l);
        }
        return l;
    }

    @Override
    public float readFloat() throws IOException {
        float f = Float.parseFloat(readString());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("readFloat数据是=[{}].", f);
        }
        return f;
    }

    @Override
    public double readDouble() throws IOException {
        double d = Double.parseDouble(readString());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("readDouble数据是=[{}].", d);
        }
        return d;
    }

    @Override
    public String readUTF() throws IOException {
        String s = readString();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("readUTF数据是=[{}].", s);
        }
        return s;
    }

    public String readString() throws IOException {
        byte b = byteBuffer.get();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("readString，首字节是=[{}].", b);
        }

        byte[] lendst = new byte[4];
        byteBuffer.get(lendst, 0, 4); // 不需要偏移量，已经读过了
        int len = getLength(lendst);
        byte[] dst = new byte[len];
        byteBuffer.get(dst, 0, len);

        return new String(dst);
    }

    @Override
    public byte[] readBytes() throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("readBytes，数据原样返回.");
        }
        return bytes;
    }

    private byte[] getBytes() {
        int pos = byteBuffer.position();
        int cap = byteBuffer.capacity();
        int len = cap - pos;
        byte[] dst = new byte[len];
        byteBuffer.get(dst, 0, len);

        return dst;
    }

    @Override
    public Object readObject() throws IOException, ClassNotFoundException {
        if (bytes == null || bytes.length == 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("readObject，数据为空，返回null.");
            }
            return null;
        }
        // 这个是心跳的
        if (bytes.length == 2 && bytes[0] == 13) {
            if (bytes[1] == 1) {
                return true;
            }
            return false;
        }

        Object o = fromBytes(getBytes());
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
        if (bytes == null || bytes.length == 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("readObject(class)，数据为空，返回null.");
            }
            return null;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("readObject(class)，数据类型是=[{}].", cls.getName());
        }

        T t = fromBytes(getBytes()); // 这里会有Map，序列化接口的信息
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

    private int getLength(byte[] res) {
        return (res[0] & 0xff) | ((res[1] << 8) & 0xff00) | ((res[2] << 24) >>> 8) | (res[3] << 24);
    }
}
