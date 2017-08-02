package com.netty.protocol.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by ypc on 2017/7/27.
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger logger = Logger.getLogger(WebSocketServerHandler.class.getName());

    private WebSocketServerHandshaker handshaker;

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 传统的 Http 接入
        if (msg instanceof FullHttpRequest)
            handleHttpRequest(ctx, (FullHttpRequest)msg);
        // WebSocket 接入
        else if (msg instanceof WebSocketFrame)
            handleWebSocketFrame(ctx, (WebSocketFrame)msg);
        //ctx.flush();// 详见 channelReadComplete
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest msg) {
        // 如果 Http 解码失败，返回 Http 异常
        // 如果它的值不是 websocket， 或者信息头中没有包含 Upgrade 字段，返回 Http 400 响应
        if (!msg.getDecoderResult().isSuccess() || (!"websocket".equals(msg.headers().get("Upgrade")))) {
            sendHttpResponse(ctx, msg, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }

        // 构造握手响应返回，本机测试
        // 开始构造握手工厂，创建握手处理类 WebSocketServerHandshakerFactory，通过它构造握手响应消息返回给客户端。
        // 同时将 WebSocket 相关的编码类与解码类动态添加到 ChannelPipeline 中，用于 WebSocket 消息的编解码。
        WebSocketServerHandshakerFactory wsFactory =
                new WebSocketServerHandshakerFactory("ws://localhost:9090/websocket", null, false);
        handshaker = wsFactory.newHandshaker(msg);
        if (handshaker == null){
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), msg);
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame msg) {
        // 判断是否是关闭链路的命令
        if (msg instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) msg.retain());
            return;
        }

        // 判断是否是 Ping 消息
        if (msg instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(msg.content().retain()));
            return;
        }

        // 本例程仅支持文本消息，不支持二进制消息
        if (!(msg instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(
                    String.format("%s frame types not supported", msg.getClass().getName()));
        }

        //返回应答消息
        String request = ((TextWebSocketFrame)msg).text();
        if (logger.isLoggable(Level.FINE)) {//Level.FINE 级别日志是否有效
            logger.fine(String.format("%s received %s", ctx.channel(), request));
        }

        ctx.channel().write(new TextWebSocketFrame(
                request+", 欢迎使用 Netty WebSocket 服务, 现在时刻: "+new Date().toString()));
    }

    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest msg, FullHttpResponse resp) {
        // 返回应答给客户端
        if (resp.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(resp.getStatus().toString(), CharsetUtil.UTF_8);
            resp.content().writeBytes(buf);
            buf.release();
            HttpHeaders.setContentLength(resp, resp.content().readableBytes());
        }

        // 如果是非 Keep-Alive. 关闭连接
        ChannelFuture cf = ctx.channel().writeAndFlush(resp);
        if (!HttpHeaders.isKeepAlive(msg) || resp.getStatus().code() != 200) {
            cf.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    //类似于 try catch finally 中的 finally 最终执行的方法
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
