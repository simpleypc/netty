package com.netty.http.xml.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.util.List;

/**
 * Created by ypc on 2017/7/27.
 */
public class HttpXmlResponseEncoder extends AbstractHttpXmlEncoder<HttpXmlResponse> {

	/**
	 * @param ctx
	 * @param msg
	 * @param out
	 * @throws Exception
     */
    protected void encode(ChannelHandlerContext ctx, HttpXmlResponse msg, List<Object> out) throws Exception {
		ByteBuf body = encode0(ctx, msg.getResult());
		FullHttpResponse response = msg.getHttpResponse();
		if (response == null) {
			response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, body);
		} else {
			response = new DefaultFullHttpResponse(msg.getHttpResponse().getProtocolVersion(),
					msg.getHttpResponse().getStatus(), body);
		}
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/xml");
		HttpHeaders.setContentLength(response, body.readableBytes());
		out.add(response);
    }
}
