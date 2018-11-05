package com.example.magicmirror.bean;

/**
 * Created by Administrator on 2017/10/12.
 */

public class PhotoMessage {
    private int states;
    private String time;
    private String result;
    private String photoAdd;

//    public PhotoMessage(int states, String time) {
//        this.states = states;
//        this.time = time;
//    }

    public PhotoMessage() {
    }

    public int getStates() {
        return states;
    }

    public String getTime() {
        return time;
    }
    public String getResult() {
        return result;
    }
    public String getPhotoAdd() {
        return photoAdd;
    }
}
