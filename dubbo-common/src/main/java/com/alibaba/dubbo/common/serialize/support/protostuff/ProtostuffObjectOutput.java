package com.alibaba.dubbo.common.serialize.support.protostuff;

import com.alibaba.dubbo.common.serialize.ObjectOutput;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 基于protostuff的序列化
 *
 * @author yinlei
 * @since 2017/10/13 9:28
 */
public class ProtostuffObjectOutput implements ObjectOutput {

    private OutputStream output;

    public ProtostuffObjectOutput(OutputStream output) {
        this.output = output;
    }

    @Override
    public void writeBool(boolean v) throws IOException {
        output.write(ProtoUtils.toBytes(v, 13));
    }

    @Override
    public void writeByte(byte v) throws IOException {
        output.write(ProtoUtils.toBytes(v, 9));
    }

    @Override
    public void writeShort(short v) throws IOException {
        output.write(ProtoUtils.toBytes(v, 11));
    }

    @Override
    public void writeInt(int v) throws IOException {
        output.write(ProtoUtils.toBytes(v, 4));
    }

    @Override
    public void writeLong(long v) throws IOException {
        output.write(ProtoUtils.toBytes(v, 5));
    }

    @Override
    public void writeFloat(float v) throws IOException {
        output.write(ProtoUtils.toBytes(v, 10));
    }

    @Override
    public void writeDouble(double v) throws IOException {
        output.write(ProtoUtils.toBytes(v, 6));
    }

    @Override
    public void writeUTF(String v) throws IOException {
        output.write(ProtoUtils.toBytes(v, 12));
    }

    @Override
    public void writeBytes(byte[] v) throws IOException {
        output.write(v);
    }

    @Override
    public void writeBytes(byte[] v, int off, int len) throws IOException {
        byte[] v2 = new byte[len];
        System.arraycopy(v, off, v2, 0, len);
        output.write(v2);
    }

    @Override
    public void flushBuffer() throws IOException {
        output.flush();
    }

    @Override
    public void writeObject(Object obj) throws IOException {
        if (obj == null) {
            return;
        }
        byte[] result = ProtoUtils.toBytes(obj);
        output.write(result);
    }
}
