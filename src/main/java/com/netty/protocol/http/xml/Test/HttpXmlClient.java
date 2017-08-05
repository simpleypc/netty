package com.netty.protocol.http.xml.test;

import com.netty.protocol.http.xml.codec.HttpXmlRequestEncoder;
import com.netty.protocol.http.xml.pojo.Order;
import com.netty.protocol.http.xml.codec.HttpXmlResponseDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;

import java.net.InetSocketAddress;

/**
 * Created by ypc on 2017/7/27.
 * 可以参考 http://blog.csdn.net/kingsonyoung/article/details/50524866
 */
public class HttpXmlClient {

	public static void main(String[] args) throws Exception {
		int port = 9090;
		if (args != null && args.length > 0) {
			try {
				port = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				// 采用默认值
			}
		}
		new HttpXmlClient().connect(port);
	}

    public void connect(int port) throws Exception {
		// 配置客户端NIO线程组
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class)
				.option(ChannelOption.TCP_NODELAY, true)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						// 添加 HTTP 响应消息解码器
						ch.pipeline().addLast("http-decoder", new HttpResponseDecoder());
						/**
						 * 添加 HttpObjectAggregator 解码器，作用是
						 * 将多个消息转换为单一的 FullHttpRequest 或者 FullHttpResponse，
						 * 原因是 HTTP 解码器在每个 HTTP 消息中会生成多个消息对象。
						 * 1. HttpRequest ／ HttpResponse;
						 * 2. HttpContent;
						 * 3. LastHttpContent;
						 */
						ch.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65536));
						// 添加 XML 响应消息解码器
						ch.pipeline().addLast("xml-decoder", new HttpXmlResponseDecoder(Order.class, true));
						// 添加 HTTP 请求消息编码器
						ch.pipeline().addLast("http-encoder", new HttpRequestEncoder());
						// 添加 XML 请求消息编码器
						ch.pipeline().addLast("xml-encoder", new HttpXmlRequestEncoder());
						// 业务操作
						ch.pipeline().addLast("xmlClientHandler", new HttpXmlClientHandle());
					}
				});

			// 发起异步连接操作
			ChannelFuture f = b.connect(new InetSocketAddress(port)).sync();

			// 当代客户端链路关闭
			f.channel().closeFuture().sync();
		} finally {
			// 优雅退出，释放NIO线程组
			group.shutdownGracefully();
		}
    }
}
