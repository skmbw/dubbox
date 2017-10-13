package com.alibaba.dubbo.common.serialize.support.kryo;

import com.esotericsoftware.kryo.Kryo;

/**
 * Referencing subclass PooledKryoFactory from superclass KryoFactory initializer might lead to class loading deadlock
 *
 * @author yinlei
 * @since 17-10-13 下午8:18
 */
public class KryoUtils {
    private static final KryoFactory FACTORY = new PooledKryoFactory();

    public static Kryo getKryo() {
        return FACTORY.getKryo();
    }

    public static void returnKryo(Kryo kryo) {
        FACTORY.returnKryo(kryo);
    }
}
