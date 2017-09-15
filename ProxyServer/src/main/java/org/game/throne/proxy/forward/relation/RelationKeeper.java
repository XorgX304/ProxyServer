package org.game.throne.proxy.forward.relation;

import io.netty.channel.ChannelHandlerContext;
import org.game.throne.proxy.forward.client.ClientFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Created by lvtu on 2017/9/7.
 */
public class RelationKeeper implements Runnable {

    private List<Relation> relations = new ArrayList<>();

    private ReentrantLock lock = new ReentrantLock();

    private ScheduledThreadPoolExecutor housekeeper = new ScheduledThreadPoolExecutor(1);

    private RelationKeeper() {
        housekeeper.scheduleWithFixedDelay(this, 0, 5, TimeUnit.SECONDS);
    }

    public BlockingDeque<ChannelHandlerContext> contextObjectPool = new LinkedBlockingDeque();

    private ChannelHandlerContext getAvailableContext() {
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

    public ChannelHandlerContext context(ChannelHandlerContext ctx) {
        if (this.exists(ctx)) {
            return this.matchedContext(ctx);
        }
        ChannelHandlerContext rightCtx = getAvailableContext();
        if (rightCtx == null) {
            return null;
        }
        this.addRelation(ctx, rightCtx);
        return rightCtx;
    }

    public static RelationKeeper forClient(ClientFactory factory) {
        RelationKeeper relationKeeper = new RelationKeeper();
        relationKeeper.housekeeper.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (relationKeeper.contextObjectPool.size() < 1) {
                    factory.create(relationKeeper);
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
        return relationKeeper;
    }

    public static RelationKeeper forServer() {
        RelationKeeper relationKeeper = new RelationKeeper();
        return relationKeeper;
    }


    /**
     * 释放已经断开的channel relation内存资源,让gc回收channnel ctx对象占用的内存。
     */
    @Override
    public void run() {
        try {
            lock.lock();
            Iterator<Relation> iterator = relations.iterator();
            while (iterator.hasNext()) {
                Relation relation = iterator.next();
                if (relation.breaked()) {
                    iterator.remove();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void addRelation(ChannelHandlerContext left, ChannelHandlerContext right) {
        try {
            lock.lock();
            Relation relation = new Relation(left, right);
            relations.add(relation);
        } finally {
            lock.unlock();
        }
    }

    public boolean exists(ChannelHandlerContext leftOrRight) {
        try {
            lock.lock();
            return relations.stream().anyMatch(relation -> relation.exist(leftOrRight));
        } finally {
            lock.unlock();
        }
    }

    public ChannelHandlerContext matchedContext(ChannelHandlerContext leftOrRight) {
        try {
            lock.lock();
            List<ChannelHandlerContext> contexts = relations.stream().filter(relation -> relation.exist(leftOrRight)).map(relation ->
                    relation.matched(leftOrRight)
            ).collect(Collectors.toList());
            if (contexts.size() > 1) {
                throw new RuntimeException("wrong relation.");
            }
            return contexts.size() == 1 ? contexts.get(0) : null;
        } finally {
            lock.unlock();
        }
    }

    public Relation matchedRelation(ChannelHandlerContext leftOrRight) {
        try {
            lock.lock();
            List<Relation> relations = this.relations.stream().filter(relation -> relation.exist(leftOrRight)).collect(Collectors.toList());
            if (relations.size() > 1) {
                throw new RuntimeException("wrong relation.");
            }
            return relations.size() == 1 ? relations.get(0) : null;
        } finally {
            lock.unlock();
        }
    }


    public void breakRelation(ChannelHandlerContext leftOrRight) {
        try {
            lock.lock();
            if (exists(leftOrRight)) {
                matchedRelation(leftOrRight).breakRelation(leftOrRight);
            }
        } finally {
            lock.unlock();
        }
    }

}
