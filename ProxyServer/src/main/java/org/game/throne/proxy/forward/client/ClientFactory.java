package org.game.throne.proxy.forward.client;

import org.game.throne.proxy.forward.relation.RelationKeeper;

/**
 * Created by lvtu on 2017/9/6.
 */
public abstract class ClientFactory {
    public abstract MClient create(RelationKeeper relationKeeper);
}
