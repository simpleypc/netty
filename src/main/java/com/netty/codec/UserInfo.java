package com.netty.codec;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * Created by ypc on 2017/6/22.
 */
public class UserInfo implements Serializable {

    private static final long serialVersionUID = -2082634751978299464L;

    private String userName;

    private int userID;

    public UserInfo buildUserName(String userName){
        this.userName = userName;
        return this;
    }

    public UserInfo buildUserID(int userID){
        this.userID = userID;
        return this;
    }

    public void setUserName(String userName){
        this.userName = userName;
    }

    public String getUserName(){
        return userName;
    }

    public void setUserID(int userID){
        this.userID = userID;
    }

    public int getUserID(){
        return userID;
    }

    public byte[] codeC(){
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        byte[] value = this.userName.getBytes();
        buffer.putInt(value.length);
        buffer.put(value);
        buffer.putInt(this.getUserID());
        buffer.flip();
        value = null;
        byte[] result = new byte[buffer.remaining()];
        buffer.get(result);
        return result;
    }

    public byte[] codeC(ByteBuffer buffer){
        buffer.clear();
        byte[] value = this.userName.getBytes();
        buffer.putInt(value.length);
        buffer.put(value);
        buffer.putInt(this.getUserID());
        buffer.flip();
        value = null;
        byte[] result = new byte[buffer.remaining()];
        buffer.get(result);
        return result;
    }
}
