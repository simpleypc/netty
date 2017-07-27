package com.netty.http.xml.codec;

/**
 * Created by ypc on 2017/7/27.
 * HttpXmlRequestEncoder 继承了 AbstractHttpXmlEncoder，
 * AbstractHttpXmlEncoder 中定义了 encode0 方法，
 * 将 Object 对象转化为对应的xml字符串，然后将返回 xml 字符串的 ByteBuf 对象，
 * 此处没有使用《Netty权威指南2》原书中的 jibx 实现 JavaBean 和 xml 的互相转换，
 * 而是使用了 XStream。方法也很简单，可以参考 http://blog.csdn.net/kingsonyoung/article/details/50524866
 */

import com.netty.http.xml.pojo.Address;
import com.netty.http.xml.pojo.Customer;
import com.netty.http.xml.pojo.Order;
import com.netty.http.xml.pojo.Shipping;
import com.thoughtworks.xstream.XStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.nio.charset.Charset;

public abstract class AbstractHttpXmlEncoder<T> extends MessageToMessageEncoder<T> {
    final static String CHARSET_NAME = "UTF-8";
    final static Charset UTF_8 = Charset.forName(CHARSET_NAME);

    protected ByteBuf encode0(ChannelHandlerContext ctx, Object body) throws Exception {
        // 将Order类转换为xml流
        XStream xStream = new XStream();
        xStream.setMode(XStream.NO_REFERENCES);
        // 注册使用了注解的VO
        xStream.processAnnotations(new Class[] { Order.class, Customer.class, Shipping.class, Address.class });
        String xml = xStream.toXML(body);
        ByteBuf encodeBuf = Unpooled.copiedBuffer(xml, UTF_8);
        return encodeBuf;
    }

    @Skip
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("fail to encode");
    }
}
