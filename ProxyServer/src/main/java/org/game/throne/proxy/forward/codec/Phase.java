package org.game.throne.proxy.forward.codec;

/**
 * Created by lvtu on 2017/9/15.
 */
public enum Phase {
    /**
     *
     */
    AWATING_COMMAND,
    AWATING_HTTP_CONTENT,

    /**
     * PIPELINE LIFECYCLE
     */
    PIPELINE_FINISHED, REQUEST, COMPLETE, CLOSED;
}
