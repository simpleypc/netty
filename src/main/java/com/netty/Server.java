package com.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

import java.util.Date;

/**
 * Hello world!
 *
 */
public class Server {

	private final int port;

	public Server(int port) {
		this.port = port;
	}

	public void start() throws InterruptedException {
		// 处理I/O操作的多线程事件循环器，
		// 接收进来的连接
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		// 处理已经被接收的连接进行读写
		NioEventLoopGroup workerGroup = new NioEventLoopGroup();

		// 启动NIO服务的辅助启动类
		ServerBootstrap bootstrap = new ServerBootstrap();

		bootstrap.group(bossGroup,workerGroup)
				.channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, 1024)
				.childHandler(new ChannelHandlerAdapter() {
					@Override
					public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
						ByteBuf buf = (ByteBuf) msg;
						byte[] req = new byte[buf.readableBytes()];
						buf.readBytes(req);
						String body = new String(req, "UTF-8");
						System.out.println("The time server receive order :" + body);
						String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ?
								new Date(System.currentTimeMillis()).toString() : "BAD ORDER";
						ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
						ctx.write(resp);
					}
					@Override
					public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
						ctx.flush();
					}
					@Override
					public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
						ctx.close();
					}
				});

		ChannelFuture future = bootstrap.bind(port).sync();
		System.out.println(Server.class.getName() + ":" + future.channel().localAddress());
		future.channel().closeFuture().sync();
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
	}

	public static void main(String[] args) throws InterruptedException {
		new Server(9090).start();
	}
}
