package com.alibaba.dubbo.common.serialize.support.protostuff;

import com.alibaba.dubbo.common.serialize.ObjectInput;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;

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
        return byteBuffer.get() != 0;
    }

    @Override
    public byte readByte() throws IOException {
        return byteBuffer.get();
    }

    @Override
    public short readShort() throws IOException {
        return byteBuffer.getShort();
    }

    @Override
    public int readInt() throws IOException {
        return byteBuffer.getInt();
    }

    @Override
    public long readLong() throws IOException {
        return byteBuffer.getInt();
    }

    @Override
    public float readFloat() throws IOException {
        return byteBuffer.getFloat();
    }

    @Override
    public double readDouble() throws IOException {
        return byteBuffer.getDouble();
    }

    @Override
    public String readUTF() throws IOException {
        return null;
    }

    @Override
    public byte[] readBytes() throws IOException {
        return new byte[0];
    }

    @Override
    public Object readObject() throws IOException, ClassNotFoundException {
        return null;
    }

    @Override
    public <T> T readObject(Class<T> cls) throws IOException, ClassNotFoundException {
        return null;
    }

    @Override
    public <T> T readObject(Class<T> cls, Type type) throws IOException, ClassNotFoundException {
        return null;
    }
}
