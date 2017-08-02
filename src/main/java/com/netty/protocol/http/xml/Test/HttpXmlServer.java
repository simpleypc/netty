package com.netty.protocol.http.xml.Test;

import com.netty.protocol.http.xml.codec.HttpXmlRequestDecoder;
import com.netty.protocol.http.xml.codec.HttpXmlResponseEncoder;
import com.netty.protocol.http.xml.pojo.Order;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;

import java.net.InetSocketAddress;

/**
 * Created by ypc on 2017/7/27.
 * 可以参考 http://blog.csdn.net/kingsonyoung/article/details/50524866
 */
public class HttpXmlServer {

	public static void main(String[] args) throws Exception {
		int port = 9090;
		if (args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		new HttpXmlServer().run(port);
	}

    public void run(final int port) throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						//添加 HTTP 请求消息解码器
						ch.pipeline().addLast("http-decoder", new HttpRequestDecoder());
						/**
						 * 添加 HttpObjectAggregator 解码器，作用是
						 * 将多个消息转换为单一的 FullHttpRequest 或者 FullHttpResponse，
						 * 原因是 HTTP 解码器在每个 HTTP 消息中会生成多个消息对象。
						 * 1. HttpRequest ／ HttpResponse;
						 * 2. HttpContent;
						 * 3. LastHttpContent;
						 */
						ch.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65536));
						// 添加 XML 请求消息解码器
						ch.pipeline().addLast("xml-decoder", new HttpXmlRequestDecoder(Order.class, true));
						// 添加 HTTP 响应消息编码器
						ch.pipeline().addLast("http-encoder", new HttpResponseEncoder());
						// 添加 XML 响应消息编码器
						ch.pipeline().addLast("xml-encoder", new HttpXmlResponseEncoder());
						// 业务操作
						ch.pipeline().addLast("xmlServerHandler", new HttpXmlServerHandler());
					}
				});
			ChannelFuture future = b.bind(new InetSocketAddress(port)).sync();
			System.out.println("HTTP订购服务器启动，网址是 : " + "http://localhost:" + port);
			future.channel().closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
    }
}
