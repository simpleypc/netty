package com.netty.nio;

/**
 * Created by ypc on 2017/3/16.
 */
public class TimeClient {
    public static void main(String[] args) {
        int port = 9090;
        if(args != null && args.length > 0){
            try{
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e){
                e.printStackTrace();
            }
        }
        new Thread(new TimeClientHandle("127.0.0.1", port), "TimeClient-001").start();
    }
}
