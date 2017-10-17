package com.alibaba.dubbo.common.utils;

import com.alibaba.dubbo.common.serialize.support.protostuff.ProtoUtils;

import static com.alibaba.dubbo.common.serialize.support.protostuff.ProtoUtils.fromBytes;
import static com.alibaba.dubbo.common.serialize.support.protostuff.ProtoUtils.toBytes;

/**
 * @author yinlei
 * @since 2017/10/16 17:00
 */
public class ProtoUtilsTest {
    public static void main(String[] aaaa) {
        String a = "yin类121adda";
        byte[] abytes = toBytes(a);
        String ab = fromBytes(abytes);
        System.out.println(ab);

        Integer c = 12356;
        abytes = toBytes(c);
        Integer cb = fromBytes(abytes);
        System.out.println(cb);

        Long cl = 232356L;
        abytes = toBytes(cl);
        Long cbl = fromBytes(abytes);
        System.out.println(cbl);

        NullPointerException exception = new NullPointerException();
        byte[] aa = ProtoUtils.toBytes(exception);
        exception = ProtoUtils.fromBytes(aa);
        System.out.println();
    }
}
