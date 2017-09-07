package org.game.throne.proxy.forward.pool;

import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lvtu on 2017/9/5.
 */
public class ContextObjectPoolImpl<T> extends GenericObjectPool<T> implements ContextObjectPool<T> {

    private final static Logger logger = LoggerFactory.getLogger(ContextObjectPoolImpl.class);

    public ContextObjectPoolImpl(ContextPooledObjectFactory factory) {
        super(factory);
    }

    public ContextObjectPoolImpl(ContextPooledObjectFactory factory, GenericObjectPoolConfig config) {
        super(factory, config);
    }

    public ContextObjectPoolImpl(ContextPooledObjectFactory factory, GenericObjectPoolConfig config, AbandonedConfig abandonedConfig) {
        super(factory, config, abandonedConfig);
    }

    @Override
    public void addObject(T ctx) throws Exception {
        ContextPooledObjectFactory factory = (ContextPooledObjectFactory) getFactory();
        logger.debug("add object. {}", ctx);
        factory.getQueue().offer(ctx);
        //不可调用这个函数,会有问题,如
        // t1 执行  add  create
        // t2 执行  create
        //那么t2的create会从t1的queue(通过t1的add添加)里取得,然后t1的create就无法继续执行了,会阻塞t1线程(会阻塞NioEventLoop的后续task)
//        super.addObject();
    }
}
