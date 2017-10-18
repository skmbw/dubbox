package com.alibaba.dubbo.common.serialize.support.protostuff;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.serialize.OptimizedSerialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 基于protostuff的序列化和反序列
 *
 * @author yinlei
 * @since 2017/10/13 9:27
 */
public class ProtostuffSerialization implements OptimizedSerialization {
    @Override
    public byte getContentTypeId() {
        return 11;
    }

    @Override
    public String getContentType() {
        return "x-application/protostuff";
    }

    @Override
    public ObjectOutput serialize(URL url, OutputStream output) throws IOException {
//        return new ProtostuffObjectOutput(output);
        return new ProtoOutput(output);
    }

    @Override
    public ObjectInput deserialize(URL url, InputStream input) throws IOException {
//        return new ProtostuffObjectInput(input);
        return new ProtoInput(input);
    }
}
