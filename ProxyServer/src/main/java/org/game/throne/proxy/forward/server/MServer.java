package org.game.throne.proxy.forward.server;

import com.google.common.base.Verify;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.game.throne.proxy.forward.ChannelHandlerFactory;

import java.io.File;

/**
 * Created by lvtu on 2017/8/31.
 */
public class MServer {

    private int port;

    public MServer(int port) {
        this.port = port;
    }

    private ChannelHandlerFactory[] factories;

    public MServer withHandlerFactory(ChannelHandlerFactory... factories) {
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

    private boolean isSecure = false;
    private File trustCertCollectionFile;

    public MServer withSecure(boolean isSecure){
        this.isSecure = isSecure;
        return this;
    }

    public MServer withTrustCertCollectionFile(File trustCertCollectionFile){
        this.trustCertCollectionFile = trustCertCollectionFile;
        return this;
    }

    public MServer run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            if(isSecure){
                                //验证客户端
                                SslContext sslCtxOfC = SslContextBuilder.forClient().trustManager(trustCertCollectionFile).build();
                                pipeline.addLast("ssl", sslCtxOfC.newHandler(ch.alloc()));
                            }
                            if (factories != null && factories.length > 0) {
                                for (int i = 0; i < factories.length; i++) {
                                    pipeline.addLast(factories[i].create());
                                }
                            }
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync(); // (7)

            f.channel().closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    workerGroup.shutdownGracefully();
                    bossGroup.shutdownGracefully();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }
}
