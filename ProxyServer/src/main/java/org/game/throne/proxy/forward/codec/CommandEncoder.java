package org.game.throne.proxy.forward.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * Created by lvtu on 2017/9/14.
 */
public class CommandEncoder extends MessageToMessageEncoder<CommandHeader> {

    @Override
    public boolean acceptOutboundMessage(Object msg) throws Exception {
        return msg instanceof CommandHeader;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, CommandHeader msg, List<Object> out) throws Exception {
        ByteBuf buffer = ctx.alloc().buffer(8);
        buffer.writeByte(msg.getCommand().getCommandCode());
        buffer.writeByte(msg.getPipePort());
        out.add(buffer);
    }
}
