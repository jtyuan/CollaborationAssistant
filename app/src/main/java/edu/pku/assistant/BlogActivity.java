package edu.pku.assistant;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.pku.assistant.Tool.Constants;
import edu.pku.assistant.Tool.CustomerHttpClient;


public class BlogActivity extends Activity {

    private TextView titleTextView;
    private TextView contentTextView;

    private String title;
    private String indexid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog);

        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        indexid = intent.getStringExtra("indexid");

        titleTextView = (TextView)findViewById(R.id.title);
        contentTextView = (TextView)findViewById(R.id.content);
        titleTextView.setText(title);
        new BlogTask(indexid).execute();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.blog, menu);
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

    public class BlogTask extends AsyncTask<Void, Void, Integer> {
        private String indexId;
        private String title;
        private String content;

        BlogTask(String indexid) {
            indexId = indexid;
        }

        @Override
        protected Integer doInBackground(Void... param) {
            Integer retValue = -1;
            String respond = null;

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("indexid", indexId));
            try {
                respond = CustomerHttpClient.post(Constants.base_url + "contact/index.php/search/searchIndex", params);
            } catch (RuntimeException e) {
                respond = null;
            }
            if (respond == null) {
                retValue = -1;
            } else {
                try {
                    JSONObject jsonInfo = new JSONObject(respond);
                    int result = jsonInfo.getInt("result");
                    if (result == 0) retValue = 0;
                    else {
                        title = jsonInfo.getString("title");
                        content = jsonInfo.getString("content");
                        retValue = 1;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return retValue;
        }

        @Override
        protected void onPostExecute(Integer success) {
            switch(success) {
                case -1:
                    Toast.makeText(BlogActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                    break;
                case 0:
                    break;
                default:
                    contentTextView.setText(content);
                    break;
            }
        }
    }
}
