package com.alibaba.dubbo.common.utils;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.ArraySchema;
import io.protostuff.runtime.ArraySchemas;
import io.protostuff.runtime.RuntimeSchema;

import java.nio.ByteBuffer;

/**
 * @author yinlei
 * @since 2017/10/17 11:14
 */
public class ByteBufferTest {

    public static void main(String[] args) {
        String[] strArray = new String[3];
        strArray[0] = "111";
        strArray[1] = "尹雷";
        strArray[2] = "df个";
        Schema schema = RuntimeSchema.getSchema(String.class);

        byte[] str2array = ProtostuffIOUtil.toByteArray(strArray, schema, LinkedBuffer.allocate());
        String[] strArray2 = new String[0];
        ProtostuffIOUtil.mergeFrom(str2array, strArray2, schema);

//        byte[] em = new byte[0];
//        System.out.println(em.length + "" + em[0]);
        byte[] bs = new byte[3];
        bs[0] = 1;
        bs[1] = 12;
        bs[2] = 14;

        ByteBuffer byteBuffer = ByteBuffer.wrap(bs);
//        byteBuffer.flip(); // 还没有写，所以不需要切换为读

        byte b = byteBuffer.get();
        System.out.println(b);

        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putDouble(234D);
        buffer.putDouble(345D);

        if (buffer.remaining() < 10) {
            ByteBuffer buffer2 = ByteBuffer.allocate(buffer.capacity() + 32 + 10);
            buffer.flip();
            buffer2.put(buffer); // put 之前要切换为读模式
            buffer = buffer2;
        }
        buffer.put(bs);

        System.out.println(buffer.remaining());
        buffer.flip();
        int limit = buffer.limit();
        byte[] actual = new byte[limit];
        buffer.get(actual);
        System.out.println();
    }
}
