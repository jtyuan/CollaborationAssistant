package edu.pku.assistant;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.pku.assistant.Tool.Constants;
import edu.pku.assistant.Tool.CustomerHttpClient;

public class StartActivity extends Activity {
    private static final int SHOW_TIME_MIN = 800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        SharedPreferences mConfig = getSharedPreferences("account", Context.MODE_PRIVATE);
        String phone = mConfig.getString("phone", null);
        String password = mConfig.getString("password", null);

        new LoginTask(phone, password).execute();
    }

    public class LoginTask extends AsyncTask<Void, Void, Boolean> {
        private String mPhone;
        private String mPassword;

        LoginTask(String phone, String password) {
            mPhone = phone;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... param) {
            Boolean retValue = false;
            String respond;
            long startTime;
            long usedTime;
            startTime = System.currentTimeMillis();
            if (mPhone == null || mPassword == null)
                retValue = false;
            else {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("phone", mPhone));
                params.add(new BasicNameValuePair("password", mPassword));
                try {
                    respond = CustomerHttpClient.post(Constants.base_url + "contact/index.php/contact/login", params);
                } catch (RuntimeException e) {
                    respond = null;
                }
                if (respond == null) {
                    Toast.makeText(StartActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                    retValue = false;
                } else {
                    try {
                        JSONObject jsonInfo = new JSONObject(respond);
                        int result = jsonInfo.getInt("result");
                        switch (result) {
                            case 0:
                                Toast.makeText(StartActivity.this, R.string.login_fail, Toast.LENGTH_SHORT).show();
                                retValue = false;
                                break;
                            case 1:
                                int userId = jsonInfo.getInt("userid");
                                String userName = jsonInfo.getString("username");
                                SharedPreferences.Editor mConfig = getSharedPreferences("account", Context.MODE_PRIVATE).edit();
                                mConfig.putString("phone", mPhone);
                                mConfig.putString("password", mPassword);
                                mConfig.putInt("userid", userId);
                                mConfig.putString("username", userName);
                                mConfig.commit();
                                retValue = true;
                                break;
                            default:
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            usedTime = System.currentTimeMillis() - startTime;
            if (usedTime < SHOW_TIME_MIN) {
                try {
                    Thread.sleep(SHOW_TIME_MIN - usedTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return retValue;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            Intent intent;
            if (success) intent = new Intent(StartActivity.this, HomeActivity.class);
            else intent = new Intent(StartActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }
}