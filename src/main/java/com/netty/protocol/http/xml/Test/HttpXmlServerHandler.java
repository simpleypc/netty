package com.netty.protocol.http.xml.test;


import com.netty.protocol.http.xml.codec.HttpXmlRequest;
import com.netty.protocol.http.xml.codec.HttpXmlResponse;
import com.netty.protocol.http.xml.pojo.Address;
import com.netty.protocol.http.xml.pojo.Order;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ypc on 2017/7/27.
 */
public class HttpXmlServerHandler extends SimpleChannelInboundHandler<HttpXmlRequest> {

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, HttpXmlRequest xmlRequest) throws Exception {
		HttpRequest request = xmlRequest.getRequest();
		Order order = (Order) xmlRequest.getBody();
		// 输出解码获得的Order对象
		System.out.println("Http server receive request : " + order);
		dobusiness(order);
		ChannelFuture future = ctx.writeAndFlush(new HttpXmlResponse(null, order));
		if (!HttpHeaders.isKeepAlive(request)) {//request.headers().get(CONNECTION) != KEEP_ALIVE
			future.addListener(new GenericFutureListener<Future<? super Void>>() {
				public void operationComplete(Future future) throws Exception {
					ctx.close();
				}
			});
		}
    }

    private void dobusiness(Order order) {
		order.getCustomer().setFirstName("狄");
		order.getCustomer().setLastName("仁杰");
		List<String> midNames = new ArrayList<String>();
		midNames.add("李元芳");
		order.getCustomer().setMiddleNames(midNames);
		Address address = order.getBillTo();
		address.setCity("洛阳");
		address.setCountry("大唐");
		address.setState("河南道");
		address.setPostCode("123456");
		order.setBillTo(address);
		order.setShipTo(address);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		// 在链路没有关闭并且出现异常的时候发送给客户端错误信息
		if (ctx.channel().isActive()) {
			sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
		}
    }

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
				Unpooled.copiedBuffer("失败: " + status.toString() + "\r\n", CharsetUtil.UTF_8));
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
