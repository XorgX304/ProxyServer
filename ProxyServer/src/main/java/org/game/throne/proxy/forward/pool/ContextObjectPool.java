package org.game.throne.proxy.forward.pool;

import org.apache.commons.pool2.ObjectPool;

/**
 * Created by lvtu on 2017/9/5.
 */
public interface ContextObjectPool<T> extends ObjectPool<T> {
    void addObject(T ctx) throws Exception;
}
