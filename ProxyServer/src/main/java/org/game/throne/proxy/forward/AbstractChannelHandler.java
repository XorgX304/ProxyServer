package org.game.throne.proxy.forward;

import io.netty.channel.SimpleChannelInboundHandler;
import org.game.throne.proxy.forward.codec.Phase;
import org.game.throne.proxy.forward.pipeline.PipelineConnection;
import org.game.throne.proxy.forward.relation.RelationKeeper;

/**
 * Created by lvtu on 2017/9/15.
 */
public abstract class AbstractChannelHandler extends SimpleChannelInboundHandler {

    protected RelationKeeper relationKeeper;

    protected Phase state = Phase.AWATING_COMMAND;

    protected PipelineConnection pipelineConnection;

    public RelationKeeper getRelationKeeper() {
        return relationKeeper;
    }

    public void setRelationKeeper(RelationKeeper relationKeeper) {
        this.relationKeeper = relationKeeper;
    }

    public Phase getState() {
        return state;
    }

    public void setState(Phase state) {
        this.state = state;
    }

    public PipelineConnection getPipelineConnection() {
        return pipelineConnection;
    }

    public void setPipelineConnection(PipelineConnection pipelineConnection) {
        this.pipelineConnection = pipelineConnection;
    }
}
