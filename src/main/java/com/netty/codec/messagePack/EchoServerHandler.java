package com.netty.codec.messagePack;

import com.netty.codec.UserInfo;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

/**
 * Created by ypc on 2017/4/3.
 */
public class EchoServerHandler extends ChannelHandlerAdapter{

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        List<UserInfo> userInfos = (List<UserInfo>) msg;
        System.out.println("Server receive the messagePack message : " + userInfos.toString());
        ctx.writeAndFlush(userInfos);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
