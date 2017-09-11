package org.game.throne.proxy.forward.secure;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;

import java.io.File;

/**
 * Created by lvtu on 2017/9/8.
 */
public class Secure {
    public static void main(String[] args) {
        try {
            Channel channel = null;
            SslContext sslCtx = SslContextBuilder.forServer(new File(""), new File("")).build();
            SslHandler sslHandlerOnServer = sslCtx.newHandler(channel.alloc());

            ChannelPipeline p = channel.pipeline();
            SslContext sslCtxOfC = SslContextBuilder.forClient().trustManager(new File("")).build();
            SslHandler sslHandlerOnClient = sslCtxOfC.newHandler(channel.alloc(), "", 18888);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
