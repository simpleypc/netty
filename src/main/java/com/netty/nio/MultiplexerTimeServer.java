package com.netty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Set;

/**
 * Created by ypc on 2017/3/15.
 */
public class MultiplexerTimeServer implements Runnable {

    private Selector selector;

    private ServerSocketChannel servChannel;

    private volatile boolean stop;

    /**
     * 初始化多路复用器，绑定监听端口
     * @param port
     */
    public MultiplexerTimeServer(int port){
        try {
            // 开启 Selector 多路复用器
            selector = Selector.open();

            // 开启用于监听客户端的连接
            servChannel = ServerSocketChannel.open();

            // 设置连接为为阻塞模式
            servChannel.configureBlocking(false);

            // 绑定监听端口
            servChannel.socket().bind(new InetSocketAddress(port), 1024);

            // 将 ServerSocketChannel 注册到 Reactor 线程的多路复用器 Selector 上，
            // 监听 OP_ACCEPT 连接事件
            /**
             * SelectionKey.OP_CONNECT　"连接就绪" 某个channel成功连接到另一个服务器
             * SelectionKey.OP_ACCEPT 　"接收就绪" 一个server socket channel准备接收新进入的连接
             * SelectionKey.OP_READ　　　"读就绪"   一个有数据可读的通道
             * SelectionKey.OP_WRITE　　"写就绪"   等待写数据的通道
             */
            servChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("the time server is start in port : " + port);
        } catch (IOException e) {
            e.printStackTrace();
            // 异常退出当前服务
            System.exit(1);
        }
    }

    public void stop(){
        this.stop = true;
    }

    @Override
    public void run() {
        while (!stop){
            try {
                // 多路复用器在线程 run 方法的
                selector.select(1000);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey key:selectionKeys) {
                    selectionKeys.remove(key);
                    try {
                        handleInput(key);
                    } catch (IOException e) {
                        if (key != null){
                            key.cancel();
                            if (key.channel() != null)
                                key.channel().close();
                        }
                    }
                }
                /*Iterator<SelectionKey> it = selectionKeys.iterator();
                SelectionKey key = null;
                while (it.hasNext()){
                    key = it.next();
                    it.remove();
                    try {
                        handleInput(key);
                    } catch (IOException e) {
                        if (key != null){
                            key.cancel();
                            if (key.channel() != null)
                                key.channel().close();
                        }
                    }
                }*/
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        // 多路复用器关闭后，所有注册在上面的 Channel 和 Pipe 等资源都会被自动去注册并关闭，
        // 所以不需要重新释放资源。
        if (selector != null){
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleInput(SelectionKey key) throws IOException {
        // 告知此键是否有效
        if (!key.isValid()) return;
        // 处理新接入的请求消息 --此键的通道是否已准备好接受新的套接字连接
        if (key.isAcceptable()){
            // 多路复用器监听到有新的客户端接入，处理新的接入请求，完成 TCP 三次握手，建立物理链路
            ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
            SocketChannel sc = ssc.accept();
            sc.configureBlocking(false);
            // 将新接入的客户端连接注册到 Reactor 线程的多路复用器上，监听读操作，读取客户端发送的网络消息。
            sc.register(selector, SelectionKey.OP_READ);
        }
        // 此键的通道是否已准备好进行读取
        if (key.isReadable()){
            SocketChannel sc = (SocketChannel) key.channel();
            // 分配一个新的字节缓冲区
            ByteBuffer readBuffer = ByteBuffer.allocate(1024);
            // 异步读取客户端请求消息到缓存区。
            int readBytes = sc.read(readBuffer);
            if (readBytes > 0){
                // 翻转 就是将一个处于存数据状态的缓冲区变为一个处于准备取数据的状态
                readBuffer.flip();
                // 返回limit和position之间相对位置差
                byte[] bytes = new byte[readBuffer.remaining()];
                // 相对读，从position位置读取一个byte，并将position+1，为下次读写作准备
                readBuffer.get(bytes);
                String body = new String(bytes, "UTF-8");
                System.out.println("the time server receive order : " + body);
                String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ?
                        new Date(System.currentTimeMillis()).toString() :
                        "BAD ORDER";
                doWrite(sc, currentTime);
            } else if (readBytes < 0) {
                // 对端链路关闭
                key.cancel();
                sc.close();
            } else {
                ; // 读到 0 节点忽略
            }
        }
    }

    private void doWrite(SocketChannel channel, String response) throws IOException {
        if (response != null && response.trim().length() > 0){
            byte[] bytes = response.getBytes();
            ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
            // 相对写，向position的位置写入一个byte，并将postion+1，为下次读写作准备
            writeBuffer.put(bytes);
            // 翻转 就是将一个处于存数据状态的缓冲区变为一个处于准备取数据的状态
            writeBuffer.flip();
            // 以一个ByteBuffer为参数,试图将该缓冲区中字节写入信道.
            channel.write(writeBuffer);
        }
    }
}
