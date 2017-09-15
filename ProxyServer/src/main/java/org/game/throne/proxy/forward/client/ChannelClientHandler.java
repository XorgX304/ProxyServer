package org.game.throne.proxy.forward.client;

import io.netty.channel.ChannelHandlerContext;
import org.game.throne.proxy.forward.AbstractChannelHandler;
import org.game.throne.proxy.forward.codec.CommandParser;
import org.game.throne.proxy.forward.relation.RelationKeeper;
import org.game.throne.proxy.forward.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lvtu on 2017/9/6.
 */
public class ChannelClientHandler extends AbstractChannelHandler {

    private final static Logger logger = LoggerFactory.getLogger(ChannelClientHandler.class);

    private ClientFactory factory;

    public ChannelClientHandler(RelationKeeper relationKeeper, ClientFactory factory) {
        this.relationKeeper = relationKeeper;
        this.factory = factory;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("connected, handler:{},channel:{}", ctx, ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        CommandParser.read(state, msg, ctx, this);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        pipelineConnection.close();
        ctx.writeAndFlush(HttpUtil.errorResponse(cause.getMessage()));
        cause.printStackTrace();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        pipelineConnection.close();
        factory.create(relationKeeper);
        super.channelInactive(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
    }

}
