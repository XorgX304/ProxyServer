package org.game.throne.proxy.forward.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpRequest;
import org.game.throne.proxy.forward.codec.CommandHeader;
import org.game.throne.proxy.forward.pipeline.PipelineConnection;
import org.game.throne.proxy.forward.pipeline.PipelineConnectionProvider;
import org.game.throne.proxy.forward.relation.RelationKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lvtu on 2017/9/6.
 */
public class LocalHandler extends SimpleChannelInboundHandler {

    private final static Logger logger = LoggerFactory.getLogger(LocalHandler.class);

    private RelationKeeper relationKeeper;

    private PipelineConnection pipelineConnection;

    public LocalHandler(RelationKeeper relationKeeper) {
        this.relationKeeper = relationKeeper;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("connected, handler:{},channel:{}", ctx, ctx.channel());
        //保存这个ChannelHandlerContext
        relationKeeper.contextObjectPool.offer(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (pipelineConnection == null) {
            pipelineConnection = PipelineConnectionProvider.existedPipeline(ctx, relationKeeper);
        }
        if(msg instanceof HttpRequest){
            pipelineConnection.write(CommandHeader.HTTP_COMMAND);
        }
        pipelineConnection.write(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        pipelineConnection.close();
        relationKeeper.contextObjectPool.remove(ctx);
        cause.printStackTrace();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("{} is closed.", ctx);
        pipelineConnection.close();
        relationKeeper.contextObjectPool.remove(ctx);
        super.channelInactive(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
    }


}
