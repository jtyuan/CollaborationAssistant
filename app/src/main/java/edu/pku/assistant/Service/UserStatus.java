package edu.pku.assistant.Service;

import android.util.Log;

public class UserStatus {
    private int userid;

    private String phonemodel;
    private String volocity;
    private String volume;
    private String light;
    private String weixinon;
    private String qqon;
    private double longitude;
    private double latitude;
    private int wifi;
    private String signal;
    private long screenofftime;
    private long timenotused;
    private String status;
    private String nextalarm;
    private String preferredway;
    private int misscall;
    private String lastcall;
    private int ismusicactive;
    private int move;

    UserStatus()
    {
        phonemodel = "0";
        volocity = "100";
        volume = "100";
        light = "25";
        weixinon = "1";
        longitude = 0;
        light="20";
        latitude = 0;
        qqon="1";
        signal = "1";
        wifi = 0;
        timenotused = 0;
        status = "1";
        nextalarm = "";
        preferredway = "";
        misscall = 0;
        lastcall = "0";
        ismusicactive = 0;
        move = 0;
    }

    public void printUserStatus(){
        Log.v("liuyitest","userid: "+userid+" ,phonemodel: "+phonemodel+", volicity: "+volocity+" ,volume: "+volume +" ,light: "+light+" ,weixinon: "+weixinon+
                " ,longitude: "+longitude +" ,light: "+light+" ,latitude: "+latitude+" ,qqon: "+qqon+" ,signal: "+signal+" ,wifi:"+wifi+" ,timenotused: "+timenotused
                +" ,state: "+status+" ,nextalarm: "+nextalarm+" ,preferredway: "+preferredway+" ,misscall: "+misscall+" ,lastcall: "+lastcall+
                " ,ismusicactive: "+ismusicactive+" ,move: "+move);
    }


    public int getuserid(){
        return userid;
    }
    public String getphonemodel(){
        return phonemodel;
    }
    public String getvolocity(){
        return volocity;
    }
    public String getvolume(){
        return volume;
    }
    public String getlight(){
        return light;
    }
    public String getweixinon(){
        return weixinon;
    }
    public String getqqon(){
        return qqon;
    }
    public double getlongitude(){
        return longitude;
    }
    public double getlatitude(){
        return latitude;
    }
    public int getwifi(){
        return wifi;
    }
    public String signal(){
        return signal;
    }
    public long screenofftime(){
        return screenofftime;
    }
    public long timenotused(){
        return timenotused;
    }
    public String status(){
        return status;
    }
    public String nextalarm(){
        return nextalarm;
    }
    public String preferredway(){
        return preferredway;
    }
    public String lastcall() {
        return lastcall;
    }

    public int misscall(){
        return misscall;
    }
    public int ismusicactive(){
        return ismusicactive;
    }
    public int move(){
        return move;
    }

    public String statusInfo()
    {
        return null;
    }
    public void set_userid(int a)
    {
        userid = a;
    }
    public void set_phonemodel(String a)
    {
        phonemodel = a;
    }

    public void set_volocity(String a)
    {
        volocity = a;
    }
    public void set_volume(String a)
    {
        volume = a;
    }
    public void set_light(String a)
    {
        light = a;
    }
    public void set_weixinon(String a)
    {
        weixinon = a;
    }
    public void set_longitude(double a){
        longitude = a;
    }
    public void set_latitude(double a){
        latitude = a;
    }
    public void set_qqon(String a){
        qqon = a;
    }
    public void set_wifi(int a){
        wifi = a;
    }
    public void set_signal(String a){
        signal = a;
    }
    public void set_screenofftime(long a){
        screenofftime = a;
    }
    public void set_timenotused(long a){
        timenotused = a;
    }
    public void set_status(String a){
        status = a;
    }
    public void set_nextalarm(String a){
        nextalarm = a;
    }
    public void set_preferredway(String a){
        preferredway = a;
    }
    public void set_misscall(int a){
        misscall = a;
    }
    public void set_lastcall(String a){
        lastcall = a;
    }
    public void set_ismusicactive(int a){
        ismusicactive = a;
    }
    public void set_move(int a){
        move = a;
    }
}
