package org.game.throne.proxy.forward.pipeline;

import org.game.throne.proxy.forward.codec.Phase;

/**
 * Created by lvtu on 2017/9/14.
 */
public interface PipelineConnection {

    void write(Object msg);

    void close();

    void done();

    void awaitDone();

    boolean isDone();//是否建立起pipeline

    Phase lifecycle();

    boolean complete();
}
