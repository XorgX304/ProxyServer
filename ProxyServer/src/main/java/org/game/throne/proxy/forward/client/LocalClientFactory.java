package org.game.throne.proxy.forward.client;

import io.netty.channel.ChannelHandler;

/**
 * Created by lvtu on 2017/9/6.
 */
public abstract class LocalClientFactory {
    abstract MClient create(ChannelHandler handler);
}
