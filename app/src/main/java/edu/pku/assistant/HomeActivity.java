package edu.pku.assistant;

import android.app.Activity;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.pku.assistant.Fragment.ContactFragment;
import edu.pku.assistant.Fragment.GroupFragment;
import edu.pku.assistant.Fragment.NavigationDrawerFragment;
import edu.pku.assistant.Fragment.NoticeFragment;
import edu.pku.assistant.Fragment.SearchFragment;
import edu.pku.assistant.Fragment.TestFragment;
import edu.pku.assistant.Tool.Constants;
import edu.pku.assistant.Tool.CustomerHttpClient;
import edu.pku.assistant.Service.UpStatus;

public class HomeActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
    public static int userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        userid = getSharedPreferences("account", MODE_PRIVATE).getInt("userid", 0);

        Intent intent = new Intent(HomeActivity.this, UpStatus.class);
        startService(intent);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        Intent intent;
        switch (position)
        {
            case 0:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, GroupFragment.newInstance())
                        .commit();
                break;
            case 1:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, ContactFragment.newInstance())
                        .commit();
                break;
            case 2:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, SearchFragment.newInstance())
                        .commit();
                break;
            case 3:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, NoticeFragment.newInstance())
                        .commit();
                break;
            case 4:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, TestFragment.newInstance())
                        .commit();
                break;
            case 5:
                intent = new Intent(HomeActivity.this, BindActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                break;
            case 6:
                intent = new Intent(HomeActivity.this, IntroActivity.class);
                intent.putExtra("finishflag", true);
                startActivity(intent);
                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                break;
            case 7:
                new LogoutTask(userid).execute();
                break;
            default:
                break;
        }

    }

    public void onSectionAttached(String title) {
        mTitle = title;
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.home, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            mNavigationDrawerFragment.refresh();
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    public class LogoutTask extends AsyncTask<Void, Void, Boolean> {

        private int UserId;

        LogoutTask(int userid) {
            UserId = userid;
        }

        @Override
        protected Boolean doInBackground(Void... param) {
            Boolean retValue = false;
            String respond;

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("userid", String.valueOf(UserId)));
            try {
                respond = CustomerHttpClient.post(Constants.base_url + "contact/index.php/contact/loginout", params);
            } catch (RuntimeException e) {
                respond = null;
            }
            if (respond == null) {
                retValue = false;
            } else {
                try {
                    JSONObject jsonInfo = new JSONObject(respond);
                    int result = jsonInfo.getInt("result");
                    switch (result) {
                        case 0:
                            retValue = false;
                            break;
                        case 1:
                            retValue = true;
                            break;
                        default:
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return retValue;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            SharedPreferences.Editor editor = getSharedPreferences("account", MODE_PRIVATE).edit();
            editor.putInt("userid", -1);
            editor.putString("phone", null);
            editor.putString("password", null);
            editor.apply();
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
        }
    }
}
