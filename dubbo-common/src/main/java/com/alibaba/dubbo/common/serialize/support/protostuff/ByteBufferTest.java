package com.alibaba.dubbo.common.serialize.support.protostuff;

import java.nio.ByteBuffer;

/**
 * @author yinlei
 * @since 2017/10/17 11:14
 */
public class ByteBufferTest {

    public static void main(String[] args) {
        byte[] bs = new byte[3];
        bs[0] = 1;
        bs[1] = 12;
        bs[2] = 14;

        ByteBuffer byteBuffer = ByteBuffer.wrap(bs);
//        byteBuffer.flip(); // 还没有写，所以不需要切换为读

        byte b = byteBuffer.get();
        System.out.println(b);
    }
}
