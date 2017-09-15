package org.game.throne.proxy.forward.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpRequest;
import org.game.throne.proxy.forward.client.ChannelClientHandler;
import org.game.throne.proxy.forward.codec.CommandHeader;
import org.game.throne.proxy.forward.codec.Phase;
import org.game.throne.proxy.forward.pipeline.PipelineConnection;
import org.game.throne.proxy.forward.pipeline.PipelineConnectionProvider;
import org.game.throne.proxy.forward.relation.RelationKeeper;
import org.game.throne.proxy.forward.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lvtu on 2017/9/6.
 */
public class ServerHandler extends SimpleChannelInboundHandler{

    private final static Logger logger = LoggerFactory.getLogger(ChannelClientHandler.class);

    private RelationKeeper relationKeeper;

    private PipelineConnection pipelineConnection;

    private Phase state = Phase.AWATING_COMMAND;

    public ServerHandler(RelationKeeper relationKeeper) {
        this.relationKeeper = relationKeeper;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("connected, handler:{},channel:{}", ctx, ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (pipelineConnection == null) {
            pipelineConnection = PipelineConnectionProvider.openPipeline(ctx, relationKeeper);
        }
        if(msg instanceof HttpRequest){
            pipelineConnection.write(CommandHeader.HTTP_COMMAND);
        }
        pipelineConnection.write(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.writeAndFlush(HttpUtil.errorResponse(cause.getMessage()));
        pipelineConnection.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        pipelineConnection.close();
        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

    }
}
