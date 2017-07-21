package com.netty.nio.test4;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by ypc on 2017/4/3.粘包 拆包
 */
public class EchoServerHandler extends ChannelHandlerAdapter{

    int counter = 0;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        String body = (String) msg;
        System.out.println("This is " + ++counter + " times receive client : [" + body + "]");

        body += "YPC";
        ByteBuf echo = Unpooled.copiedBuffer(body.getBytes());//创建直接缓冲区
        ctx.writeAndFlush(echo);//ctx.write(echo) 方法导致，客户端接收到数据，但无法打印
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
