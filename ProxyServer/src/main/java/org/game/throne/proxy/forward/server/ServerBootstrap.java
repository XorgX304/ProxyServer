package org.game.throne.proxy.forward.server;

import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.game.throne.proxy.forward.ChannelHandlerFactory;
import org.game.throne.proxy.forward.relation.RelationKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lvtu on 2017/9/4.
 */
public class ServerBootstrap {

    private final static Logger logger = LoggerFactory.getLogger(ServerBootstrap.class);

    public static void main(String[] args) {
        int serverListenerPort = 8083;
        int channelListenerPort = 8082;

        RelationKeeper relationKeeper = new RelationKeeper();
        ChannelServerHandler channelServerHandler = new ChannelServerHandler(relationKeeper);
        ServerHandler serverHandler = new ServerHandler(channelServerHandler,relationKeeper);
        new MServer(serverListenerPort).withHandlerFactory(new ChannelHandlerFactory(HttpRequestDecoder.class), new ChannelHandlerFactory(HttpResponseEncoder.class)).withHandler(serverHandler).run().releaseOnClose();
        new MServer(channelListenerPort).withHandlerFactory(new ChannelHandlerFactory(HttpRequestEncoder.class), new ChannelHandlerFactory(HttpResponseDecoder.class)).withHandler(channelServerHandler).run().releaseOnClose();
        logger.info("servers started up.");
    }
}
