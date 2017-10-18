package com.alibaba.dubbo.common.serialize.support.protostuff;

/**
 * protostuff不能直接序列化数组，包装一下。
 *
 * @author yinlei
 * @since 17-10-18 下午9:18
 */
public class WrapArray {
    private Object[] array;

    public WrapArray() {
    }

    public WrapArray(Object[] array) {
        this.array = array;
    }

    public Object[] getArray() {
        return array;
    }

    public void setArray(Object[] array) {
        this.array = array;
    }
}
