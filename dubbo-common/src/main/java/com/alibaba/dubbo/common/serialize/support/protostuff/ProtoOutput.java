package com.alibaba.dubbo.common.serialize.support.protostuff;

import com.alibaba.dubbo.common.serialize.ObjectOutput;
import io.protostuff.*;
import io.protostuff.runtime.RuntimeSchema;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 基于ByteBuffer实现的数据输出流。
 *
 * @author yinlei
 * @since 2017/10/18 12:57
 */
public class ProtoOutput implements ObjectOutput {

    private OutputStream output;
    private ByteBuffer byteBuffer;

    public ProtoOutput(OutputStream output) {
        this.output = output;
        this.byteBuffer = ByteBuffer.allocate(1024); // 1kb
    }

    @Override
    public void writeBool(boolean v) throws IOException {
        check(1);
        byteBuffer.put((byte) 1);
    }

    @Override
    public void writeByte(byte v) throws IOException {
        check(1);
        byteBuffer.put(v);
    }

    @Override
    public void writeShort(short v) throws IOException {
        check(2);
        byteBuffer.putShort(v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        check(4);
        byteBuffer.putInt(v);
    }

    @Override
    public void writeLong(long v) throws IOException {
        check(8);
        byteBuffer.putLong(v);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        check(4);
        byteBuffer.putFloat(v);
    }

    @Override
    public void writeDouble(double v) throws IOException {
        check(8);
        byteBuffer.putDouble(v);
    }

    @Override
    public void writeUTF(String v) throws IOException {
        check(1);
        byteBuffer.put((byte) 12); // 数据类型
        if (v == null) {
            check(4);
            byteBuffer.putInt(0); // 长度为0
        } else {
            byte[] bytes = v.getBytes();
            int len = bytes.length;
            check(4 + len);
            byteBuffer.putInt(len);
            byteBuffer.put(bytes); // 存入数据
        }
    }

    @Override
    public void writeBytes(byte[] v) throws IOException {
        if (v == null || v.length == 0) {
            check(5);
            byteBuffer.put((byte) 14);
            byteBuffer.putInt(0);
        } else {
            writeBytes(v, 0 , v.length);
        }
    }

    @Override
    public void writeBytes(byte[] v, int off, int len) throws IOException {
        check(1);
        byteBuffer.put((byte) 14);
        if (v == null) {
            check(4);
            byteBuffer.putInt(0);
        } else {
            check(4 + len);
            byteBuffer.putInt(len);
            byteBuffer.put(v, off, len);
        }
    }

    @Override
    public void flushBuffer() throws IOException {
        byteBuffer.flip();
        int limit = byteBuffer.limit();
        byte[] bytes = new byte[limit];
        byteBuffer.get(bytes);
        output.write(bytes);
        output.flush();
    }

    @Override
    public void writeObject(Object obj) throws IOException {
        if (obj == null) {
            check(5);
            byteBuffer.put((byte) 0); // type
            byteBuffer.putInt(0);
            return;
        }
        Class cls;
        if (obj instanceof List) {
            List list = (List) obj;
            if (list.isEmpty()) {
                check(5);
                byteBuffer.put((byte) 1); // 类型
                byteBuffer.putInt(0); // 长度
                return;
            }
            cls = list.get(0).getClass();
            byte[] nameBytes = cls.getName().getBytes();
            int nameLength = nameBytes.length;
            byte[] listBytes = collectToBytes(cls, list);
            int dataLength = listBytes.length;
            // 9 = 1 + 4 + 4
            int totalLength = 9 + nameLength + dataLength;
            check(totalLength);
            byteBuffer.put((byte) 1); // 类型
            byteBuffer.putInt(totalLength);
            byteBuffer.putInt(nameLength);
            byteBuffer.put(nameBytes);
            byteBuffer.put(listBytes);
        } else if (obj instanceof Set) {
            Set set = (Set) obj;
            if (set.isEmpty()) {
                check(5);
                byteBuffer.put((byte) 2); // 类型
                byteBuffer.putInt(0); // 长度
                return;
            }
            cls = set.iterator().next().getClass();
            byte[] nameBytes = cls.getName().getBytes();
            int nameLength = nameBytes.length;
            byte[] dataBytes = collectToBytes(cls, set);
            int dataLength = dataBytes.length;
            // 9 = 1 + 4 + 4
            int totalLength = 9 + nameLength + dataLength;
            check(totalLength);
            byteBuffer.put((byte) 2); // 类型
            byteBuffer.putInt(totalLength);
            byteBuffer.putInt(nameLength);
            byteBuffer.put(nameBytes);
            byteBuffer.put(dataBytes);
        } else if (obj instanceof Map) {
            Map map = (Map) obj;
            if (map.isEmpty()) {
                check(5);
                byteBuffer.put((byte) 3); // 类型
                byteBuffer.putInt(0); // 长度
                return;
            }
            // value对象的类型
            cls = map.values().iterator().next().getClass();
            byte[] nameBytes = cls.getName().getBytes();
            int nameLength = nameBytes.length;
            byte[] mapBytes = mapToBytes(cls, map);
            int dataLength = mapBytes.length;
            // 9 = 1 + 4 + 4
            int totalLength = 9 + nameLength + dataLength;
            check(totalLength);
            byteBuffer.put((byte) 3); // 类型
            byteBuffer.putInt(totalLength);
            byteBuffer.putInt(nameLength);
            byteBuffer.put(nameBytes);
            byteBuffer.put(mapBytes);
        } else if (obj instanceof Number) {
            if (obj instanceof Integer) {
                int v = (int) obj;
                writeInt(v);
            } else if (obj instanceof Long) {
                long l = (long) obj;
                writeLong(l);
            } else if (obj instanceof Double) {
                double d = (double) obj;
                writeDouble(d);
            } else if (obj instanceof BigInteger) {
                BigInteger s = (BigInteger) obj;
                String v = s.toString();
                writeUTF(v);
            } else if (obj instanceof BigDecimal) {
                BigDecimal s = (BigDecimal) obj;
                String v = s.toString();
                writeUTF(v);
            } else if (obj instanceof Byte) {
                byte v = (byte) obj;
                writeByte(v);
            } else if (obj instanceof Float) {
                float v = (float) obj;
                writeFloat(v);
            } else if (obj instanceof Short) {
                short v = (short) obj;
                writeFloat(v);
            } else {
                throw new RuntimeException("不支持的数字类型:" + obj.getClass().getName());
            }
        } else if (obj instanceof String) {
            String v = (String) obj;
            writeUTF(v);
        } else if (obj instanceof Boolean) {
            boolean b = (boolean) obj;
            writeBool(b);
        } else {
            cls = obj.getClass();
            Schema schema = RuntimeSchema.getSchema(cls);
            LinkedBuffer buffer = LinkedBuffer.allocate();
            @SuppressWarnings("unchecked")
            byte[] bytes = ProtostuffIOUtil.toByteArray(obj, schema, buffer);
            int length = bytes.length;
            byte[] nameBytes = cls.getName().getBytes();
            int nameLength = nameBytes.length;
            int totalLength = 9 + nameLength + length;
            check(totalLength);
            byteBuffer.put((byte) 0);
            byteBuffer.putInt(totalLength);
            byteBuffer.putInt(nameLength);
            byteBuffer.put(nameBytes);
            byteBuffer.put(bytes);
        }
    }

    @SuppressWarnings("unchecked")
    private byte[] mapToBytes(Class clazz, Map map) {
        Schema schema = RuntimeSchema.getSchema(clazz);
        StringMapSchema collectionSchema = new StringMapSchema(schema);
        LinkedBuffer buffer = LinkedBuffer.allocate(1024);
        return ProtostuffIOUtil.toByteArray(map, collectionSchema, buffer);
    }

    @SuppressWarnings("unchecked")
    private byte[] collectToBytes(Class clazz, Collection list) {
        Schema schema = RuntimeSchema.getSchema(clazz);
        MessageCollectionSchema collectionSchema = new MessageCollectionSchema(schema);
        LinkedBuffer buffer = LinkedBuffer.allocate(1024);
        return ProtostuffIOUtil.toByteArray(list, collectionSchema, buffer);
    }

    /**
     * 检查buffer中的剩余空间是否能放下新加入的数据，不行就扩容。
     * 扩容的大小为（size + 256）。
     *
     * @param size buffer要新加入的数据大小
     */
    private void check(int size) {
        if (byteBuffer.remaining() < size) {
            int cap = byteBuffer.capacity() + size + 256;
            ByteBuffer buffer = ByteBuffer.allocate(cap);
            buffer.put(byteBuffer);
            byteBuffer = buffer;
        }
    }
}
