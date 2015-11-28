package edu.pku.assistant.Service;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import edu.pku.assistant.Tool.Constants;
import edu.pku.assistant.Tool.CustomerHttpClient;

public class UpStatus extends IntentService implements SensorEventListener
{
    private static int sleeptime = 20000;
    private SensorManager mSensorManager;
    private String light;
    public UserStatus userStatus;
    private LocationManager locManager;
    private Context context;
    private AudioManager audioManager;
    private TelephonyManager telephoneManager;
    private ConnectivityManager connectionManager;
    private NetworkInfo networkInfo;
    private PowerManager powerManager;
    private AccountManager accountManager;
    private Account[] accounts;
    private float ax, ay, az;
    private Handler handler;
    public UpStatus(){
        super("UpStatus");
    }
    //初始化一下信息
    public void onCreate()
    {
        super.onCreate();
        context = this.getApplicationContext();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        telephoneManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_UI);
        connectionManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        accountManager = AccountManager.get(this);
        accounts = accountManager.getAccountsByType("com.google");
        ax = ay = az = 0;
        userStatus = new UserStatus();
        userStatus.set_userid(getSharedPreferences("account", MODE_PRIVATE).getInt("userid", 0));
        getSignal();

        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        final BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                String action = intent.getAction();
                if(Intent.ACTION_SCREEN_OFF.equals(action))
                    userStatus.set_screenofftime(SystemClock.elapsedRealtime());
            }
        };

        registerReceiver(mBatInfoReceiver, filter);
    }

    public void onDestroy()
    {
        mSensorManager.unregisterListener(this);
        mSensorManager.unregisterListener(listener);
        super.onDestroy();
    }
    @SuppressLint("HandlerLeak")
    //在这里执行你要在intentService中执行的服务，这里我们主要是获取用户的信息并上传
    protected void onHandleIntent(Intent intent)
    {
        while(true)
        {
            try
            {
                mSensorManager.unregisterListener(this);
                mSensorManager.unregisterListener(listener);
                Thread.sleep(sleeptime);
                mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_UI);
                userStatus.set_move(0);
                mSensorManager.registerListener(listener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
                Thread.sleep(sleeptime);
                userStatus.set_light(light);

                //userStatus.set_volume(String.valueOf(rt.getValue()));
                locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Location location = null;
                String enableProvider = null;
                if(locManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                {
                    enableProvider = LocationManager.GPS_PROVIDER;
                    location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                else if (locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                {
                    enableProvider = LocationManager.NETWORK_PROVIDER;
                    location = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
                else
                {
                    System.out.println("NONE");
                }
                if(location != null)
                {
                    userStatus.set_latitude(location.getLatitude());
                    userStatus.set_longitude(location.getLongitude());
                    if (location.hasSpeed())
                    {
                        userStatus.set_volocity(String.valueOf(location.getSpeed()));
                        Log.v("liuyitest", "have speed");
                    }
                    locManager.requestLocationUpdates(enableProvider, 0, 0, new LocationListener()
                    {
                        @Override
                        public void onLocationChanged(Location location)
                        {
                            if(location != null)
                            {
                                userStatus.set_latitude(location.getLatitude());
                                userStatus.set_longitude(location.getLongitude());
                                if (location.hasSpeed())
                                {
                                    userStatus.set_volocity(String.valueOf(location.getSpeed()));
                                    Log.v("liuyitest", "have speed");
                                }
                            }
                        }
                        @Override
                        public void onProviderDisabled(String provider)
                        {
                        }
                        @Override
                        public void onProviderEnabled(String provider)
                        {
                            Location location = locManager.getLastKnownLocation(provider);

                            userStatus.set_latitude(location.getLatitude());
                            userStatus.set_longitude(location.getLongitude());
                            if (location.hasSpeed())
                            {
                                userStatus.set_volocity(String.valueOf(location.getSpeed()));
                            }

                        }
                        @Override
                        public void onStatusChanged(String provider,
                                                    int status, Bundle extras) {
                            // TODO Auto-generated method stub

                        }
                    });
                }
                userStatus.set_weixinon(getWX());
                userStatus.set_qqon(getQQ());
                userStatus.set_phonemodel(""+getModel());
                userStatus.set_wifi(getNetworkType());
                userStatus.set_nextalarm(Settings.System.getString(getContentResolver(),  Settings.System.NEXT_ALARM_FORMATTED));
                userStatus.set_status(""+getStatus());
                userStatus.set_preferredway(""+getPreferredWay());
                userStatus.set_misscall(getMisscall(getApplicationContext()));
                userStatus.set_lastcall(getLastcall(getApplicationContext()));
                userStatus.set_ismusicactive(getIsMusicActive(getApplicationContext()));
                if (powerManager.isScreenOn())
                {
                    userStatus.set_timenotused(0);
                }
                else
                {
                    userStatus.set_timenotused(SystemClock.elapsedRealtime()-userStatus.screenofftime());
                }

                //SocketHelper.send(userStatus.statusInfo());
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("userid",""+userStatus.getuserid()));
                params.add(new BasicNameValuePair("phonestatus",""+userStatus.getphonemodel()));
                params.add(new BasicNameValuePair("volocity",""+userStatus.getvolocity()));
                params.add(new BasicNameValuePair("volume",""+userStatus.getvolume()));
                params.add(new BasicNameValuePair("light",""+userStatus.getlight()));
                params.add(new BasicNameValuePair("weixinon",""+userStatus.getweixinon()));
                params.add(new BasicNameValuePair("qqon",""+userStatus.getqqon()));
                params.add(new BasicNameValuePair("longitude",""+userStatus.getlongitude()));
                params.add(new BasicNameValuePair("light",""+userStatus.getlight()));
                params.add(new BasicNameValuePair("latitude",""+userStatus.getlatitude()));
                params.add(new BasicNameValuePair("wifi",""+userStatus.getwifi()));
                params.add(new BasicNameValuePair("signal",userStatus.signal()));
                params.add(new BasicNameValuePair("timenotused",""+userStatus.timenotused()));
                params.add(new BasicNameValuePair("status",""+userStatus.status()));
                params.add(new BasicNameValuePair("alarmtime",""+userStatus.nextalarm()));
                params.add(new BasicNameValuePair("preferway",""+userStatus.preferredway()));
                params.add(new BasicNameValuePair("misscall", ""+userStatus.misscall()));
                params.add(new BasicNameValuePair("lastcall", ""+userStatus.lastcall()));
                params.add(new BasicNameValuePair("musicactive", ""+userStatus.ismusicactive()));
                params.add(new BasicNameValuePair("move", ""+userStatus.move()));
                userStatus.printUserStatus();

                String result = CustomerHttpClient.post(Constants.base_url + "contact/index.php/contact/sendsensordata", params);

                handler=new Handler(Looper.getMainLooper());
                handler.post(new Runnable(){
                    public void run(){
                        Toast.makeText(getApplicationContext(), userStatus.getvolocity(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    //查看网络状态
    public boolean checkNetwork()
    {
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info=cm.getActiveNetworkInfo();
        if(info != null && info.isConnected())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public void onSensorChanged(SensorEvent event)
    {
        // TODO Auto-generated method stub
        float[] values = event.values;
        int sensorType = event.sensor.getType();
        switch(sensorType)
        {
            //这里可以添加其他sensor的监听，如果需要：比如重力传感器、温度传感器等信息
            case Sensor.TYPE_LIGHT:
                light = Integer.toString((int) values[0]);
                break;
            default:
                break;
        }
    }
    /**
     * 返回微信的状态，是否正在运行，是：1，否：0（这里根据需要可以修改运行的概念为是否使用)
     */
    private String getWX() {
        int result = 0;
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> list = am.getRunningTasks(100);
        for (RunningTaskInfo info : list) {
            if (info.topActivity.getPackageName().equals("com.tencent.mm")) {
                if (info == list.get(0)) result = 1;
                else result = 2;
                break;
            }
        }
        return ""+result;
    }

    /**
     * 返回qq的状态，是否正在运行，是：1，否：0
     */
    private String getQQ() {
        int result = 0;
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> list = am.getRunningTasks(100);
        for (RunningTaskInfo info : list) {
            if (info.topActivity.getPackageName().equals("com.tencent.mobileqq")) {
                if (info == list.get(0)) result = 1;
                else result = 2;
                break;
            }
        }
        return ""+result;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }
    /**
     * 获取用户的情景模式信息
     */
    public int getModel(){
        int model = 0;

        switch(audioManager.getRingerMode()){
            case AudioManager.RINGER_MODE_NORMAL:
                model = 0;
                break;
            case AudioManager.RINGER_MODE_SILENT:
                model = 1;
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                model = 2;
                break;
            default:
                break;
        }
        //获取初始音量，暂时用不到
        int volume=audioManager.getStreamVolume(AudioManager.STREAM_RING);
        return model;
    }
    //获取联网类型
    public int getNetworkType(){
        networkInfo = connectionManager.getActiveNetworkInfo();
        int type = networkInfo.getType();
        String typeName = networkInfo.getTypeName();
        Log.v("liuyitest",typeName);
        return type;
    }
    //获取手机的信号
    public int getSignal(){
        MyPhoneStateListener MyListener = new MyPhoneStateListener();
        telephoneManager.listen(MyListener ,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        return 1;
    }

    private class MyPhoneStateListener extends PhoneStateListener

    {
        /* 从得到的信号强度,每个tiome供应商有更新*/
        public void onSignalStrengthsChanged(SignalStrength signalStrength)
        {
            super.onSignalStrengthsChanged(signalStrength);
            Log.v("liuyitest","GSM Cinr = "+ String.valueOf(signalStrength.getGsmSignalStrength()));
            userStatus.set_signal(String.valueOf(signalStrength.getGsmSignalStrength()));
        }
    }

    private String getStatus()
    {
        SharedPreferences settings = getSharedPreferences("PREFERRED_STATUS", MODE_PRIVATE);
        return(String.valueOf(settings.getInt("status", 1)));
    }

    private String getPreferredWay()
    {
        String preferredway = "";
        SharedPreferences settings = getSharedPreferences("PREFERRED_INFO", MODE_PRIVATE);
        if (settings.getInt("微信", 0) == 1) preferredway += "1,";
        if (settings.getInt("新浪微博", 0) == 1) preferredway += "2,";
        if (settings.getInt("QQ", 0) == 1) preferredway += "3,";
        if (settings.getInt("人人", 0) == 1) preferredway += "4,";
        if (settings.getInt("电话", 0) == 1) preferredway += "5,";
        if (settings.getInt("短信", 0) == 1) preferredway += "6,";
        if (preferredway != "") preferredway = preferredway.substring(0,preferredway.length() - 1);
        return preferredway;
    }

    private int getMisscall(Context context) {
        int result = 0;
        Cursor cur = null;
        try {
            cur = context.getContentResolver().query(Calls.CONTENT_URI, null,
                    "type = 3 and new = 1", null, null);
            if (null != cur) {
                result = cur.getCount();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.v("oo", e.getMessage());
        } finally {
            if (cur != null) {
                cur.close();
            }
        }
        return result;
    }

    private String getLastcall(Context context) {
        String time = "";
        Cursor cur = null;
        try {
            cur = context.getContentResolver().query(Calls.CONTENT_URI, new String[] {
                            CallLog.Calls.DATE },
                    "type = 1", null, "date desc limit 1");
            if (null != cur) {
                if (cur.moveToFirst())
                    time = cur.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.v("oo", e.getMessage());
        } finally {
            if (cur != null) {
                cur.close();
            }
        }
        return time.substring(0, 10);
    }

    private int getIsMusicActive(Context context){
        AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if (am != null)
            if(am.isMusicActive()) return 1;
        return 0;
    }

    private final SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER) {
                float x=event.values[0];
                float y=event.values[1];
                float z=event.values[2];
                //Log.v("oo", String.valueOf(x)+String.valueOf(y)+String.valueOf(z));
                if (ax==0 && ay==0 && az==0)
                {
                    ax = x;
                    ay = y;
                    az = z;
                }
                else
                {
                    float difx = Math.abs(x-ax);
                    float dify = Math.abs(y-ay);
                    float difz = Math.abs(z-az);
                    if (difx>1 || dify>1 || difz>1)
                        userStatus.set_move(1);
                }
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };
}
