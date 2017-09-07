package org.game.throne.proxy.forward.client;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.ChannelInputShutdownEvent;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCounted;
import org.game.throne.proxy.forward.ChannelRelationEvent;
import org.game.throne.proxy.forward.pool.ContextObjectPool;
import org.game.throne.proxy.forward.pool.ContextObjectPoolImpl;
import org.game.throne.proxy.forward.pool.ContextPooledObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by lvtu on 2017/9/6.
 */
@ChannelHandler.Sharable
public class LocalHandler extends SimpleChannelInboundHandler {

    private final static Logger logger = LoggerFactory.getLogger(LocalHandler.class);

    protected ChannelClientHandler clientHandler = null;

    private ContextObjectPool<ChannelHandlerContext> contextObjectPool;

    protected Map<ChannelHandlerContext, ChannelHandlerContext> relation = new ConcurrentHashMap<>();

    private LocalClientFactory factory;

    public LocalHandler(LocalClientFactory factory) {
        contextObjectPool = new ContextObjectPoolImpl<ChannelHandlerContext>(new ContextPooledObjectFactory());
        this.factory = factory;
    }

    private ReentrantLock lock = new ReentrantLock();

    public ChannelHandlerContext getUsableContext() {
        try {
            lock.lock();
            if (contextObjectPool.getNumIdle() <= 0) {
                addLocalChannel();
            }
            return contextObjectPool.borrowObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            lock.unlock();
        }
    }

    private ChannelHandlerContext channelClientConext(ChannelHandlerContext ctx) {
        return relation.get(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("connected, handler:{},channel:{}", ctx, ctx.channel());
        //保存这个ChannelHandlerContext
        contextObjectPool.addObject(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        writeToNextChannel(ctx, msg);
        if (msg instanceof LastHttpContent) {
            flushToNextChannel(ctx);
            //response发送到了channel client中,可以解除关联了
            responseFinishedNotify(ctx);
        }
    }

    private void writeToNextChannel(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof ReferenceCounted) {
            logger.debug("Retaining reference counted message");
            ((ReferenceCounted) msg).retain();
        }
        logger.info("start to get next context.");
        ChannelHandlerContext channelClientConext = channelClientConext(ctx);
        logger.info("data arrived. from channel:{},start to write into next channel:{}, msg:{}", ctx.channel(), channelClientConext.channel(), msg);
        channelClientConext.write(msg);
    }

    private void flushToNextChannel(ChannelHandlerContext ctx) throws Exception {
        ChannelHandlerContext channelClientConext = channelClientConext(ctx);
        logger.info("flush data. from channel:{},start to flush into next channel:{}", ctx.channel(), channelClientConext.channel());
        channelClientConext.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        contextObjectPool.invalidateObject(ctx);
        cause.printStackTrace();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        contextObjectPool.invalidateObject(ctx);
        super.channelInactive(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
    }

    private void addLocalChannel() {
        factory.create(this);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //服务器优先发送了FIN packet
        if (ChannelInputShutdownEvent.INSTANCE.equals(evt)) {
            requestBreakRelation(ctx);
            contextObjectPool.invalidateObject(ctx);
            ctx.close();
        }
        if(ChannelRelationEvent.BREAK.equals(evt)){
            responseBreakRelation(ctx);
        }
    }

    private void responseFinishedNotify(ChannelHandlerContext ctx){
        channelClientConext(ctx).pipeline().fireUserEventTriggered(ChannelRelationEvent.RESPONSE_FINISHED);
    }

    private void responseBreakRelation(ChannelHandlerContext ctx){
        relation.remove(ctx);
        try {
            contextObjectPool.returnObject(ctx);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestBreakRelation(ChannelHandlerContext ctx) {
        ChannelHandlerContext channelClientConext = relation.remove(ctx);
        channelClientConext.pipeline().fireUserEventTriggered(ChannelRelationEvent.BREAK);
        try {
            contextObjectPool.returnObject(ctx);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
