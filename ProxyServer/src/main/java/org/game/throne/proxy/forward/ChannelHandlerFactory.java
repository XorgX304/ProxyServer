package org.game.throne.proxy.forward;

import io.netty.channel.ChannelHandler;

/**
 * Created by lvtu on 2017/9/6.
 */
public class ChannelHandlerFactory<T extends ChannelHandler> {

    private Class<T> clazz;

    public ChannelHandlerFactory(Class<T> clazz){
        this.clazz = clazz;
    }

    public ChannelHandler create(){
        try {
            return clazz.newInstance();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
