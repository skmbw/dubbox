package com.alibaba.dubbo.rpc.protocol.thrift2;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.protocol.AbstractProxyProtocol;
import org.apache.thrift.TException;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 为dubbox-rpc添加"原生thrift"支持
 *
 * @author yinlei
 * @since 2017-9-27
 */
public class Thrift2Protocol extends AbstractProxyProtocol {
    public static final int DEFAULT_PORT = 33208;
    private static final Logger LOGGER = LoggerFactory.getLogger(Thrift2Protocol.class);

    private volatile AtomicBoolean init = new AtomicBoolean(false);
    private TMultiplexedProcessor multiplexedProcessor;
    private TServer thriftServer;

    public int getDefaultPort() {
        return DEFAULT_PORT;
    }

    public Thrift2Protocol() {
        super(TException.class, RpcException.class);
        multiplexedProcessor = new TMultiplexedProcessor();
    }

    private void init (URL url) {
        TThreadedSelectorServer.Args tArgs = null;
        try {
            // 解决并发连接数上限默认只有50的问题
            TNonblockingServerSocket.NonblockingAbstractServerSocketArgs args = new TNonblockingServerSocket.NonblockingAbstractServerSocketArgs();
            args.backlog(1000);// 1k个连接
            args.port(url.getPort());
            args.clientTimeout(8000);// 10秒超时

            TNonblockingServerSocket transport = new TNonblockingServerSocket(args);

            tArgs = new TThreadedSelectorServer.Args(transport);
            tArgs.workerThreads(200);
            tArgs.selectorThreads(4);
            tArgs.acceptQueueSizePerThread(256);
            tArgs.processor(multiplexedProcessor);
            tArgs.transportFactory(new TFramedTransport.Factory());
            tArgs.protocolFactory(new TCompactProtocol.Factory());
        } catch (Exception e) {
            LOGGER.error("init thrift server error.", e);
        }

        if (tArgs == null) {
            LOGGER.error("Fail to create thrift server(" + url + ") due to null args");
            throw new RpcException("Fail to create thrift server(" + url + ") due to null args");
        }
        thriftServer = new TThreadedSelectorServer(tArgs);
        Thread thread = new Thread(() -> {
            LOGGER.info("Start Thrift ThreadedSelectorServer");
            thriftServer.serve();
            LOGGER.info("Thrift ThreadedSelectorServer started.");
        });
        thread.start();
    }

    @Override
    protected <T> Runnable doExport(T impl, Class<T> type, URL url)
            throws RpcException {
        LOGGER.info("impl => " + impl.getClass());
        LOGGER.info("type => " + type.getName());
        LOGGER.info("url => " + url);
        return exportThreadedSelectorServer(impl, type, url);
    }

    @Override
    protected <T> T doRefer(Class<T> type, URL url) throws RpcException {
        LOGGER.info("type => " + type.getName());
        LOGGER.info("url => " + url);
        return doReferFrameAndCompact(type, url);
    }

    private <T> Runnable exportThreadedSelectorServer(T impl, Class<T> type, URL url)
            throws RpcException {
        if (init.compareAndSet(false ,true)) {
            init(url);
        }
        TProcessor tprocessor;
//        TThreadedSelectorServer.Args tArgs = null;
        String iFace = "$Iface";
        String processor = "$Processor";
        String typeName = type.getName();
//        TNonblockingServerSocket transport;
        if (typeName.endsWith(iFace)) {
            String processorClsName = typeName.substring(0, typeName.indexOf(iFace)) + processor;
            try {
                Class<?> clazz = Class.forName(processorClsName);
                Constructor constructor = clazz.getConstructor(type);
                try {
                    tprocessor = (TProcessor) constructor.newInstance(impl);
                    // 共用端口
                    multiplexedProcessor.registerProcessor(typeName, tprocessor);

                    // 解决并发连接数上限默认只有50的问题
//                    TNonblockingServerSocket.NonblockingAbstractServerSocketArgs args = new TNonblockingServerSocket.NonblockingAbstractServerSocketArgs();
//                    args.backlog(1000);// 1k个连接
//                    args.port(url.getPort());
//                    args.clientTimeout(10000);// 10秒超时
//
//                    transport = new TNonblockingServerSocket(args);
//
//                    tArgs = new TThreadedSelectorServer.Args(transport);
//                    tArgs.workerThreads(200);
//                    tArgs.selectorThreads(4);
//                    tArgs.acceptQueueSizePerThread(256);
//                    tArgs.processor(tprocessor);
//                    tArgs.transportFactory(new TFramedTransport.Factory());
//                    tArgs.protocolFactory(new TCompactProtocol.Factory());
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    throw new RpcException("Fail to create thrift server(" + url + ") : " + e.getMessage(), e);
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                throw new RpcException("Fail to create thrift server(" + url + ") : " + e.getMessage(), e);
            }
        }

//        if (tArgs == null) {
//            LOGGER.error("Fail to create thrift server(" + url + ") due to null args");
//            throw new RpcException("Fail to create thrift server(" + url + ") due to null args");
//        }
//        final TServer thriftServer = new TThreadedSelectorServer(tArgs);
//
//        Thread thread = new Thread(() -> {
//            LOGGER.info("Start Thrift ThreadedSelectorServer");
//            thriftServer.serve();
//            LOGGER.info("Thrift ThreadedSelectorServer started.");
//        });
//        thread.start();

        return () -> {
            try {
                LOGGER.info("Close Thrift NonblockingServer");
                thriftServer.stop();
            } catch (Throwable e) {
                LOGGER.warn(e.getMessage(), e);
            }
        };
    }

    private <T> T doReferFrameAndCompact(Class<T> type, URL url) throws RpcException {
        try {
            TSocket tSocket;
            TTransport transport;
            TProtocol protocol;
            T thriftClient = null;
            String iFace = "$Iface";
            String client = "$Client";

            String typeName = type.getName();
            if (typeName.endsWith(iFace)) {
                String clientClsName = typeName.substring(0, typeName.indexOf(iFace)) + client;
                Class<T> clazz = (Class) Class.forName(clientClsName);
                Constructor<T> constructor = clazz.getConstructor(TProtocol.class);
                try {
                    tSocket = new TSocket(url.getHost(), url.getPort());
                    transport = new TFramedTransport(tSocket);
                    protocol = new TCompactProtocol(transport);
                    // 共用端口
                    TMultiplexedProtocol multiplexedProtocol = new TMultiplexedProtocol(protocol, typeName);
                    thriftClient = constructor.newInstance(multiplexedProtocol);
                    transport.open();
                    LOGGER.info("thrift client opened for service(" + url + ")");
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    throw new RpcException("Fail to create remote client:" + e.getMessage(), e);
                }
            }
            return thriftClient;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RpcException("Fail to create remote client for service(" + url + "): " + e.getMessage(), e);
        }
    }
}
