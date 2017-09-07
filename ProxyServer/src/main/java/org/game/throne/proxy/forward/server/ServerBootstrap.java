package org.game.throne.proxy.forward.server;

import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.game.throne.proxy.forward.ChannelHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lvtu on 2017/9/4.
 */
public class ServerBootstrap {

    private final static Logger logger = LoggerFactory.getLogger(ServerBootstrap.class);

    public static void main(String[] args) {
        ChannelServerHandler channelServerHandler = new ChannelServerHandler();
        ServerHandler serverHandler = new ServerHandler(channelServerHandler);
        new MServer(8083).withHandlerFactory(new ChannelHandlerFactory(HttpRequestDecoder.class), new ChannelHandlerFactory(HttpResponseEncoder.class)).withHandler(serverHandler).run();
        new MServer(8082).withHandlerFactory(new ChannelHandlerFactory(HttpRequestEncoder.class), new ChannelHandlerFactory(HttpResponseDecoder.class)).withHandler(channelServerHandler).run();
        logger.info("servers started up.");
    }
}
