package org.game.throne.proxy.forward.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.game.throne.proxy.forward.ChannelHandlerFactory;

/**
 * Created by lvtu on 2017/9/1.
 */
public class MClient {

    private String host;
    private int port;
    private ChannelHandler[] handler;

    private Channel channel;
    private EventLoopGroup workerGroup;

    public MClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public MClient withHandler(ChannelHandler... handler) {
        this.handler = handler;
        return this;
    }

    private ChannelHandlerFactory[] factories;

    public MClient withHandlerFactory(ChannelHandlerFactory... factories) {
        this.factories = factories;
        return this;
    }


    public MClient connect() {
        workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
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
            });

            // Start the client.
            ChannelFuture f = b.connect(host, port).sync(); // (5)

            channel = f.channel();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public void releaseOnClose() {
        try {
//            channel.closeFuture().sync(); //担心这种写法会有deadlock,例如这个任务会等待下一个任务执行的close,但是这个任务又会一直等待close,因此有可能形成deadlock。
//            channel.close(); netty好像自己会调用,需要调查一下
            channel.closeFuture().addListener(new GenericFutureListener() {
                @Override
                public void operationComplete(Future future) throws Exception {
                    workerGroup.shutdownGracefully();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
