package com.netty.http.xml.xstream;

import com.netty.http.xml.pojo.Address;
import com.netty.http.xml.pojo.Order;
import com.netty.http.xml.pojo.OrderFactory;
import com.thoughtworks.xstream.XStream;


/**
 * Created by ypc on 2017/7/27.
 * 测试 XStream
 */
public class TestXStream {
    public static void main(String[] args) {
        String res = toXML();
        toEntity(res);
    }

    public static void toEntity(String inputXML) {
        XStream xs = new XStream();
        xs.setMode(XStream.NO_REFERENCES);
        // 注册使用了注解的VO
        xs.processAnnotations(new Class[] { Order.class, Address.class });
        Order person = (Order) xs.fromXML(inputXML);
        System.out.println(person.toString());
    }

    public static String toXML() {
        XStream xStream = new XStream();
        Order order = OrderFactory.create(123);
        xStream.setMode(XStream.NO_REFERENCES);
        // 注册使用了注解的VO
        xStream.processAnnotations(new Class[] { Order.class, Address.class });
        String xml = xStream.toXML(order);
        System.out.println(xml);
        return xml;
    }
}
