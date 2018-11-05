package com.example.magicmirror.bean;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/11/2.
 */

public class PhotoResult {
    private int status;//状态
    private ArrayList<Photo> data;//获取用户的照片信息

    public PhotoResult(int status, ArrayList<Photo> data) {
        this.status = status;
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public ArrayList<Photo> getData() {
        return data;
    }

    public void setData(ArrayList<Photo> data) {
        this.data = data;
    }

}
