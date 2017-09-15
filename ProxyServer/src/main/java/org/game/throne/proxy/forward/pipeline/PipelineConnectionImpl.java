package org.game.throne.proxy.forward.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCounted;
import org.game.throne.proxy.forward.codec.CommandEnum;
import org.game.throne.proxy.forward.codec.CommandHeader;
import org.game.throne.proxy.forward.codec.Phase;
import org.game.throne.proxy.forward.relation.RelationKeeper;
import org.game.throne.proxy.forward.util.FutureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by lvtu on 2017/9/14.
 */
public class PipelineConnectionImpl implements PipelineConnection {

    private final static AtomicInteger portAllocator = new AtomicInteger(1);

    private int pipeport;

    private final static Logger logger = LoggerFactory.getLogger(PipelineConnectionProvider.class);

    protected ChannelHandlerContext leftCtx;

    protected ChannelHandlerContext rightCtx;

    protected RelationKeeper relationKeeper;

    private Lock lock = new ReentrantLock();

    private Condition established = lock.newCondition();

    private Phase state;

    private Phase lifecycle;

    private AtomicInteger respondedRequestCount = new AtomicInteger(0);

    protected PipelineConnectionImpl(ChannelHandlerContext leftCtx, RelationKeeper relationKeeper) {
        this.leftCtx = leftCtx;
        this.relationKeeper = relationKeeper;
        rightCtx = relationKeeper.context(leftCtx);
        if (rightCtx == null) {
            leftCtx.pipeline().writeAndFlush("time out.");
        }
    }

    @Override
    public void write(Object msg) {
        if (msg instanceof CommandEnum) {
            msg = new CommandHeader((CommandEnum) msg, pipeport);
        }
        writeToNextChannel(leftCtx, msg);
    }

    private void writeToNextChannel(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof ReferenceCounted) {
            logger.debug("Retaining reference counted message");
            ((ReferenceCounted) msg).retain();
        }
        if(msg instanceof HttpRequest){
            respondedRequestCount.incrementAndGet();
        }
        if(msg instanceof HttpResponse){
            respondedRequestCount.decrementAndGet();
        }
        lifecycle = respondedRequestCount.get() > 0 ? Phase.REQUEST : Phase.COMPLETE;

        logger.info("start to get next context.");
        logger.info("data arrived. from channel:{},start to write into next channel:{}, msg:{}", ctx.channel(), rightCtx.channel(), msg);
        rightCtx.write(msg).addListener(FutureUtil.errorLogListener(ctx));
        if (msg instanceof LastHttpContent) {
            flushToNextChannel(ctx);
        }
    }

    private void flushToNextChannel(ChannelHandlerContext ctx) {
        logger.info("flush data. from channel:{},start to flush into next channel:{}", ctx.channel(), rightCtx.channel());
        rightCtx.flush();
    }

    @Override
    public void close() {
        if(complete()){
            release();
        }else {
            reallyClose();
        }
    }

    /**
     * 关闭相关联的所有socket连接
     */
    private void reallyClose(){
        release();
        leftCtx.close();
        rightCtx.close();
    }

    /**
     * 不关闭socket连接,回归池里,仅仅断开关联关系
     */
    private void release(){
        relationKeeper.breakRelation(leftCtx);
    }

    protected void bindPipeport() {
        pipeport = portAllocator.getAndIncrement();
    }

    protected void bindPipeport(int pipeport) {
        this.pipeport = pipeport;
    }

    @Override
    public void done() {
        lock.lock();
        try {
            state = Phase.PIPELINE_FINISHED;
            established.signalAll();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isDone() {
        return state == Phase.PIPELINE_FINISHED;
    }

    @Override
    public void awaitDone() {
        lock.lock();
        try {
            established.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }


    @Override
    public Phase lifecycle() {
        return lifecycle;
    }

    @Override
    public boolean complete() {
        return lifecycle == Phase.COMPLETE;
    }

    public int getPipeport() {
        return pipeport;
    }

    public void setPipeport(int pipeport) {
        this.pipeport = pipeport;
    }

    public ChannelHandlerContext getLeftCtx() {
        return leftCtx;
    }

    public void setLeftCtx(ChannelHandlerContext leftCtx) {
        this.leftCtx = leftCtx;
    }

    public ChannelHandlerContext getRightCtx() {
        return rightCtx;
    }

    public void setRightCtx(ChannelHandlerContext rightCtx) {
        this.rightCtx = rightCtx;
    }

    public RelationKeeper getRelationKeeper() {
        return relationKeeper;
    }

    public void setRelationKeeper(RelationKeeper relationKeeper) {
        this.relationKeeper = relationKeeper;
    }
}
