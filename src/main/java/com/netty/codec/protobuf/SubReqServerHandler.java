package com.netty.codec.protobuf;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by ypc on 2017/4/3.
 */
public class SubReqServerHandler extends ChannelHandlerAdapter{

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SubscribeReqProto.SubscribeReq req  = (SubscribeReqProto.SubscribeReq)msg;
        if ("YangPengCHeng".equalsIgnoreCase(req.getUserName())) {
            System.out.println("Server accept client subscribe req : [\n" + req.toString() + "]");
            ctx.writeAndFlush(resp(req.getSubReqID()));
        }
    }

    private SubscribeRespProto.SubscribeResp resp(int subReqID) {
        SubscribeRespProto.SubscribeResp.Builder builder =
                SubscribeRespProto.SubscribeResp.newBuilder();
        // 通过 builder 对属性进行设置
        builder.setSubReqID(subReqID);
        builder.setRespCode(0);
        builder.setDesc("Netty Study order succeed, 3 days later, sent to the designated address");
        return builder.build();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
