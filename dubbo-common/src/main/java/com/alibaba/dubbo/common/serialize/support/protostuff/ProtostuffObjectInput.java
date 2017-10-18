package com.alibaba.dubbo.common.serialize.support.protostuff;

import com.alibaba.dubbo.common.serialize.ObjectInput;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.*;

import static com.alibaba.dubbo.common.serialize.support.protostuff.ProtoUtils.fromBytes;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

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

    private String readString() throws IOException {
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
        int cap = byteBuffer.capacity();
        int pos = byteBuffer.position();
        if (pos + 5 > cap) { // 对于返回null的，除了头，没有信息了
            return ProtoUtils.EMPTY_BYTES;
        }

        byteBuffer.mark();
        byte type = byteBuffer.get();
        int len;
        if (type == 3) { // 是hashmap，duboo接口信息，是最后一个字段
            len = cap - pos;
        } else {
            byte[] lendst = new byte[4];
            byteBuffer.get(lendst, 0, 4);
            len = getLength(lendst) + 5;
        }
        byteBuffer.reset();

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
            if (o != null) {
                LOGGER.debug("readObject，数据类型是=[{}].", o.getClass().getName());
            } else {
                LOGGER.debug("readObject，反序列化后数据是=[null].");
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

        T t = fromBytes(getBytes());
        if (t == null) {
            LOGGER.debug("readObject(class)，反序列化后数据是=[null].");
            return (T) cast(cls);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("readObject(class)，反序列化后数据是=[{}].", t.getClass().getName());
        }
        return t;
    }

    @Override
    public <T> T readObject(Class<T> cls, Type type) throws IOException, ClassNotFoundException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("readObject(class, type)，数据类型type是=[{}].", type.getTypeName());
        }
        T t = readObject(cls);
        if (t == null) {
            return (T) cast(cls);
        }
        return t;
    }

    private <T> Object cast(Class<T> cls) {
        if (cls.isAssignableFrom(List.class)) {
            return emptyList();
        } else if (cls.isAssignableFrom(Set.class)) {
            return emptySet();
        } else if (cls.isAssignableFrom(Map.class)) {
            return emptyMap();
        } else {
            try {
                return cls.newInstance();
            } catch (Exception e) {
                LOGGER.error("序列化返回空cls.newInstance错误，msg=[{}].", e.getMessage());
            }
        }
        return null;
    }

    private int getLength(byte[] res) {
        return (res[0] & 0xff) | ((res[1] << 8) & 0xff00) | ((res[2] << 24) >>> 8) | (res[3] << 24);
    }
}
