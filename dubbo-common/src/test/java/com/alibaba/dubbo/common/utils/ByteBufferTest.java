package com.alibaba.dubbo.common.utils;

import java.nio.ByteBuffer;

/**
 * @author yinlei
 * @since 2017/10/17 11:14
 */
public class ByteBufferTest {

    public static void main(String[] args) {
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
