package com.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@SuppressWarnings("rawtypes")
public class EchoClientHandler extends SimpleChannelInboundHandler {

	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		byte[] body = "Netty rocks! 现在时间req?".getBytes();
		ByteBuf message = Unpooled.buffer(body.length);
		message.writeBytes(body);
		ctx.writeAndFlush(message);
		//ctx.writeAndFlush("Netty rocks! 现在时间req?");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println("success req is body:" + msg.toString());
		
	}

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println("释放资源success req is body:" + msg.toString());
	}
}
