package com.alibaba.dubbo.common.serialize.support.protostuff;

/**
 * 基本类型和字节数组之间的转换
 *
 * @author yinlei
 * @since 2017/10/18 12:25
 */
public class NumberUtils {
    /**
     * 短整型转2字节数组
     *
     * @param number 短整型
     * @return 两位的字节数组
     */
    public static byte[] shortToByte(short number) {
        int temp = number;
        byte[] b = new byte[2];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Integer(temp & 0xff).byteValue();// 将最低位保存在最低位
            temp = temp >> 8; // 向右移8位
        }
        return b;
    }

    /**
     * 2字节的数组转为短整型
     *
     * @param bytes 两位的字节数组
     * @return 短整型
     */
    public static short byteToShort(byte[] bytes) {
        short s;
        short s0 = (short) (bytes[0] & 0xff);// 最低位
        short s1 = (short) (bytes[1] & 0xff);
        s1 <<= 8;
        s = (short) (s0 | s1);
        return s;
    }

    /**
     * 整型转为4字节数组
     *
     * @param i 整型
     * @return 四位的字节数组
     */
    public static byte[] intToByte(int i) {
        byte[] bt = new byte[4];
        bt[0] = (byte) (0xff & i);
        bt[1] = (byte) ((0xff00 & i) >> 8);
        bt[2] = (byte) ((0xff0000 & i) >> 16);
        bt[3] = (byte) ((0xff000000 & i) >> 24);
        return bt;
    }

    /**
     * 4字节数组转为整型
     *
     * @param bytes 字节数组
     * @return 整型
     */
    public static int byteToInt(byte[] bytes) {
        int num = bytes[0] & 0xFF;
        num |= ((bytes[1] << 8) & 0xFF00);
        num |= ((bytes[2] << 16) & 0xFF0000);
        num |= ((bytes[3] << 24) & 0xFF000000);
        return num;
    }

    /**
     * 长整型转为8字节数组
     *
     * @param number 长整型
     * @return byte[]
     */
    public static byte[] longToByte(long number) {
        long temp = number;
        byte[] b = new byte[8];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Long(temp & 0xff).byteValue();
            // 将最低位保存在最低位
            temp = temp >> 8;
            // 向右移8位
        }
        return b;
    }

    /**
     * 8字节数组转为长整型
     *
     * @param bytes 8字节数组
     * @return 长整型
     */
    public static long byteToLong(byte[] bytes) {
        long s;
        long s0 = bytes[0] & 0xff;// 最低位
        long s1 = bytes[1] & 0xff;
        long s2 = bytes[2] & 0xff;
        long s3 = bytes[3] & 0xff;
        long s4 = bytes[4] & 0xff;// 最低位
        long s5 = bytes[5] & 0xff;
        long s6 = bytes[6] & 0xff;
        long s7 = bytes[7] & 0xff; // s0不变
        s1 <<= 8;
        s2 <<= 16;
        s3 <<= 24;
        s4 <<= 8 * 4;
        s5 <<= 8 * 5;
        s6 <<= 8 * 6;
        s7 <<= 8 * 7;
        s = s0 | s1 | s2 | s3 | s4 | s5 | s6 | s7;
        return s;
    }

    /**
     * double转8字节数组
     *
     * @param d double
     * @return 8 byte array
     */
    public static byte[] doubleToByte(double d) {
        byte[] b = new byte[8];
        long l = Double.doubleToLongBits(d);
        for (int i = 0; i < b.length; i++) {
            b[i] = new Long(l).byteValue();
            l = l >> 8;
        }
        return b;
    }

    /**
     * 8字节数组转double
     *
     * @param bytes 8字节数组
     * @return double
     */
    public static double byteToDouble(byte[] bytes) {
        long l;
        l = bytes[0];
        l &= 0xff;
        l |= ((long) bytes[1] << 8);
        l &= 0xffff;
        l |= ((long) bytes[2] << 16);
        l &= 0xffffff;
        l |= ((long) bytes[3] << 24);
        l &= 0xffffffffL;
        l |= ((long) bytes[4] << 32);
        l &= 0xffffffffffL;

        l |= ((long) bytes[5] << 40);
        l &= 0xffffffffffffL;
        l |= ((long) bytes[6] << 48);

        l |= ((long) bytes[7] << 56);
        return Double.longBitsToDouble(l);
    }

    /**
     * 单精度浮点转为4字节数组
     *
     * @param f 单精度浮点数
     * @return 4字节数组
     */
    public static byte[] floatToByte(float f) {

        // 把float转换为byte[]
        int fbit = Float.floatToIntBits(f);

        return intToByte(fbit);

//        byte[] b = new byte[4];
//        for (int i = 0; i < 4; i++) {
//            b[i] = (byte) (fbit >> (24 - i * 8));
//        }
//
//        // 翻转数组
//        int len = b.length;
//        // 建立一个与源数组元素类型相同的数组
//        byte[] dest = new byte[len];
//        // 为了防止修改源数组，将源数组拷贝一份副本
//        System.arraycopy(b, 0, dest, 0, len);
//        byte temp;
//        // 将顺位第i个与倒数第i个交换
//        for (int i = 0; i < len / 2; ++i) {
//            temp = dest[i];
//            dest[i] = dest[len - i - 1];
//            dest[len - i - 1] = temp;
//        }
//
//        return dest;
    }

    /**
     * 4字节数组转为单精度浮点
     *
     * @param bytes 4字节数组
     * @return float
     */
    public static float byteToFloat(byte[] bytes) {
        int l;
        l = bytes[0];
        l &= 0xff;
        l |= ((long) bytes[1] << 8);
        l &= 0xffff;
        l |= ((long) bytes[2] << 16);
        l &= 0xffffff;
        l |= ((long) bytes[3] << 24);
        return Float.intBitsToFloat(l);
    }
}
