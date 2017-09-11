package org.game.throne.proxy.forward.client;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.ChannelInputShutdownEvent;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCounted;
import org.game.throne.proxy.forward.ChannelRelationEvent;
import org.game.throne.proxy.forward.relation.RelationKeeper;
import org.game.throne.proxy.forward.relation.RelationProcess;
import org.game.throne.proxy.forward.util.FutureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by lvtu on 2017/9/6.
 */
@ChannelHandler.Sharable
public class LocalHandler extends SimpleChannelInboundHandler implements RelationProcess, Runnable {

    private final static Logger logger = LoggerFactory.getLogger(LocalHandler.class);

    protected ChannelClientHandler clientHandler = null;

    private BlockingDeque<ChannelHandlerContext> contextObjectPool = new LinkedBlockingDeque();

    private ClientFactory factory;

    private ReentrantLock lock = new ReentrantLock();

    private RelationKeeper relationKeeper;

    private ScheduledThreadPoolExecutor clientHouseKeeper = new ScheduledThreadPoolExecutor(1);

    public LocalHandler(ClientFactory factory, RelationKeeper relationKeeper) {
        this.factory = factory;
        this.relationKeeper = relationKeeper;
        clientHouseKeeper.scheduleWithFixedDelay(this, 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        if (contextObjectPool.size() <= 0) {
            addLocalChannel();
        }
    }

    public ChannelHandlerContext getAvailableContext() {
        try {
            lock.lock();
            return contextObjectPool.poll(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            lock.unlock();
        }
    }

    private ChannelHandlerContext channelClientConext(ChannelHandlerContext ctx) {
        return relationKeeper.matchedContext(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("connected, handler:{},channel:{}", ctx, ctx.channel());
        //保存这个ChannelHandlerContext
        contextObjectPool.offer(ctx);
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
        channelClientConext.write(msg).addListener(FutureUtil.errorLogListener(ctx));
    }

    private void flushToNextChannel(ChannelHandlerContext ctx) throws Exception {
        ChannelHandlerContext channelClientConext = channelClientConext(ctx);
        logger.info("flush data. from channel:{},start to flush into next channel:{}", ctx.channel(), channelClientConext.channel());
        channelClientConext.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        requestBreakRelation(ctx);
        contextObjectPool.remove(ctx);
        cause.printStackTrace();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("{} is closed.", ctx);
        requestBreakRelation(ctx);
        contextObjectPool.remove(ctx);
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
            contextObjectPool.remove(ctx);
            ctx.close();
        }
        if (ChannelRelationEvent.BREAK.equals(evt)) {
            responseBreakRelation(ctx);
        }
    }

    private void responseFinishedNotify(ChannelHandlerContext ctx) {
        channelClientConext(ctx).pipeline().fireUserEventTriggered(ChannelRelationEvent.RESPONSE_FINISHED);
    }

    @Override
    public void requestBreakRelation(ChannelHandlerContext ctx) {
        relationKeeper.breakRelation(ctx);
    }

    @Override
    public void responseBreakRelation(ChannelHandlerContext ctx) {
        try {
            contextObjectPool.offer(ctx);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
