package org.game.throne.proxy.forward.pipeline;

import io.netty.channel.ChannelHandlerContext;
import org.game.throne.proxy.forward.relation.Relation;
import org.game.throne.proxy.forward.relation.RelationKeeper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lvtu on 2017/9/14.
 */
public class PipelineConnectionProvider {

    private static Map<Relation, PipelineConnection> relationToConnection = new ConcurrentHashMap<>();
    private static Map<Integer, PipelineConnection> portToConnection = new ConcurrentHashMap<>();

    public static PipelineConnection openPipeline(ChannelHandlerContext leftOrRightCtx, RelationKeeper relationKeeper) {
        PipelineConnectionImpl pipeline = new PipelineConnectionImpl(leftOrRightCtx, relationKeeper);
        pipeline.bindPipeport();
        pipeline.awaitReady();
        relationToConnection.put(relationKeeper.matchedRelation(leftOrRightCtx), pipeline);
        portToConnection.put(pipeline.getPipeport(), pipeline);
        return pipeline;
    }

    public static PipelineConnection openPipeline1(ChannelHandlerContext leftOrRightCtx, RelationKeeper relationKeeper, int pipeport) {
        PipelineConnectionImpl pipeline = new PipelineConnectionImpl(leftOrRightCtx, relationKeeper);
        pipeline.bindPipeport(pipeport);
        relationToConnection.put(relationKeeper.matchedRelation(leftOrRightCtx), pipeline);
        portToConnection.put(pipeline.getPipeport(), pipeline);
        return pipeline;
    }

    public static PipelineConnection existedPipeline(ChannelHandlerContext leftOrRightCtx, RelationKeeper relationKeeper) {
        Relation relation = relationKeeper.matchedRelation(leftOrRightCtx);
        return relationToConnection.get(relation);
    }

    public static PipelineConnection existedPipeline(int pipeport) {
        return portToConnection.get(pipeport);
    }

}
