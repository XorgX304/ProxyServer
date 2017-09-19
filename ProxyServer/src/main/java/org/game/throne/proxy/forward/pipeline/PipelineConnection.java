package org.game.throne.proxy.forward.pipeline;

/**
 * Created by lvtu on 2017/9/14.
 */
public interface PipelineConnection {

    void write(Object msg);

    void close();

    /**
     * 可以用来发送request
     */
    void ready();

    void awaitReady();

    boolean isReady();

    boolean complete();

    int pipeport();
}
