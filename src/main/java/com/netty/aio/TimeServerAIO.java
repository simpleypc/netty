package com.netty.aio;

import com.netty.bio.TimeServerHandler;
import com.netty.bio.TimeServerHandlerExecutePool;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 伪异步 I/O 服务端
 * Created by ypc on 2017/3/15.
 * 为了解决同步阻塞 I/O 面临的一个链路需要一个线程处理的问题，后来对它的线程模型进行了优化
 * --后端通过一个线程池来处理多个客户端的请求接入，形成客户端个数 M：线程池最大线程数 N
 * 的比例关系，其中 M 可以远远大于 N。通过线程池可以灵活地调配线程资源，设置线程的最大值，
 * 防止由于海量并发接入导致线程耗尽。
 */
public class TimeServerAIO {
    public static void main(String[] args) {
        int port = 9090;
        if(args != null && args.length > 0){
            try{
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e){
                e.printStackTrace();
            }
        }

        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
            System.out.println("the time serer is start in port : "+port);
            Socket socket = null;
            TimeServerHandlerExecutePool  singleExecutor =
                    new TimeServerHandlerExecutePool(50, 10000);//创建I/O任务线程池
            while(true){
                socket = server.accept();
                /*try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                singleExecutor.execute(new TimeServerHandler(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (server != null){
                System.out.println("the time server close");
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                server = null;
            }
        }
    }
}
