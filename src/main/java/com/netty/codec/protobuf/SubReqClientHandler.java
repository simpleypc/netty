package com.netty.codec.protobuf;

import com.netty.codec.UserInfo;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by ypc on 2017/6/20.
 */
public class SubReqClientHandler extends ChannelHandlerAdapter {

    private static final Logger logger = Logger.getLogger(SubReqClientHandler.class.getName());

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        for (SubscribeReqProto.SubscribeReq subscribeReq:subscribeReqList(10)) {
           ctx.write(subscribeReq);
        }
        ctx.flush();
    }

    private List<SubscribeReqProto.SubscribeReq> subscribeReqList(int sendNum) {
        List<SubscribeReqProto.SubscribeReq> subscribeReqs = new ArrayList<SubscribeReqProto.SubscribeReq>();
        SubscribeReqProto.SubscribeReq.Builder builder = null;
        for (int i = 1; i <= sendNum; i++) {
            // 通过静态方法 newBuilder 创建 SubscribeReqProto.SubscribeReq 的 Builder 实例。
            builder = SubscribeReqProto.SubscribeReq.newBuilder();
            // 通过 builder 对属性进行设置
            builder.setSubReqID(i);
            builder.setUserName("YangPengCheng");
            builder.setProductName("Netty Book For ProtoBuf");
            // 集合类型 通过 addAllXXX() 方法可以将集合对象设置到对应的属性中。
            List<String> address = new ArrayList<String>();
            address.add("BeiJing");
            address.add("TianJin");
            address.add("HaiXing");
            builder.addAllAddress(address);
            subscribeReqs.add(builder.build());
        }
        return subscribeReqs;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("Receive server response : [\n" + msg + "]");
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