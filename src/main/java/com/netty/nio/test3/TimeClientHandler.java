package com.netty.nio.test3;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.logging.Logger;

/**
 * Created by ypc on 2017/6/20.粘包 拆包
 */
public class TimeClientHandler extends ChannelHandlerAdapter {

    private static final Logger logger = Logger.getLogger(TimeClientHandler.class.getName());

    private int counter;

    private byte[] req;

    public TimeClientHandler() {
        // System.getProperty("line.separator") 获取换行符
        // 功能和"\n"是一致的,但是此种写法屏蔽了 Windows和Linux的区别 ，更保险一些.
        //req = ("QUERY TIME ORDER" + System.getProperty("line.separator")).getBytes();
        req = ("QUERY TIME ORDER" + "YPC" + System.getProperty("line.separator")).getBytes();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ByteBuf message = null;
        for (int i = 0; i < 100; i++){
            message = Unpooled.buffer(req.length);//创建堆缓冲区
            message.writeBytes(req);
            ctx.writeAndFlush(message);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String body = (String) msg;
        System.out.println("Now is : " + body + " ; the counter is : " + ++counter);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //释放资源
        logger.warning("Unexpected exception from downstream : "+cause.getMessage());
        ctx.close();
    }
}
