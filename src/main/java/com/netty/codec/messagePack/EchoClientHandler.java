package com.netty.codec.messagePack;

import com.netty.codec.UserInfo;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by ypc on 2017/6/20.
 */
public class EchoClientHandler extends ChannelHandlerAdapter {

    private static final Logger logger = Logger.getLogger(EchoClientHandler.class.getName());

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        for (UserInfo info:userInfo(1000)) {
           ctx.write(info);
        }
        ctx.flush();
    }

    private List<UserInfo> userInfo(int sendNum) {
        List<UserInfo> userInfos = new ArrayList<UserInfo>();
        UserInfo userInfo = null;
        for (int i = 1; i <= sendNum; i++) {
            userInfo = new UserInfo();
            userInfo.setUserID(i);
            userInfo.setUserName("YangPengCheng ---> ");
            userInfos.add(userInfo);
        }
        return userInfos;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("Client receive the messagePack message : " + msg);
        //ctx.write(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warning("Unexpected exception from downstream : "+cause.getMessage());//释放资源
        cause.printStackTrace();
        ctx.close();
    }
}