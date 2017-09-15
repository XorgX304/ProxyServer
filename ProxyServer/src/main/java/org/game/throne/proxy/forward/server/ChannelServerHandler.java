package org.game.throne.proxy.forward.server;

import io.netty.channel.ChannelHandlerContext;
import org.game.throne.proxy.forward.AbstractChannelHandler;
import org.game.throne.proxy.forward.client.LocalHandler;
import org.game.throne.proxy.forward.codec.CommandParser;
import org.game.throne.proxy.forward.relation.RelationKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lvtu on 2017/9/6.
 */
public class ChannelServerHandler extends AbstractChannelHandler {

    private final static Logger logger = LoggerFactory.getLogger(LocalHandler.class);

    public ChannelServerHandler(RelationKeeper relationKeeper) {
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
        CommandParser.read(state, msg, ctx, this);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        pipelineConnection.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        relationKeeper.contextObjectPool.remove(ctx);
        pipelineConnection.close();
        super.channelInactive(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
    }

}
