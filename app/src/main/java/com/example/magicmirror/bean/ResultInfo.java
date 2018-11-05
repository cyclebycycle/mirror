package com.example.magicmirror.bean;

/**
 * Created by xyt on 2018-05-25.
 */

public class ResultInfo {
    private String time;
    private int gender;
    private int age;
    private int color_level;
    private int hue_level;
    private int oil_level;
    private int smooth_level;
    private int acne_level;
    private int pore_level;
    private int black_level;

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getAge() {
        return age;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getGender() {
        return gender;
    }

    public int getColor_level() {
        return color_level;
    }

    public void setColor_level(int color_level) {
        this.color_level = color_level;
    }

    public int getHue_level() {
        return hue_level;
    }

    public void setHue_level(int hue_level) {
        this.hue_level = hue_level;
    }

    public int getOil_level() {
        return oil_level;
    }

    public void setOil_level(int oil_level) {
        this.oil_level = oil_level;
    }

    public int getSmooth_level() {
        return smooth_level;
    }

    public void setSmooth_level(int smooth_level) {
        this.smooth_level = smooth_level;
    }

    public int getAcne_level() {
        return acne_level;
    }

    public void setAcne_level(int acne_level) {
        this.acne_level = acne_level;
    }

    public int getPore_level() {
        return pore_level;
    }

    public void setPore_level(int pore_level) {
        this.pore_level = pore_level;
    }

    public int getBlack_level() {
        return black_level;
    }

    public void setBlack_level(int black_level) {
        this.black_level = black_level;
    }

    //    public void show(){
//        System.out.print("time=" + time + ",");
//        System.out.print("age=" + age+",");
//        System.out.print("gender=" + gender+",");
//
//    }
}
