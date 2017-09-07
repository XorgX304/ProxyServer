package org.game.throne.proxy.forward.relation;

import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.game.throne.proxy.forward.ChannelRelationEvent;

/**
 * Created by lvtu on 2017/9/7.
 */
public class Relation {

    enum State {
        ESTABLISHED,
        BREAKED;
    }

    private Pair<ChannelHandlerContext, ChannelHandlerContext> pair;
    private State state;

    public Relation(ChannelHandlerContext left, ChannelHandlerContext right) {
        pair = new MutablePair(left, right);
        state = State.ESTABLISHED;
    }

    public boolean established() {
        return State.ESTABLISHED == state;
    }

    public boolean breaked() {
        return State.BREAKED == state;
    }

    public boolean exist(ChannelHandlerContext leftOrRight) {
        return established() && (leftOrRight == pair.getLeft() || leftOrRight == pair.getRight());
    }

    public ChannelHandlerContext matched(ChannelHandlerContext leftOrRight) {
        return exist(leftOrRight) ? (leftOrRight == pair.getLeft() ? pair.getRight() : pair.getLeft()) : null;
    }

    public void breakRelation(ChannelHandlerContext leftOrRight) {
        if (exist(leftOrRight)) {
            state = State.BREAKED;
            pair.getLeft().pipeline().fireUserEventTriggered(ChannelRelationEvent.BREAK);
            pair.getRight().pipeline().fireUserEventTriggered(ChannelRelationEvent.BREAK);
        }
    }

    public ChannelHandlerContext getLeft(){
        return pair.getLeft();
    }

    public ChannelHandlerContext getRight(){
        return pair.getRight();
    }

}
