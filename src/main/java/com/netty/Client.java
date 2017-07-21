package com.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

public class Client {

	private final String host;
	private final int port;

	public Client(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void start() throws InterruptedException {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		//EventLoopGroup workGroup = new NioEventLoopGroup();
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(bossGroup).channel(NioSocketChannel.class)
				.option(ChannelOption.TCP_NODELAY, true)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						/*ByteBuf delimiter = Unpooled.copiedBuffer("_#_".getBytes());
                        // 增加 DelimiterBasedFrameDecoder编码器
						ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new DelimiterBasedFrameDecoder(1024, delimiter));
                        pipeline.addLast(new StringDecoder());
						pipeline.addLast(new EchoClientHandler());*/
						ch.pipeline().addLast(new EchoClientHandler());
					}
				});
		ChannelFuture future = bootstrap.connect(host,port).sync();
		future.channel().closeFuture().sync();
		bossGroup.shutdownGracefully();

	}

	public static void main(String[] args) throws InterruptedException {
		new Client("127.0.0.1", 9090).start();
	}

}
