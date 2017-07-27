package com.netty.http.xml.Test;

import com.netty.http.xml.codec.HttpXmlRequest;
import com.netty.http.xml.codec.HttpXmlResponse;
import com.netty.http.xml.pojo.OrderFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by ypc on 2017/7/27.
 */
public class HttpXmlClientHandle extends SimpleChannelInboundHandler<HttpXmlResponse> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // 给客户端发送请求消息，HttpXmlRequest 包含 FullHttpRequest 和 Order
		HttpXmlRequest request = new HttpXmlRequest(null, OrderFactory.create(123));
		ctx.writeAndFlush(request);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, HttpXmlResponse msg) throws Exception {
		System.out.println("The client receive response of http header is : " + msg.getHttpResponse().headers().names());
		System.out.println("The client receive response of http body is : " + msg.getResult());
    }
}
