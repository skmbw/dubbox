package com.alibaba.dubbo.common.serialize.support.protostuff;

import com.alibaba.dubbo.common.serialize.ObjectOutput;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.io.IOException;
import java.io.OutputStream;

import static java.lang.System.out;

/**
 * 基于protostuff的序列化
 *
 * @author yinlei
 * @since 2017/10/13 9:28
 */
public class ProtostuffObjectOutput implements ObjectOutput {

    private OutputStream outputStream;

    public ProtostuffObjectOutput(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void writeBool(boolean v) throws IOException {
    }

    @Override
    public void writeByte(byte v) throws IOException {

    }

    @Override
    public void writeShort(short v) throws IOException {

    }

    @Override
    public void writeInt(int v) throws IOException {

    }

    @Override
    public void writeLong(long v) throws IOException {

    }

    @Override
    public void writeFloat(float v) throws IOException {

    }

    @Override
    public void writeDouble(double v) throws IOException {

    }

    @Override
    public void writeUTF(String v) throws IOException {
        outputStream.write(v.getBytes());
    }

    @Override
    public void writeBytes(byte[] v) throws IOException {
        outputStream.write(v);
    }

    @Override
    public void writeBytes(byte[] v, int off, int len) throws IOException {
        outputStream.write(v, off, len);
    }

    @Override
    public void flushBuffer() throws IOException {
        outputStream.flush();
    }

    @Override
    public void writeObject(Object obj) throws IOException {
        Schema schema = RuntimeSchema.getSchema(obj.getClass());
        ProtostuffIOUtil.writeTo(out, obj, schema, LinkedBuffer.allocate());
    }
}
