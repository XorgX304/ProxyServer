package org.game.throne.proxy.forward.client;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.game.throne.proxy.forward.ChannelHandlerFactory;

/**
 * Created by lvtu on 2017/9/4.
 */
public class ClientBootstrap {

    public static void main(String[] args) {
        ChannelClientHandler channelClientHandler = new ChannelClientHandler(new LocalHandler(new LocalClientFactory() {
            @Override
            MClient create(ChannelHandler handler) {
                return new MClient("localhost", 8888)
                        .withHandlerFactory(new ChannelHandlerFactory(HttpRequestEncoder.class), new ChannelHandlerFactory(HttpResponseDecoder.class))
                        .withHandler(handler).connect();
            }
        }));

        new MClient("localhost", 8082)
                .withHandlerFactory(new ChannelHandlerFactory(HttpRequestDecoder.class), new ChannelHandlerFactory(HttpResponseEncoder.class))
                .withHandler(channelClientHandler).connect();
    }
}
