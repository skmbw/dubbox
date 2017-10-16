package com.alibaba.dubbo.common.utils;

import com.alibaba.dubbo.common.serialize.support.protostuff.ProtoUtils;
import org.junit.Test;

/**
 * @author yinlei
 * @since 2017/10/16 17:00
 */
public class ProtoUtilsTest {
    @Test
    public void test1() {
        String a = "yin类121adda";
        byte[] abytes = ProtoUtils.toBytes(a);
        String ab = ProtoUtils.fromBytes(abytes);
        System.out.println(ab);

        Integer c = 12356;
        abytes = ProtoUtils.toBytes(c);
        Integer cb = ProtoUtils.fromBytes(abytes);
        System.out.println(cb);

        Long cl = 232356L;
        abytes = ProtoUtils.toBytes(cl);
        Long cbl = ProtoUtils.fromBytes(abytes);
        System.out.println(cbl);
    }
}
