package com.netty.aio;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 伪异步I/O
 * Created by ypc on 2017/3/15.
 * 首先创建一个时间服务器处理类的线程池，当接收到新的客户端连接时，
 * 将请求 Socket 封装成一个 Task，然后调用线程池的 execute 方法执行，
 * 从而避免了每个请求都创建一个新的线程。
 */
public class TimeServerHandlerExecutePool {

    private ExecutorService executor;

    public TimeServerHandlerExecutePool(int maxPoolSize, int queueSize) {
        executor = new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors(),
                maxPoolSize,
                120L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(queueSize));
    }

    public void execute(Runnable task){
        executor.execute(task);
    }
}
