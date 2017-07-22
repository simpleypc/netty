package com.netty.codec.messagePack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.msgpack.MessagePack;

/**
 * Created by ypc on 2017/7/21.
 * MessagePack 编码器开发
 */
public class MessagePackEncoder extends MessageToByteEncoder<Object> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        MessagePack messagePack = new MessagePack();
        // serialize
        byte[] raw = messagePack.write(msg);
        ByteBuf byteBuf = out.writeBytes(raw);
        System.out.println(byteBuf);
    }
}
