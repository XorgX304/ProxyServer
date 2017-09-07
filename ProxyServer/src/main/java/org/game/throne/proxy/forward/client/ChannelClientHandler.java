package org.game.throne.proxy.forward.client;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCounted;
import org.game.throne.proxy.forward.ChannelRelationEvent;
import org.game.throne.proxy.forward.relation.RelationKeeper;
import org.game.throne.proxy.forward.relation.RelationProcess;
import org.game.throne.proxy.forward.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lvtu on 2017/9/6.
 */
@ChannelHandler.Sharable
public class ChannelClientHandler extends SimpleChannelInboundHandler implements RelationProcess {

    private final static Logger logger = LoggerFactory.getLogger(ChannelClientHandler.class);

    private LocalHandler localHandler;

    private RelationKeeper relationKeeper;

    public ChannelClientHandler(LocalHandler localHandler) {
        this.localHandler = localHandler;
        localHandler.clientHandler = this;
    }

    public ChannelClientHandler(LocalHandler localHandler,RelationKeeper relationKeeper) {
        this(localHandler);
        this.relationKeeper = relationKeeper;
    }

    private ChannelHandlerContext localConext(ChannelHandlerContext ctx) {
        if(relationKeeper.exists(ctx)){
            return relationKeeper.matchedContext(ctx);
        }
        ChannelHandlerContext localContext = localHandler.getUsableContext();;
        relationKeeper.addRelation(ctx,localContext);
        return localContext;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("connected, handler:{},channel:{}", ctx, ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        writeToNextChannel(ctx, msg);
        if (msg instanceof LastHttpContent) {
            flushToNextChannel(ctx);
        }
    }

    private void writeToNextChannel(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof ReferenceCounted) {
            logger.debug("Retaining reference counted message");
            ((ReferenceCounted) msg).retain();
        }
        logger.info("start to get next context.");
        ChannelHandlerContext nextContext = localConext(ctx);
        logger.info("data arrived. from channel:{},start to write into next channel:{}, msg:{}", ctx.channel(), nextContext.channel(), msg);
        nextContext.write(msg);
    }

    private void flushToNextChannel(ChannelHandlerContext ctx) {
        ChannelHandlerContext localConext = localConext(ctx);
        logger.info("flush data. from channel:{},start to flush into next channel:{}", ctx.channel(), localConext.channel());
        localConext.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        requestBreakRelation(ctx);
        ctx.writeAndFlush(HttpUtil.errorResponse());
        cause.printStackTrace();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        requestBreakRelation(ctx);
        super.channelInactive(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt.equals(ChannelRelationEvent.BREAK)) {
            responseBreakRelation(ctx);
            return;
        }
        if(evt.equals(ChannelRelationEvent.RESPONSE_FINISHED)){
            requestBreakRelation(ctx);
            return;
        }
    }

    @Override
    public void requestBreakRelation(ChannelHandlerContext ctx){
        relationKeeper.breakRelation(ctx);
    }

    @Override
    public void responseBreakRelation(ChannelHandlerContext ctx){

    }

}
