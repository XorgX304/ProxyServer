package org.game.throne.proxy.forward.codec;

import io.netty.channel.ChannelHandlerContext;
import org.game.throne.proxy.forward.AbstractChannelHandler;
import org.game.throne.proxy.forward.pipeline.PipelineConnection;
import org.game.throne.proxy.forward.pipeline.PipelineConnectionProvider;

/**
 * Created by lvtu on 2017/9/15.
 */
public class CommandParser {

    public static void read(Phase state, Object msg, ChannelHandlerContext ctx, AbstractChannelHandler handler) {
        switch (state) {
            case AWATING_COMMAND:
                CommandHeader commandHeader = (CommandHeader) msg;
                PipelineConnection pipelineConnection;
                switch (commandHeader.getCommand()) {
                    case HTTP:
                        pipelineConnection = PipelineConnectionProvider.existedPipeline(ctx, handler.getRelationKeeper());
                        handler.setPipelineConnection(pipelineConnection);
                        handler.setState(Phase.AWATING_HTTP_CONTENT);
                    case PIPELINE:
                        pipelineConnection = PipelineConnectionProvider.openPipeline1(ctx, handler.getRelationKeeper(), commandHeader.getPipePort());
                        handler.setPipelineConnection(pipelineConnection);
                        ctx.writeAndFlush(new CommandHeader(CommandEnum.ACK_PIPELINE, commandHeader.getPipePort()));
                        break;
                    case ACK_PIPELINE:
                        PipelineConnectionProvider.existedPipeline(commandHeader.getPipePort()).ready();
                        break;
                    case CLOSE_PIPELINE:
                        handler.getPipelineConnection().close();
                        break;
                }
                break;
            case AWATING_HTTP_CONTENT:
                handler.getPipelineConnection().write(msg);
                break;
        }
        throw new RuntimeException("no command.");
    }

}
