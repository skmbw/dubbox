package com.alibaba.dubbo.common.utils;

import com.alibaba.dubbo.common.serialize.support.protostuff.NumberUtils;

/**
 * @author yinlei
 * @since 2017/10/18 12:47
 */
public class NumberUtilsTest {
    public static void main(String[] args) {
        long l = 1234567L;
        byte[] longBytes = NumberUtils.longToByte(l);
        long l2 = NumberUtils.byteToLong(longBytes);
        System.out.println(l == l2);

        double d = 9234566447.67819D;
        byte[] doubleBytes = NumberUtils.doubleToByte(d);
        double d2 = NumberUtils.byteToDouble(doubleBytes);
        System.out.println(d == d2);

        int i = 33981212;
        byte[] intBytes = NumberUtils.intToByte(i);
        int i2 = NumberUtils.byteToInt(intBytes);
        System.out.println(i == i2);

        float f = 8967565.343F;
        byte[] floatBytes = NumberUtils.floatToByte(f);
        float f2 = NumberUtils.byteToFloat(floatBytes);
        System.out.println(f == f2);

        short s = 9233;
        byte[] shortBytes = NumberUtils.shortToByte(s);
        short s2 = NumberUtils.byteToShort(shortBytes);
        System.out.println(s == s2);
    }
}
