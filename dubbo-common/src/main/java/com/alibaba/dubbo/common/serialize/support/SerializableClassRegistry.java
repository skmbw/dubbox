package com.alibaba.dubbo.common.serialize.support;

import java.util.Set;
import java.util.TreeSet;

/**
 * @author lishen
 */
public abstract class SerializableClassRegistry {

    private static final Set<Class> registrations = new TreeSet<>((o1, o2) -> o1.getName().compareTo(o2.getName()));

    /**
     * only supposed to be called at startup time
     */
    public static void registerClass(Class clazz) {
        registrations.add(clazz);
    }

    public static Set<Class> getRegisteredClasses() {
        return registrations;
    }
}
