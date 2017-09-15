package org.game.throne.proxy.forward.client;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.game.throne.proxy.forward.ChannelHandlerFactory;
import org.game.throne.proxy.forward.codec.CommandDecoder;
import org.game.throne.proxy.forward.codec.CommandEncoder;
import org.game.throne.proxy.forward.relation.RelationKeeper;

import java.io.File;

/**
 * Created by lvtu on 2017/9/4.
 */
public class ClientBootstrap {

    public static void main(String[] args) {
        String localhost = "localhost";
        int localPort = 8888;
        String remoteHost = "localhost";
        int remotePort = 8082;

        File keyCertChainFile = new File("/Users/lvtu/workspace/english/cert/cer.pem");
        File keyFile = new File("/Users/lvtu/workspace/english/cert/privateKey.pkcs8.pem");

        ClientFactory localClientFactory = new ClientFactory() {
            @Override
            public MClient create(RelationKeeper relationKeeper) {
                return new MClient(localhost, localPort)
                        .withHandlerFactory(new ChannelHandlerFactory(HttpRequestEncoder.class), new ChannelHandlerFactory(HttpResponseDecoder.class))
                        .withHandlerFactory(new ChannelHandlerFactory(LocalHandler.class){
                            @Override
                            public ChannelHandler create() {
                                return new LocalHandler(relationKeeper);
                            }
                        })
                        .connect();
            }
        };

        ClientFactory channelClientFactory = new ClientFactory() {
            private ClientFactory clientFactory = this;
            @Override
            public MClient create(RelationKeeper relationKeeper) {
                return new MClient(remoteHost, remotePort)
                        .withHandlerFactory(new ChannelHandlerFactory(CommandDecoder.class),new ChannelHandlerFactory(CommandEncoder.class),new ChannelHandlerFactory(HttpRequestDecoder.class), new ChannelHandlerFactory(HttpResponseEncoder.class))
                        .withHandlerFactory(new ChannelHandlerFactory(ChannelClientHandler.class){
                            @Override
                            public ChannelHandler create() {
                                return new ChannelClientHandler(relationKeeper, clientFactory);
                            }
                        })
                        .withSecure(true)
                        .withSecureFile(keyCertChainFile, keyFile)
                        .connect();
            }
        };

        RelationKeeper relationKeeper = RelationKeeper.forClient(localClientFactory);

        channelClientFactory.create(relationKeeper);

    }
}
