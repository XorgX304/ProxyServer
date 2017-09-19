package org.game.throne.proxy.forward.relation;

import io.netty.channel.ChannelHandlerContext;

/**
 * Created by lvtu on 2017/9/7.
 */
@Deprecated
public interface RelationProcess {
    void requestBreakRelation(ChannelHandlerContext ctx);

    /**
     * Relation会向Relation的元素发送事件,在该函数中,处理具体的relation break流程,如回收资源等等。
     * @param ctx
     */
    void responseBreakRelation(ChannelHandlerContext ctx);
}
