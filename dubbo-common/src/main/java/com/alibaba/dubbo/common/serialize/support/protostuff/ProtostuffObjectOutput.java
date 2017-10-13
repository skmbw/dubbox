package com.alibaba.dubbo.common.serialize.support.protostuff;

import com.alibaba.dubbo.common.serialize.ObjectOutput;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffOutput;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 基于protostuff的序列化
 *
 * @author yinlei
 * @since 2017/10/13 9:28
 */
public class ProtostuffObjectOutput implements ObjectOutput {

    private OutputStream outputStream;
    private ProtostuffOutput output;
    private int fieldNumber;

    public ProtostuffObjectOutput(OutputStream outputStream) {
        this.outputStream = outputStream;
        output = new ProtostuffOutput(LinkedBuffer.allocate(1024));
        fieldNumber = 1;
    }

    @Override
    public void writeBool(boolean v) throws IOException {
        output.writeBool(fieldNumber++, v, false);
    }

    @Override
    public void writeByte(byte v) throws IOException {
        output.writeInt32(fieldNumber++, v, false);
    }

    @Override
    public void writeShort(short v) throws IOException {
        output.writeInt32(fieldNumber++, v, false);
    }

    @Override
    public void writeInt(int v) throws IOException {
        output.writeInt32(fieldNumber++, v, false);
    }

    @Override
    public void writeLong(long v) throws IOException {
        output.writeInt64(fieldNumber++, v, false);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        output.writeFloat(fieldNumber++, v, false);
    }

    @Override
    public void writeDouble(double v) throws IOException {
        output.writeDouble(fieldNumber++, v, false);
    }

    @Override
    public void writeUTF(String v) throws IOException {
//        output.writeString(fieldNumber++, v, false);
//        outputStream.write(v.getBytes());
        writeObject(v);
    }

    @Override
    public void writeBytes(byte[] v) throws IOException {
        output.writeByteArray(fieldNumber++, v, false);
    }

    @Override
    public void writeBytes(byte[] v, int off, int len) throws IOException {
        byte[] v2 = new byte[len];
        System.arraycopy(v, off, v2, 0, len);
        output.writeByteArray(fieldNumber++, v2, false);
    }

    @Override
    public void flushBuffer() throws IOException {
        byte[] bytes = output.toByteArray();
        outputStream.write(bytes);
        outputStream.flush();
    }

    @Override
    public void writeObject(Object obj) throws IOException {
        if (obj == null) {
//            outputStream.write(1);
            return;
        }
//        Schema schema = RuntimeSchema.getSchema(obj.getClass());
//        ProtostuffIOUtil.writeTo(outputStream, obj, schema, LinkedBuffer.allocate());
        byte[] result = ProtoUtils.toBytes(obj);
        outputStream.write(result);
    }
}
