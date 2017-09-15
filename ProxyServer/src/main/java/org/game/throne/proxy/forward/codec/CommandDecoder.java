package org.game.throne.proxy.forward.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created by lvtu on 2017/9/14.
 */
public class CommandDecoder extends ByteToMessageDecoder {

    private boolean parsedCommand = false;

    private byte[] commandHeader = new byte[8];
    private int position = 0;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (!parsedCommand && in.isReadable()) {
            if (position < commandHeader.length) {
                int min = Math.min(commandHeader.length - position, in.readableBytes());
                in.readBytes(commandHeader, position, min);
                position = min;

                if (position == commandHeader.length) {
                    int command = commandHeader[0] << 24 & commandHeader[1] << 16 & commandHeader[2] << 8 & commandHeader[3];
                    int pipePort = commandHeader[4] << 24 & commandHeader[5] << 16 & commandHeader[6] << 8 & commandHeader[7];
                    CommandEnum commandEnum = CommandEnum.of(command);
                    CommandHeader commandHeader = new CommandHeader(commandEnum, pipePort);
                    out.add(commandHeader);
                    out.add(in);
                    parsedCommand = true;
                    this.commandHeader = null;
                }
            }
        }
    }
}
