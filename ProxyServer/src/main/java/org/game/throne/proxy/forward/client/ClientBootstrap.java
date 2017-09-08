package org.game.throne.proxy.forward.client;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.game.throne.proxy.forward.ChannelHandlerFactory;
import org.game.throne.proxy.forward.relation.RelationKeeper;

/**
 * Created by lvtu on 2017/9/4.
 */
public class ClientBootstrap {

    public static void main(String[] args) {
        RelationKeeper relationKeeper = new RelationKeeper();
        ClientFactory localClientFactory = new ClientFactory() {
            @Override
            MClient create(ChannelHandler handler) {
                MClient client = new MClient("localhost", 8888)
                        .withHandlerFactory(new ChannelHandlerFactory(HttpRequestEncoder.class), new ChannelHandlerFactory(HttpResponseDecoder.class))
                        .withHandler(handler).connect();
                client.releaseOnClose();
                return client;
            }
        };
        ClientFactory channelClientFactory = new ClientFactory() {
            @Override
            MClient create(ChannelHandler handler) {
                MClient client = new MClient("localhost", 8082)
                        .withHandlerFactory(new ChannelHandlerFactory(HttpRequestDecoder.class), new ChannelHandlerFactory(HttpResponseEncoder.class))
                        .withHandler(handler).connect();
                client.releaseOnClose();
                return client;
            }
        };
        LocalHandler localHandler = new LocalHandler(localClientFactory, relationKeeper);
        ChannelClientHandler channelClientHandler = new ChannelClientHandler(localHandler, relationKeeper, channelClientFactory);

        channelClientFactory.create(channelClientHandler);

    }
}