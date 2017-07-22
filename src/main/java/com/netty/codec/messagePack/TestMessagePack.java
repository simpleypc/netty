package com.netty.codec.messagePack;

import org.msgpack.MessagePack;
import org.msgpack.template.Templates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ypc on 2017/7/21.
 * 测试 MessagePack 编译器
 */
public class TestMessagePack {

    public static void main(String[] args) throws IOException {
        List<String> src = new ArrayList<String>();
        src.add("magpack1");
        src.add("magpack2");
        src.add("magpack3");
        MessagePack messagePack = new MessagePack();
        byte[] raw = messagePack.write(src);

        List<String> read =
                messagePack.read(raw, Templates.tList(Templates.TString));
        System.out.println(read.get(0));
        System.out.println(read.get(1));
        System.out.println(read.get(2));
    }

}
