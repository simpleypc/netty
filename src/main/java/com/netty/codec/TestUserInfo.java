package com.netty.codec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Created by ypc on 2017/7/20.
 * 测试结果:采用 java 序列化机制编码后的二进制数组大小是二进制编码的 5.29 倍
 */
public class TestUserInfo {
    public static void main(String[] args) throws IOException {
        UserInfo info = new UserInfo();
        info.buildUserID(100).buildUserName("Welcome to Netty");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(bos);
        os.writeObject(info);
        os.flush();
        os.close();
        byte[] b = bos.toByteArray();
        System.out.println("The jdk serializable length is : "+b.length);//109
        bos.close();
        System.out.println("-----------------------");
        System.out.println("The byte array serializable length is : "+info.codeC().length);//24
    }
}
