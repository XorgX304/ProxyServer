package org.game.throne.proxy;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Created by lvtu on 2017/8/28.
 */
public class Server {

    public final static byte CR = 13;
    public final static byte SP = 32;

    public void start() {
        try {
            Selector sel = Selector.open();

            ServerSocketChannel scc = ServerSocketChannel.open();
            SocketAddress address = new InetSocketAddress(20000);
            scc.configureBlocking(false);
            scc.bind(address, 100);

            scc.register(sel, SelectionKey.OP_ACCEPT);

            while(true){
                int count = sel.select();

                Iterator<SelectionKey> iterator = count>0?sel.selectedKeys().iterator():null;

                while (iterator!=null && iterator.hasNext()) {
                    SelectionKey selKey = iterator.next();
                    if (selKey.isValid()) {
                        if(selKey.isAcceptable()){
                            SocketChannel sc = scc.accept();
                            sc.configureBlocking(false);
                            sc.register(sel, SelectionKey.OP_READ);
                        }

                        if (selKey.isReadable()) {
                            ByteBuffer bb = ByteBuffer.allocate(5);
                            bb.clear();
//                            sc.read(bb);
                            if (bb.hasRemaining()) {
                                int remaining = bb.remaining();
                                byte[] b = new byte[remaining];
                                System.out.println(new String(b, "utf-8"));
                                bb.get(b);
                                if (b[b.length - 1] == SP) {

                                }
                            }


                        }
                    }
                    iterator.remove();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Server();
    }

}
