package org.game.throne.proxy.forward.server;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.game.throne.proxy.forward.ChannelHandlerFactory;
import org.game.throne.proxy.forward.codec.CommandDecoder;
import org.game.throne.proxy.forward.codec.CommandEncoder;
import org.game.throne.proxy.forward.relation.RelationKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by lvtu on 2017/9/4.
 */
public class ServerBootstrap {

    private final static Logger logger = LoggerFactory.getLogger(ServerBootstrap.class);

    public static void main(String[] args) {
        int serverListenerPort = 8083;
        int channelListenerPort = 8082;

        File trustCertCollectionFile = new File("/Users/lvtu/workspace/english/cert/cer.pem");

        RelationKeeper relationKeeper = RelationKeeper.forServer();

        new MServer(serverListenerPort)
                .withHandlerFactory(new ChannelHandlerFactory(CommandDecoder.class), new ChannelHandlerFactory(CommandEncoder.class), new ChannelHandlerFactory(HttpRequestDecoder.class), new ChannelHandlerFactory(HttpResponseEncoder.class))
                .withHandlerFactory(new ChannelHandlerFactory(ServerHandler.class){
                    @Override
                    public ChannelHandler create() {
                        return new ServerHandler(relationKeeper);
                    }
                })
                .run();

        new MServer(channelListenerPort)
                .withHandlerFactory(new ChannelHandlerFactory(CommandDecoder.class),new ChannelHandlerFactory(CommandEncoder.class))
                .withHandlerFactory(new ChannelHandlerFactory(HttpRequestEncoder.class), new ChannelHandlerFactory(HttpResponseDecoder.class))
                .withHandlerFactory(new ChannelHandlerFactory(ServerHandler.class){
                    @Override
                    public ChannelHandler create() {
                        return new ChannelServerHandler(relationKeeper);
                    }
                })
                .withSecure(true).withTrustCertCollectionFile(trustCertCollectionFile)
                .run();

        logger.info("servers started up.");
    }


}
