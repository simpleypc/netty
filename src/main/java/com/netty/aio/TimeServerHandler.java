package com.netty.aio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

/**
 * Created by ypc on 2017/3/15.
 * 同步阻塞I/O
 * 当有新的客户端接入的时候，以 Socket 为参数构造 TimeServerHandler 对象，
 * TimeServerHandler 是一个 Runnable，使用它的构造函数的参数创建一个新的
 * 客户端相城处理这条 Socket 链路。
 */
public class TimeServerHandler implements Runnable {

    private Socket socket;

    public TimeServerHandler(Socket socket){
        this.socket = socket;
    }

    public void run() {
        BufferedReader in = null;
        PrintWriter out = null;

        try {
            in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            out = new PrintWriter(this.socket.getOutputStream(), true);
            String currentTime = null;
            String body = null;
            while(true){
                body = in.readLine();
                if (body == null) break;
                System.out.println("the time server receive order :"+body);
                currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ?
                        new Date(System.currentTimeMillis()).toString() :
                        "BAD ORDER";
                out.println(currentTime);
            }
        } catch (IOException e) {
            if (in != null){
                try {
                    in.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (out != null){
                out.close();
            }
            if (this.socket != null){
                try {
                    this.socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                this.socket = null;
            }
        }
    }
}
