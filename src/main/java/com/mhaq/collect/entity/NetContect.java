package com.mhaq.collect.entity;



/**
 * @author xutao
 * @date 2021/4/7 5:43 下午
 * @description 局域网连接类
 */

public class NetContect {

    private String host;

    private String userName;

    private String passWord;

    private String path;

    public NetContect(String host, String userName, String passWord, String path) {
        this.host = host;
        this.userName = userName;
        this.passWord = passWord;
        this.path = path;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
