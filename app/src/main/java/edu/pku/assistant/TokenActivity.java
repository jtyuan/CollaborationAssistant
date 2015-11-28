package edu.pku.assistant;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import edu.pku.assistant.Tool.Constants;
import edu.pku.assistant.Tool.CustomerHttpClient;


public class TokenActivity extends Activity {

    WebView mWebView;
    String code;
    int userid;
    String url;
    String extend_url;
    String type;

    static final private String url_www = "sec.webkdd.org";

    @SuppressLint({ "SetJavaScriptEnabled", "JavascriptInterface" })

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token);
        final EditText editText = new EditText(this);
        AlertDialog.Builder ad = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.alert_google_mail))
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(editText).setPositiveButton(getResources().getString(R.string.button_confirm), null);
        ad.show();
        type = getIntent().getStringExtra("type");
        if (type.equals("微信"))
        {
            url = "";
            extend_url = "";
        }
        else if (type.equals("微博"))
        {
            url = "https://api.weibo.com/oauth2/authorize?client_id=2298980512" +
                    "&response_type=code" +
                    "&redirect_uri=http%3A%2F%2F" + url_www + "%2FContactAssistant%2Fcontact%2Findex.php%2Fcontact%2Fgoogleredirect";
            extend_url = "contact/index.php/weibo/postcode";
        }else if (type.equals("人人"))
        {
            url = "https://graph.renren.com/oauth/grant?client_id=896a7f449b9b4f439ad2a6a5ddebf49b"
                    + "&redirect_uri=http%3A%2F%2F" + url_www + "%2FContactAssistant%2Fcontact%2Findex.php%2Fcontact%2Fgoogleredirect"
                    + "&response_type=code"
                    + "&scope=publish_feed photo_upload read_user_blog read_user_share read_user_status read_user_comment read_user_photo read_user_album"
                    + "&display=touch";
            extend_url = "contact/index.php/renren/postcode";
        }else if (type.equals("Google"))
        {
            url = "https://accounts.google.com/o/oauth2/auth?response_type=code" +
                    "&redirect_uri=http%3A%2F%2F" + url_www + "%2FContactAssistant%2Fcontact%2Findex.php%2Fcontact%2Fgoogleredirect" +
                    "&client_id=622999074075-dk2kkdn3o2nsct295nu8egiv1vbgt1r6.apps.googleusercontent.com" +
                    "&scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fcalendar" +
                    "&access_type=offline&approval_prompt=force";
            extend_url = "contact/index.php/contact/postcode";
        }

        mWebView = (WebView) findViewById(R.id.browser);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if(url.startsWith("http://sec.webkdd.org/ContactAssistant/"/*Constants.base_url*/)){
                    int start = url.indexOf("code");
                    code = url.substring(start+5);
                    if (code.contains("googleredirect")) {
                        TokenActivity.this.setResult(RESULT_OK);
                        TokenActivity.this.finish();
                    }
                    SharedPreferences mConfig = getSharedPreferences("bind", MODE_PRIVATE);
                    mConfig.edit().putString(type + "code", code).commit();
                    mConfig.edit().putInt(type + "status", 1).commit();
                    Thread newThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            List<NameValuePair> params = new ArrayList<NameValuePair>();
                            SharedPreferences settings = getSharedPreferences("account",0);
                            userid = settings.getInt("userid",0);
                            params.add(new BasicNameValuePair("userid", String.valueOf(userid)));
                            params.add(new BasicNameValuePair("code", code));
                            params.add(new BasicNameValuePair("gmail", editText.getText().toString().trim()));
//                            String result = CustomerHttpClient.post(Constants.base_url + extend_url, params);
                            String result = CustomerHttpClient.post("http://sec.webkdd.org/ContactAssistant/" + extend_url, params);
                            Toast.makeText(TokenActivity.this, result, Toast.LENGTH_SHORT).show();
                            TokenActivity.this.setResult(RESULT_OK);
                            TokenActivity.this.finish();
                        }
                    });
                    newThread.start();
                }
                super.onPageStarted(view, url, favicon);
            }
        });
        mWebView.loadUrl(url);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.token, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
