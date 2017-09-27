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
        TThreadedSelectorServer.Args selectorServerArgs = null;
        try {
            // 解决并发连接数上限默认只有50的问题
            TNonblockingServerSocket.NonblockingAbstractServerSocketArgs serverSocketArgs = new TNonblockingServerSocket.NonblockingAbstractServerSocketArgs();
            serverSocketArgs.backlog(1000);// 1k个连接
            serverSocketArgs.port(url.getPort());
            serverSocketArgs.clientTimeout(8000);// 10秒超时

            TNonblockingServerSocket transport = new TNonblockingServerSocket(serverSocketArgs);
            // 这里使用可配置，结合spring
            selectorServerArgs = new TThreadedSelectorServer.Args(transport);
            selectorServerArgs.workerThreads(20); // 工作线程
            selectorServerArgs.selectorThreads(1); // selector 线程，一般1个足够用了
            // selector线程等待请求队列，业务方是期望快速返回的，服务端繁忙时客户端也不会一直等下去，所以不需设置太多
            selectorServerArgs.acceptQueueSizePerThread(20);
            selectorServerArgs.processor(multiplexedProcessor);
            selectorServerArgs.transportFactory(new TFramedTransport.Factory());
            selectorServerArgs.protocolFactory(new TCompactProtocol.Factory());
        } catch (Exception e) {
            LOGGER.error("init thrift server error.", e);
        }

        if (selectorServerArgs == null) {
            LOGGER.error("Fail to create thrift server(" + url + ") due to null args");
            throw new RpcException("Fail to create thrift server(" + url + ") due to null args");
        }
        thriftServer = new TThreadedSelectorServer(selectorServerArgs);
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
        String iFace = "$Iface";
        String processor = "$Processor";
        String typeName = type.getName();
        if (typeName.endsWith(iFace)) {
            String processorClsName = typeName.substring(0, typeName.indexOf(iFace)) + processor;
            try {
                Class<?> clazz = Class.forName(processorClsName);
                Constructor constructor = clazz.getConstructor(type);
                try {
                    tprocessor = (TProcessor) constructor.newInstance(impl);
                    // 共用端口
                    multiplexedProcessor.registerProcessor(typeName, tprocessor);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    throw new RpcException("Fail to create thrift server(" + url + ") : " + e.getMessage(), e);
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                throw new RpcException("Fail to create thrift server(" + url + ") : " + e.getMessage(), e);
            }
        }

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
