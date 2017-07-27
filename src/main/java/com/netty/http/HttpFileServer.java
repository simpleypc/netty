package com.netty.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;


/**
 * Created by ypc on 2017/7/26.
 */
public class HttpFileServer {
    public static void main(String[] args) {
        int port = 9090;
        if(args != null && args.length > 0){
            try{
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e){
                e.printStackTrace();
            }
        }
        new HttpFileServer().bind(port);
    }

    private void bind(int port) {
        // 配置服务端的 NIO 线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 添加 HTTP 请求消息解码器
                            socketChannel.pipeline().addLast("http-decoder", new HttpRequestDecoder());
                            /**
                             * 添加 HttpObjectAggregator 解码器，作用是
                             * 将多个消息转换为单一的 FullHttpRequest 或者 FullHttpResponse，
                             * 原因是 HTTP 解码器在每个 HTTP 消息中会生成多个消息对象。
                             * 1. HttpRequest ／ HttpResponse;
                             * 2. HttpContent;
                             * 3. LastHttpContent;
                             */
                            socketChannel.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65536));

                            //添加 HTTP 响应消息编码器
                            socketChannel.pipeline().addLast("http-Encoder", new HttpResponseEncoder());

                            /**
                             * 添加 ChunkedWriteHandler 它的主要作用是支持异步发送大的码流（例如大文件传输），
                             * 但不占用过多的内存，防止发生 Java 内存溢出错误。
                             */
                            socketChannel.pipeline().addLast("http-chunked", new ChunkedWriteHandler());

                            // 添加 HttpFileServerHandler 用于文件服务器的业务逻辑处理
                            socketChannel.pipeline().addLast("fileServerHandler", new HttpFileServerHandler(                 ));
                        }
                    });

            // 绑定端口，同步等待成功
            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("HTTP文件目录服务器启动，网址是 : http://192.168.2.9:9090/src/main/java/com/netty/");

            // 等待服务端监听端口关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 优雅退出，释放线程池资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
