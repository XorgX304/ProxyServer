package org.game.throne.proxy.forward.util;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lvtu on 2017/9/11.
 */
public class FutureUtil {

    private final static Logger logger = LoggerFactory.getLogger(FutureUtil.class);

    private final static GenericFutureListener errorLogListener = new GenericFutureListener<Future<? super Void>>() {
        @Override
        public void operationComplete(Future<? super Void> future) throws Exception {
            if (future.cause() != null) {
                logger.error("{}", future.cause());
            }
        }
    };

    public static GenericFutureListener errorLogListener(ChannelHandlerContext ctx) {
        return new ContextFutureListener(ctx);
    }

    static class ContextFutureListener implements GenericFutureListener {

        private ChannelHandlerContext ctx;

        ContextFutureListener(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void operationComplete(Future future) throws Exception {
            if (future.cause() != null) {
                ctx.pipeline().fireExceptionCaught(future.cause());
            }
        }
    }
}
