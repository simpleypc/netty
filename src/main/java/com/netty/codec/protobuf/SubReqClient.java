package com.netty.codec.protobuf;

import com.netty.codec.messagePack.MessagePackDecoder;
import com.netty.codec.messagePack.MessagePackEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

/**
 * Created by ypc on 2017/6/20.
 * Protobuf 版本的图书订购客户端开发
 */
public class SubReqClient {

    public static void main(String[] args) throws Exception {
        int port = 9090;
        if(args != null && args.length > 0){
            try{
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e){
                e.printStackTrace();
            }
        }
        new SubReqClient().connect(port, "127.0.0.1");
    }

    public void connect(int port, String host) throws Exception {
        //配置客户端 NIO 线程组
        EventLoopGroup group = new NioEventLoopGroup();
        try{
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 用于半包处理
                            socketChannel.pipeline().addLast(new ProtobufVarint32FrameDecoder());

                            // 添加解码器 参数 com.google.protobuf.MessageLite
                            // 实际上是告诉 ProtobufDecoder 需要解码的目标类是什么
                            SubscribeRespProto.SubscribeResp subscribeResp = SubscribeRespProto.SubscribeResp.getDefaultInstance();
                            socketChannel.pipeline().addLast(new ProtobufDecoder(subscribeResp));

                            // 一个编码器，用于添加Google协议缓冲区
                            socketChannel.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());

                            // 添加编码器
                            socketChannel.pipeline().addLast(new ProtobufEncoder());

                            socketChannel.pipeline().addLast(new SubReqClientHandler());
                        }
                    });
            //发起异步连接操作
            ChannelFuture future = bootstrap.connect(host, port).sync();

            //等待客户端链路关闭
            future.channel().closeFuture().sync();
        } finally {
            //优雅退出，释放 NIO 线路组
            group.shutdownGracefully();
        }
    }
}
