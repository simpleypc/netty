package com.netty.codec.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ypc on 2017/7/24.
 * Mac 上搭建 Protobuf 编译环境及简单使用 安装
 *
 */
public class TestSubscribeReqProto {

    public static void main(String[] args) throws InvalidProtocolBufferException {
        SubscribeReqProto.SubscribeReq req = createSubscribeReq();
        System.out.println("Before encode : \n" + req.toString());

        SubscribeReqProto.SubscribeReq req2 = decode(encode(req));

        System.out.println("After decode : \n" + req.toString());

        System.out.println("Assert equal : --> " + req2.equals(req));
    }

    // 编码通过调用 SubscribeReqProto.SubscribeReq 实例的 toByteArray() 编码成 byte 数组
    private static byte[] encode(SubscribeReqProto.SubscribeReq req){
        return req.toByteArray();
    }

    // 解码通过调用 SubscribeReqProto.SubscribeReq 的静态方法 parseFrom(byte[]) 将二进制解码为原始对象
    private static SubscribeReqProto.SubscribeReq decode(byte[] body) throws InvalidProtocolBufferException {
        return SubscribeReqProto.SubscribeReq.parseFrom(body);
    }

    private static SubscribeReqProto.SubscribeReq createSubscribeReq(){
        // 通过静态方法 newBuilder 创建 SubscribeReqProto.SubscribeReq 的 Builder 实例。
        SubscribeReqProto.SubscribeReq.Builder builder =
                SubscribeReqProto.SubscribeReq.newBuilder();
        // 通过 builder 对属性进行设置
        builder.setSubReqID(1);
        builder.setUserName("YangPengCheng");
        builder.setProductName("Netty Study");

        // 集合类型 通过 addAllXXX() 方法可以将集合对象设置到对应的属性中。
        List<String> address = new ArrayList<String>();
        address.add("BeiJing");
        address.add("TianJin");
        address.add("HaiXing");
        builder.addAllAddress(address);
        return builder.build();
    }
}
