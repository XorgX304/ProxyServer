package org.game.throne.proxy.forward.pool;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by lvtu on 2017/9/5.
 */
public class ContextPooledObjectFactory<T> extends BasePooledObjectFactory<T> {

    private final static Logger logger = LoggerFactory.getLogger(ContextPooledObjectFactory.class);

    private BlockingQueue<T> queue = new LinkedBlockingDeque();

    public BlockingQueue<T> getQueue() {
        return queue;
    }

    @Override
    public T create() throws Exception {
        logger.debug("create pooled object.");
        return queue.take();
    }

    @Override
    public PooledObject wrap(T obj) {
        return new DefaultPooledObject(obj);
    }
}
