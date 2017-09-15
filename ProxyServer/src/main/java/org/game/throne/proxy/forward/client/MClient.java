package org.game.throne.proxy.forward.client;

import com.google.common.base.Verify;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.game.throne.proxy.forward.ChannelHandlerFactory;

import java.io.File;

/**
 * Created by lvtu on 2017/9/1.
 */
public class MClient {

    private String host;
    private int port;
    private ChannelHandler[] handler;

    public MClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private ChannelHandlerFactory[] factories;

    public MClient withHandlerFactory(ChannelHandlerFactory... factories) {
        Verify.verifyNotNull(factories);
        if (factories == null) {
            this.factories = factories;
        } else {
            ChannelHandlerFactory[] f = new ChannelHandlerFactory[factories.length + this.factories.length];
            System.arraycopy(this.factories, 0, f, 0, this.factories.length);
            System.arraycopy(factories, 0, f, this.factories.length, factories.length);
            this.factories = f;
        }
        return this;
    }

    private boolean isSecure;
    private File keyCertChainFile;
    private File keyFile;

    public MClient withSecure(boolean isSecure) {
        this.isSecure = isSecure;
        return this;
    }

    public MClient withSecureFile(File keyCertChainFile, File keyFile) {
        this.keyCertChainFile = keyCertChainFile;
        this.keyFile = keyFile;
        return this;
    }

    public MClient connect() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    if (isSecure) {
                        SslContext sslCtx = SslContextBuilder.forServer(keyCertChainFile, keyFile).build();
                        pipeline.addLast("ssl", sslCtx.newHandler(ch.alloc()));
                    }
                    if (factories != null && factories.length > 0) {
                        for (int i = 0; i < factories.length; i++) {
                            pipeline.addLast(factories[i].create());
                        }
                    }
                    pipeline.addLast(handler);
                }
            });

            // Start the client.
            ChannelFuture f = b.connect(host, port).sync(); // (5)

            f.channel().closeFuture().addListener(new GenericFutureListener() {
                @Override
                public void operationComplete(Future future) throws Exception {
                    workerGroup.shutdownGracefully();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }
}
