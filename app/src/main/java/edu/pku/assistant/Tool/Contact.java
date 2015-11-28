package edu.pku.assistant.Tool;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import org.apache.commons.lang3.builder.EqualsBuilder;

import java.io.Serializable;

public class Contact implements Comparable<Contact>, Serializable {

    private static final long serialVersionUID = 9089422736760463212L;
    //人人姓名
    private String name;

    //人人id
    private int id;

    //app姓名
    private String username;

    //app id
    private int userid;

    //联系人电话
    private String phone;

    //联系人头像
    private Bitmap thumbnail;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return username;
    }

    public void setUserName(String username) {
        this.username = username;
    }

    public int getUserId() {
        return userid;
    }

    public void setUserId(int userid) {
        this.userid = userid;
    }

    public String getPhone() { return phone; }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Bitmap getThumbnail () { return thumbnail; }

    public void setThumbnail(Bitmap thumbnail) { this.thumbnail = thumbnail; }

    public Contact(int id, String name, int userid, String username, String phone) {
        this.id = id;
        this.name = name;
        this.userid = userid;
        this.username = username;
        this.phone = phone;
    }

    public Contact(String name, String phone) {
        this.id = -1;
        this.name = name;
        this.userid = -1;
        this.username = "";
        this.phone = phone;
    }
    public Contact(String name) {
        this.name = name;
        this.id = -1;
    }

    public Contact(int id, String name) {
        this.id = id;
        this.name = name;
    }


    @Override
    public int compareTo(@NonNull Contact member) {
        if (this.name.equals(member.name)) {
            long flag = this.id - member.id;
            if (flag > 0)
                return 1;
            else if (flag < 0)
                return -1;
            else
                return 0;
        } else {
            return this.name.compareTo(member.name);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Contact))
            return false;
        if (obj == this)
            return true;

        Contact rhs = (Contact) obj;
        return new EqualsBuilder()
                .append(this.name, rhs.name)
                .append(this.id, rhs.id)
                .isEquals();
    }

    public boolean isAvailable() {
        return Math.random()>0.5;
    }
}