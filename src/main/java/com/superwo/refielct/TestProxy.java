package com.superwo.refielct;

import com.superwo.pojo.IExample;
import com.superwo.pojo.IUser;

/**
 * Created by yangpengcheng on 2017/6/22.
 * 不实现接口，直接通过反射到MAP中的方式创建对象并调用方法
 */
public class TestProxy {

    public static void main(String[] args) {
        IExample example = (IExample)MyProxyView.newInstance(new Class[]{IExample.class});

        IUser user = (IUser)MyProxyView.newInstance(new Class[]{IUser.class});

        // aduit bean 1
        example.setName("my example");
        example.setDesc("my proxy example");
        // aduit bean 2
        user.setUserID("jia20003");
        user.setUserName("gloomyfish");

        System.out.println("exmaple name : " + example.getName());
        System.out.println("exmaple desc : " + example.getDesc());
        System.out.println();
        System.out.println("user ID : " + user.getUserID());
        System.out.println("user name : " + user.getUserName());
    }

}
