package org.game.throne.proxy.forward.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.game.throne.proxy.forward.ChannelHandlerFactory;

/**
 * Created by lvtu on 2017/8/31.
 */
public class MServer {

    private int port;

    public MServer(int port) {
        this.port = port;
    }

    private ChannelHandler[] handler;

    public MServer withHandler(ChannelHandler... handler) {
        this.handler = handler;
        return this;
    }

    private ChannelHandlerFactory[] factories;

    public MServer withHandlerFactory(ChannelHandlerFactory... factories) {
        this.factories = factories;
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
                            if (factories != null && factories.length > 0) {
                                for (int i = 0; i < factories.length; i++) {
                                    pipeline.addLast(factories[i].create());
                                }
                            }
                            pipeline.addLast(handler);
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync(); // (7)

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
//            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            workerGroup.shutdownGracefully();
//            bossGroup.shutdownGracefully();
        }
        return this;
    }
}
