package org.game.throne.proxy.forward.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

/**
 * Created by lvtu on 2017/9/7.
 */
public class HttpUtil {

    public static HttpResponse errorResponse() {
        return errorResponse("errror");
    }

    public static HttpResponse errorResponse(String s) {
        ByteBuf content = Unpooled.copiedBuffer(s, CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, content);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");//必须加这个，否则浏览器不响应
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, content.readableBytes());//必须加这个，否则浏览器不响应
        response.headers().set(HttpHeaders.Names.CONNECTION, "keep-alive");
        return response;
    }
}
