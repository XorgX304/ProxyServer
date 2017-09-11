package org.game.throne.proxy.forward.server;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.ChannelInputShutdownEvent;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCounted;
import org.game.throne.proxy.forward.ChannelRelationEvent;
import org.game.throne.proxy.forward.TimeoutException;
import org.game.throne.proxy.forward.client.ChannelClientHandler;
import org.game.throne.proxy.forward.relation.RelationKeeper;
import org.game.throne.proxy.forward.relation.RelationProcess;
import org.game.throne.proxy.forward.util.FutureUtil;
import org.game.throne.proxy.forward.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lvtu on 2017/9/6.
 */
@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler implements RelationProcess {

    private final static Logger logger = LoggerFactory.getLogger(ChannelClientHandler.class);

    private ChannelServerHandler channelServerHandler;

    private RelationKeeper relationKeeper;

    public ServerHandler(ChannelServerHandler channelServerHandler) {
        this.channelServerHandler = channelServerHandler;
        channelServerHandler.serverHandler = this;
    }

    public ServerHandler(ChannelServerHandler channelServerHandler, RelationKeeper relationKeeper) {
        this(channelServerHandler);
        this.relationKeeper = relationKeeper;
    }

    private ChannelHandlerContext channelServerConext(ChannelHandlerContext ctx) {
        if (relationKeeper.exists(ctx)) {
            return relationKeeper.matchedContext(ctx);
        }
        ChannelHandlerContext channelServerHandlerUsableContext = channelServerHandler.getAvailableContext();
        if (channelServerHandlerUsableContext == null) {
            //因为超时没有获取到
            return null;
        }
        relationKeeper.addRelation(ctx, channelServerHandlerUsableContext);
        return channelServerHandlerUsableContext;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("connected, handler:{},channel:{}", ctx, ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        writeToNextChannel(ctx, msg);
    }

    private void writeToNextChannel(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof ReferenceCounted) {
            logger.debug("Retaining reference counted message");
            ((ReferenceCounted) msg).retain();
        }
        logger.info("start to get next context.");
        ChannelHandlerContext channelServerConext = channelServerConext(ctx);
        if (channelServerConext == null) {
            //因为超时等原因,没有获取到可用的context。
            ctx.pipeline().fireExceptionCaught(new TimeoutException("server timeout"));
            return;
        }
        logger.info("data arrived. from channel:{},start to write into next channel:{}, msg:{}", ctx.channel(), channelServerConext.channel(), msg);
        channelServerConext.write(msg).addListener(FutureUtil.errorLogListener(ctx));
        if (msg instanceof LastHttpContent) {
            flushToNextChannel(ctx);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.writeAndFlush(HttpUtil.errorResponse(cause.getMessage()));
        requestBreakRelation(ctx);
    }

    private void flushToNextChannel(ChannelHandlerContext ctx) {
        ChannelHandlerContext channelServerConext = channelServerConext(ctx);
        logger.info("flush data. from channel:{},start to flush into next channel:{}", ctx.channel(), channelServerConext.channel());
        channelServerConext.flush();
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
        if (ChannelInputShutdownEvent.INSTANCE.equals(evt)) {
            logger.debug("ChannelInputShutdownEvent received.");
            requestBreakRelation(ctx);
            return;
        }
        if (ChannelRelationEvent.RESPONSE_FINISHED.equals(evt)) {
            logger.debug("RESPONSE_FINISHED Event received.");
            requestBreakRelation(ctx);
            return;
        }
        if (ChannelRelationEvent.BREAK.equals(evt)) {
            responseBreakRelation(ctx);
        }
    }

    @Override
    public void requestBreakRelation(ChannelHandlerContext ctx) {
        relationKeeper.breakRelation(ctx);
    }

    @Override
    public void responseBreakRelation(ChannelHandlerContext ctx) {

    }
}
