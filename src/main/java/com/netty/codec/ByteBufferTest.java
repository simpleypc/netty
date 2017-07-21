package com.netty.codec;

import java.nio.ByteBuffer;

/**
 * Created by ypc on 2017/7/20.
 */
public class ByteBufferTest {

    public static void main(String[] args) {

        //10个字节大小
        ByteBuffer buffer = ByteBuffer.allocate(10);

        //容量是10，EOF位置是10，初始位置也是10
        println(buffer.capacity());
        println(buffer.limit());

        //输出看看，输出是10个0
        printBuffer(buffer);

        //此时，指针指向位置10，已经是最大容量了。
        //把指针挪回位置1
        buffer.rewind();

        //写操作，指针会自动移动
        buffer.putChar('a');
        println(buffer.position()); //指针指向2
        buffer.putChar('啊');
        println(buffer.position()); //指针指向4

        //当前位置设置为EOF，指针挪回位置1
        //相当于下面两句：
        //buffer.limit(4);
        //buffer.position(0);
        buffer.flip();

        //输出前4个字节看看，输出是0 61 55 4a
        printBuffer(buffer);

        //指针挪到位置1，压缩一下
        //输出是61 55 4a 4a 0 0 0 0 0 0
        //compact方法会把EOF位置重置为最大容量，这里就是10
        buffer.position(1);
        buffer.compact();
        printBuffer(buffer);

        //注意当前指针指向3，继续写入数据的话，就会覆盖后面的数据了。
        println(buffer.position());

    }

    /**
     * 输出buffer内容.
     */
    public static void printBuffer(ByteBuffer buffer){

        //记住当前位置
        int p = buffer.position();

        //指针挪到0
        buffer.position(0);

        //循环输出每个字节内容
        for(int i=0;i<buffer.limit();i++){
            byte b = buffer.get(); //读操作，指针会自动移动
            println(Integer.toHexString(b));
        }

        //指针再挪回去
        buffer.position(p);

        //本想用mark()和reset()来实现。
        //但是，它们貌似只能正向使用。
        //如，位置6的时候，做一下Mark，
        //然后在位置10（位置要大于6）的时候，用reset就会跳回位置6.

        //而position(n)这个方法，如果之前做了Mark，但是Mark位置大于新位置，Mark会被清除。
        //也就是说，做了Mark后，只能向前跳，不能往回跳，否则Mark就丢失。
        //rewind()方法，更干脆，直接清除mark。
        //flip()方法，也清除mark
        //clear()方法，也清除mark
        //compact方法，也清除mark

        //所以，mark方法干脆不要用了，自己拿变量记一下就完了。
    }

    public static void println(Object o){
        System.out.println(o);
    }

}
