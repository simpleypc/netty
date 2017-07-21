package com.netty.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 同步阻塞I/O 服务端
 * 采用BIO通信模型的服务器，通常由一个独立的 Acceptor 线程负责监听客户端的连接，
 * 它接收到客户端连接请求之后为每个客户端创建一个新的线程进行链路处理，处理完成之后，
 * 通过输出流返回应答给客户端，线程销毁。典型的一请求一应答通信模型。
 * Created by ypc on 2017/3/14.
 */
public class TimeServerBIO {
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
            while(true){
                socket = server.accept();
                /*try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                new Thread(new TimeServerHandler(socket)).start();
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
