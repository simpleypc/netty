package com.netty.codec.messagePack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.msgpack.MessagePack;
import org.msgpack.type.Value;

import java.util.List;

/**
 * Created by ypc on 2017/7/21.
 * MessagePack 解码器开发
 */
public class MessagePackDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        final byte[] array;
        final int length = msg.readableBytes();
        array = new byte[length];
        // 从数据报 msg 中获取需要解码的 byte 数组
        msg.getBytes(msg.readerIndex(), array, 0, length);

        MessagePack messagePack = new MessagePack();
        // 调用 MessagePack 的 read 方法 将其反序列化为 Object 对象
        Value read = messagePack.read(array);

        // 将解码后的对象加入到编码列表 out 中，完成解码操作
        out.add(read);
    }
}
