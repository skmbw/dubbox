package com.alibaba.dubbo.common.serialize.support.protostuff;

import com.alibaba.dubbo.common.serialize.ObjectOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 基于protostuff的序列化
 *
 * @author yinlei
 * @since 2017/10/13 9:28
 */
public class ProtostuffObjectOutput implements ObjectOutput {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtostuffObjectOutput.class);

    private OutputStream output;

    public ProtostuffObjectOutput(OutputStream output) {
        this.output = output;
    }

    @Override
    public void writeBool(boolean v) throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("writeBool，数据是=[{}].", v);
        }
        output.write(ProtoUtils.toBytes(v));
    }

    @Override
    public void writeByte(byte v) throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("writeByte，数据是=[{}].", v);
        }
        output.write(ProtoUtils.toBytes(v));
    }

    @Override
    public void writeShort(short v) throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("writeShort，数据是=[{}].", v);
        }
        output.write(ProtoUtils.toBytes(v, 11));
    }

    @Override
    public void writeInt(int v) throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("writeInt，数据是=[{}].", v);
        }
        output.write(ProtoUtils.toBytes(v, 4));
    }

    @Override
    public void writeLong(long v) throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("writeLong，数据是=[{}].", v);
        }
        output.write(ProtoUtils.toBytes(v, 5));
    }

    @Override
    public void writeFloat(float v) throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("writeFloat，数据是=[{}].", v);
        }
        output.write(ProtoUtils.toBytes(v, 10));
    }

    @Override
    public void writeDouble(double v) throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("writeDouble，数据是=[{}].", v);
        }
        output.write(ProtoUtils.toBytes(v, 6));
    }

    @Override
    public void writeUTF(String v) throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("writeUTF，数据是=[{}].", v);
        }
        output.write(ProtoUtils.toBytes(v, 12));
    }

    @Override
    public void writeBytes(byte[] v) throws IOException {
        if (v == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("writeByte，数据为空，返回-1.");
            }
            output.write(-1);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("writeBytes，数据长度是=[{}]，第一个字节是=[{}].", v.length, v[0]);
            }
            output.write(v);
        }
    }

    @Override
    public void writeBytes(byte[] v, int off, int len) throws IOException {
        if (v == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("writeByte，带offset偏移量，数据为空，返回-1.");
            }
            output.write(-1);
        } else {
            byte[] v2 = new byte[len];
            System.arraycopy(v, off, v2, 0, len);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("writeByte，带offset偏移量，数据长度是=[{}]，第一个字节是=[{}].", v2.length, v2[0]);
            }
            output.write(v2);
        }
    }

    @Override
    public void flushBuffer() throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("flushBuffer 刷新数据.");
        }
        output.flush();
    }

    @Override
    public void writeObject(Object obj) throws IOException {
        if (obj == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("writeObject 数据为空.");
            }
            return;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("writeObject 数据类型是=[{}].", obj.getClass().getName());
        }
        byte[] result = ProtoUtils.toBytes(obj);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("writeObject 序列化后，数据长度是=[{}]，第一个字节是=[{}].", result.length, result[0]);
        }
        output.write(result);
    }
}
