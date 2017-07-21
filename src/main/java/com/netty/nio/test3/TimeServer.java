package com.netty.nio.test3;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Created by ypc on 2017/4/3.粘包 拆包 解决
 *  socketChannel.pipeline().addLast(new LineBasedFrameDecoder(1024));
 *  socketChannel.pipeline().addLast(new StringDecoder());
 *
 *  LineBasedFrameDecoder 的工作原理是它依次遍历 ByteBuf 中的可读字节，
 *  判断看是否有 "\n" 或者 "\r\n"，如果有就以此位置为结束位置，从可读索引
 *  到结束位置区间的字节就组成了一行，它是以换行符为结束标志的解码器，支持携带
 *  结束符或者不携带结束符两种解码方式，同时支持配置单行的最大长度。如果连续读到
 *  最大长度后仍然没有发现换行符，就会抛出异常，同时忽略掉之前读到的异常码流。
 *
 *  StringDecoder 的功能非常简单，就是将接收到的对象转换成字符串，然后继续调用
 *  后面的 Handler。 LineBasedFrameDecoder + StringDecoder 组合就是
 *  按行切换的文本解码器，它被设计用来支持 TCP 的粘包和拆包
 */
public class TimeServer {
    public static void main(String[] args) {
        int port = 9090;
        if(args != null && args.length > 0){
            try{
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e){
                e.printStackTrace();
            }
        }
        new TimeServer().bind(port);
    }

    private void bind(int port) {
        // 配置服务端的 NIO 线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new LineBasedFrameDecoder(1024));
                            socketChannel.pipeline().addLast(new StringDecoder());
                            socketChannel.pipeline().addLast(new TimeServerHandler());
                        }
                    });

            // 绑定端口，同步等待成功
            ChannelFuture future = bootstrap.bind(port).sync();

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
