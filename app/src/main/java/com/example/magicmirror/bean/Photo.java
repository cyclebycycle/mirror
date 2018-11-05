package com.example.magicmirror.bean;

/**
 * Created by Administrator on 2017/11/2.
 */

public class Photo {
    private String time;
    private String result;

    public Photo(String time, String result) {
        this.time = time;
        this.result = result;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
