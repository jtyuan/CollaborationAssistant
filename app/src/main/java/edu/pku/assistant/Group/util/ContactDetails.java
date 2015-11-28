package edu.pku.assistant.Group.util;

import java.io.Serializable;

// TO-DO
public class ContactDetails implements Serializable {

    private static final long serialVersionUID = 9139701857095411562L;

    public String mail;
    public String phone;
    public String renren;
    public String weibo;
    public String twitter;
    public String facebook;


    public ContactDetails() {
        this.mail = "null1";
        this.phone = "null2";
        this.renren = "null3";
        this.weibo = "null4";
        this.twitter = "null5";
        this.facebook = "null6";
    }


    public ContactDetails(String mail, String phone, String renren, String weibo, String twitter, String facebook) {
        this.mail = "null1";
        this.phone = "null2";
        this.renren = "null3";
        this.weibo = "null4";
        this.twitter = "null5";
        this.facebook = "null6";
    }

    public ContactDetails(String mail) {
        this();
        this.mail = mail;
    }

    public String getMail() {
        return this.mail;
    }

    public String getPhone() {
        return this.phone;
    }

    public String getRenren() {
        return this.renren;
    }

    public String getWeibo() {
        return this.weibo;
    }

    public String getTwitter() {
        return this.twitter;
    }

    public String getFacebook() {
        return this.facebook;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setRenren(String renren) {
        this.renren = renren;
    }

    public void setWeibo(String weibo) {
        this.weibo = weibo;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }
}
