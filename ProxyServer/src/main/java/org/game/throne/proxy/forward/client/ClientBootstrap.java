package org.game.throne.proxy.forward.client;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.game.throne.proxy.forward.ChannelHandlerFactory;
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


        RelationKeeper relationKeeper = new RelationKeeper();
        ClientFactory localClientFactory = new ClientFactory() {
            @Override
            MClient create(ChannelHandler handler) {
                MClient client = new MClient(localhost, localPort)
                        .withHandlerFactory(new ChannelHandlerFactory(HttpRequestEncoder.class), new ChannelHandlerFactory(HttpResponseDecoder.class))
                        .withHandler(handler).connect();
                return client;
            }
        };
        ClientFactory channelClientFactory = new ClientFactory() {
            @Override
            MClient create(ChannelHandler handler) {
                MClient client = new MClient(remoteHost, remotePort)
                        .withHandlerFactory(new ChannelHandlerFactory(HttpRequestDecoder.class), new ChannelHandlerFactory(HttpResponseEncoder.class))
                        .withHandler(handler).withSecure(true).withSecureFile(keyCertChainFile, keyFile).connect();
                return client;
            }
        };
        LocalHandler localHandler = new LocalHandler(localClientFactory, relationKeeper);
        ChannelClientHandler channelClientHandler = new ChannelClientHandler(localHandler, relationKeeper, channelClientFactory);

        channelClientFactory.create(channelClientHandler);

    }
}
