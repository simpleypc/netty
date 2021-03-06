package com.netty.source;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledHeapByteBuf;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * Created by ypc on 2017/8/3.
 */
public class TestByteBuf {

    /**
     * ByteBuf 功能说明
     * 常用的缓存区就是 JDK NIO java.nio.Buffer 的实现类
     * 实际上 7种基础类型（Boolean除外）都有自己的缓存区实现。
     * 对于 NIO 编程主要使用 ByteBuffer，但有其局限性，缺点如下：
     *    1. ByteBuffer 长度固定，一旦分配完成，它的容量不能动态扩展和收缩，
     *       当需要编码的 POJO 对象大于 ByteBuffer 的容量时，会发生数组越界异常。
     *    2. ByteBuffer 只有一个标识位置的指针 position，读写的时候需要手工调用 flip() 和 rewind() 等，
     *       使用者必须小心谨慎的处理这些 API，否则很容易导致程序处理失败。
     *    3. ByyeBuffer 的 API 功能有限，一些高级和应用的特性它不支持，需要使用者自己编程实现。
     *
     * 为了弥补这些不足，Netty 提供了自己的 ByteBuffer 实现 -- ByteBuf。
     */

    /**
     * ByteBuf 工作原理
     * ByteBuf 依然是个 Byte 数组的缓冲区，它的基本功能应该与 JDK 的 ByteBuffer 一致，提供一下几类基本功能。
     *    1. 7种 Java 基础类型／byte数组／ByteBuffer（ByteBuf）等的读写；
     *    2. 缓冲区自身的 copy 和 slice 等；
     *    3. 设置网络字节序；
     *    4. 构造缓冲区实例；
     *    5. 操作位置指针等方法；
     * 由于 JDK 的 ByteBuffer 已经提供了这些基础能力的实现。因此 Netty ByteBuf 的实现可以有两种策略。
     *    1. 参考 JDK ByteBuffer 的实现，增加额外的功能，解决原 ByteBuffer 的缺点；
     *    2. 聚合 JDK ByteBuffer，通过 Facade（外观设计）模式对其进行包装，可以减少自身的代码量，降低实现成本；
     */

    /**
     * JDK ByteBuffer 由于只有一个位置指针用于处理读写操作，因此每次读写的时候都需要
     * 额外调用 flip 或 clear 方法，否则功能将出错，它的典型用法如下。
     */
    @Test
    public void TestByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(88);
        String value = "Netty 测试";
        buffer.put(value.getBytes());
        //buffer.flip(); // 如果不做 flip 操作，读到的将是 position 到 capacity 之间的错误内容如：     ...    
        byte[] vArray = new byte[buffer.remaining()];
        buffer.get(vArray);
        String decodeValue = new String(vArray);
        System.out.println(decodeValue);
    }


    @Test
    public void TestByteBuf() {
        ByteBuf buffer = Unpooled.compositeBuffer(8);
        buffer.writeInt(2);
        
        ByteBuf buf = Unpooled.compositeBuffer(20);
        buf.writeInt(1);
        buf.writeInt(2);
        buf.writeInt(3);
        buf.readBytes(buffer, 0, 8);

        buffer.readerIndex(0);
        int readInt = buffer.readInt();

        buf.readerIndex(0);

        // 读 buf 数据
        byte[] data = new byte[buf.writerIndex()-buf.readerIndex()];
        buf.readBytes(data, 0, 12);

        String returnStr = byte4ToInt(data);
        System.out.println(returnStr);

        buffer.clear();
        buffer.writeBytes(data, 0, 12);
    }

    /**byte 转换为 int*/
    public static String byte4ToInt(byte[] arr) {
        if (arr == null || arr.length%4 != 0)
            throw new IllegalArgumentException("byte数组必须不为空,并且是4位!");

        String returnStr = "";
        for (int i = 0; i < arr.length/4; i++) {
            int j = (int) (((arr[(i+1)*4-4] & 0xff) << 24) | ((arr[(i+1)*4-3] & 0xff) << 16) | ((arr[(i+1)*4-2] & 0xff) << 8) | ((arr[(i+1)*4-1] & 0xff)));
            returnStr += j;
        }
        return returnStr;
    }

    /**
     * package io.netty.buffer;
     * ByteBuf 动态扩展容量 AbstractByteBuf.calculateNewCapacity 解析
     * 先采用倍增扩张机制 以4M为阀值进行倍增
     * 后采用步进算法 以64为计数进行倍增
     *
     * 采用先倍增后步进的原因 如下：
     * 当内存比较小的情况下，倍增操作并不会带来太多的内存浪费 例如：64字节 --> 128字节 --> 256字节
     * 这样的内存扩张方式对于大多数应用系统是可以接受的。
     *
     * 但是当内存增长到一定阀值后，再进行倍增就可能会带来额外的内存浪费 例如：10MB --> 20MB
     * 但很可能系统只需要 12MB，扩张到 20MB 后会带来 8MB 的内存浪费。
     *
     * 由于每个客户端连接都可能维护自己独立的接收和发送缓冲区，这样随着客户读的线性增长，
     * 内存浪费也会成比例增加，因此，达到某个阀值后就需要以步进的方式对内存进行平滑的扩张。
     */
    /*private int calculateNewCapacity(int minNewCapacity) {
        final int maxCapacity = this.maxCapacity;
        final int threshold = 1048576 * 4; // 4 MiB page
        if (minNewCapacity == threshold)
            return threshold;
        // 步进算法
        if (minNewCapacity > threshold) {
            int newCapacity = minNewCapacity / threshold * threshold;
            if (newCapacity > maxCapacity - threshold)
                newCapacity = maxCapacity;
             else
                newCapacity += threshold;
            return newCapacity;
        }

        // 倍增算法
        int newCapacity = 64;
        while (newCapacity < minNewCapacity) {
            newCapacity <<= 1;
        }
        return Math.min(newCapacity, maxCapacity);
    }*/


    /**
     * package io.netty.buffer;
     * ByteBuf 动态扩展缓冲区 UnpooledHeapByteBuf.capacity(int newCapacity) 解析
     * 1. 方法入口首先对新容量进行合法性校验，如果大于容量上限或者小于 0，则抛出异常
     * 2. 判断新的容量只是否大于当前的缓冲区容量。
     * 3. 如果大于则需要进行动态扩展
     *      1. 需要动态扩展，通过 newCapacity 创建新的缓冲区字节数组。
     *      2. 通过 System.arraycopy 进行内存复制，将旧的字节数组复制到新创建的字节数组中
     *      3. 调用 setArray 替换旧的字节数组。
     * 4. 如果小于则需要创建子缓冲区
     *      1. 需要截取当前缓冲区创建一个新的子缓冲区，具体算法如下
     *      2. 首先判断下读索引是否小于新的容量值
     *      3. 如果小于
     *          1. 进一步判断写索引是否大于新的容量值
     *              1. 如果大于则将写索引设置为新的容量值(防止越界)
     *          2. 通过内存复制 System.arraycopy 将当前可读的字节数组复制到新创建的子缓冲区中
     *      4. 如果不小于，说明没有可读的字节数组需要复制到新创建的缓冲区中，将读写索引设置为新的容量值即可
     *      5. 调用 setArray 替换旧的字节数组
     */
    /*public ByteBuf capacity(int newCapacity) {
        ensureAccessible();
        if (newCapacity < 0 || newCapacity > maxCapacity()) {
            throw new IllegalArgumentException("newCapacity: " + newCapacity);
        }

        int oldCapacity = array.length;
        if (newCapacity > oldCapacity) {
            byte[] newArray = new byte[newCapacity];
            System.arraycopy(array, 0, newArray, 0, array.length);
            setArray(newArray);
        } else if (newCapacity < oldCapacity) {
            byte[] newArray = new byte[newCapacity];
            int readerIndex = readerIndex();
            if (readerIndex < newCapacity) {
                int writerIndex = writerIndex();
                if (writerIndex > newCapacity) {
                    writerIndex(writerIndex = newCapacity);
                }
                System.arraycopy(array, readerIndex, newArray, readerIndex, writerIndex - readerIndex);
            } else {
                setIndex(newCapacity, newCapacity);
            }
            setArray(newArray);
        }
        return this;
    }*/



}
