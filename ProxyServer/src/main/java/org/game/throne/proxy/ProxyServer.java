package org.game.throne.proxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lvtu on 2017/8/24.
 */
public class ProxyServer {

    private static final Logger logger = LoggerFactory.getLogger(ProxyServer.class);

    public static void main(String[] args) {
//        if (args.length < 3) {
//            logger.info("please specify listenerPort,forward host and port.");
//        }

        int listenerPort = 8081;//Integer.parseInt(args[0]);
        String host = "l";//args[1];
        String port = "8888";//args[2];

        HttpProxyServer server =
                DefaultHttpProxyServer.bootstrap()
                        .withPort(listenerPort)
                        .withAllowRequestToOriginServer(true)
                        .withAllowLocalOnly(false)
                        .withFiltersSource(new HttpFiltersSourceAdapter() {
                            public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                                return new HttpFiltersAdapter(originalRequest) {

                                    @Override
                                    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
                                        if (httpObject instanceof DefaultHttpRequest) {
                                            logger.info("received request.");
                                            DefaultHttpRequest request = (DefaultHttpRequest) httpObject;
                                            HttpHeaders headers = request.headers();
                                            headers.set("host", host + ":" + port);
                                        }
                                        return null;
                                    }
                                };
                            }
                        })
                        .start();
    }
}
