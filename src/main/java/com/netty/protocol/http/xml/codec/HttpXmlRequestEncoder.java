package com.netty.protocol.http.xml.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.net.InetAddress;
import java.util.List;

/**
 * Created by ypc on 2017/7/27.
 * HttpXml 编码器开发
 */
public class HttpXmlRequestEncoder extends AbstractHttpXmlEncoder<HttpXmlRequest> {

    /**
     * encode 方法中接收到的的 msg 就是之前 HttpXmlClientHandle 中我们写入 ChannelPipeLine 中的对象。
     * HttpXmlRequestEncoder 获得的 msg 中的 Object 对象，通过父类的 encode0 方法将其转换为 Bytebuf对象。
     * 因为这个例子中上述 request 必定为 null，所以会构造新的 FullHttpRequest 对象。
     * 在构造方法中将数据 body 传入 FullHttpRequest 对象中。
     * 最后要记得设置 CONTENT_LENGTH 请求头，并将 request 对象添加到 out 对象中。
     * @param ctx
     * @param msg
     * @param out
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, HttpXmlRequest msg, List<Object> out) throws Exception {
        // 调用父类的encode0方法将Order对象转换为xml字符串，并将其封装为ByteBuf
        ByteBuf body = encode0(ctx, msg.getBody());
        FullHttpRequest request = msg.getRequest();
        // 如 request 为空，则新建一个 FullHttpRequest 对象，并将设置消息头
        if (request == null) {
            // 在构造方法中，将 body 设置为请求消息体
            request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/do", body);
            HttpHeaders headers = request.headers();
            // 表示请求的服务器网址
            headers.set(HttpHeaders.Names.HOST, InetAddress.getLocalHost().getHostAddress());
            // Connection 表示客户端与服务连接类型；Keep-Alive 表示长连接；CLOSE 表示短连接
            // header中包含了值为close的connection，都表明当前正在使用的tcp链接在请求处理完毕后会被断掉。
            // 以后client再进行新的请求时就必须创建新的tcp链接了。
            headers.set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
            // 浏览器支持的压缩编码是 gzip 和 deflate
            headers.set(HttpHeaders.Names.ACCEPT_ENCODING,
                    HttpHeaders.Values.GZIP.toString()+','+HttpHeaders.Values.DEFLATE.toString());
            // 浏览器支持的解码集
            headers.set(HttpHeaders.Names.ACCEPT_CHARSET, "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
            // 浏览器支持的语言
            headers.set(HttpHeaders.Names.ACCEPT_LANGUAGE, "zh");
            // 使用的用户代理是 Netty xml Http Client side
            headers.set(HttpHeaders.Names.USER_AGENT, "Netty xml Http Client side");
            // 浏览器支持的 MIME 类型,优先顺序为从左到右
            headers.set(HttpHeaders.Names.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        }
        // 由于此处没有使用 chunk 方式，所以要设置消息头中设置消息体的 CONTENT_LENGTH
        HttpHeaders.setContentLength(request, body.readableBytes());
        // 将请求消息添加进out中，待后面的编码器对消息进行编码
        out.add(request);
    }
}
