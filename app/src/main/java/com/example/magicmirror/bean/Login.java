package com.example.magicmirror.bean;

/**
 * Created by Administrator on 2017/6/30.
 */

public class Login {

    /**
     * status : 0
     */

    private int status;
    private String mirror_id;

    public String getMirror_id() {
        return mirror_id;
    }

    public void setMirror_id(String mirror_id) {
        this.mirror_id = mirror_id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


}
