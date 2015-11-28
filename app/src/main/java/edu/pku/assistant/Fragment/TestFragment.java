package edu.pku.assistant.Fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.pku.assistant.HomeActivity;
import edu.pku.assistant.R;
import edu.pku.assistant.Tool.Constants;
import edu.pku.assistant.Tool.CustomerHttpClient;
import edu.pku.assistant.Tool.Database;
import edu.pku.assistant.UserInfoActivity;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;
import it.gmariotti.cardslib.library.view.CardView;


public class TestFragment extends Fragment {

    private static final String FRAGMENT_TITLE_CN = "搜索";
    private static final String FRAGMENT_TITLE_EN = "Search";

    private TextView TextView_Item_Number;
    private Card card;
    private CardView cardView;
    private CardListView ListView_Search;
    private Button Button_Send;

    private SharedPreferences mConfig;
    private int userId;
    private List<Card> SearchItemCards;
    private CardAdapter SearchAdapter;
    private Database database;


    public static TestFragment newInstance() {
        TestFragment fragment = new TestFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public TestFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_test, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mConfig = getActivity().getSharedPreferences("account", Context.MODE_PRIVATE);
        userId = mConfig.getInt("userid", -1);
        database = new Database(getActivity());

        card = new SearchBarCard(getActivity());
        cardView = (CardView) getActivity().findViewById(R.id.search_card);
        cardView.setCard(card);

        TextView_Item_Number = (TextView) getActivity().findViewById(R.id.search_item_number);

        SearchItemCards = new ArrayList<Card>();
        SearchAdapter = new CardAdapter(getActivity(), SearchItemCards);

        ListView_Search = (CardListView) getActivity().findViewById(R.id.search_list);
        ListView_Search.setAdapter(SearchAdapter);

        Button_Send = (Button) getActivity().findViewById(R.id.send_result);
        Button_Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int cnt = ListView_Search.getCount();
                Boolean feed_result[];
                feed_result = new Boolean[cnt];
                for (int i = 0; i < cnt; i++) {
                    feed_result[i] = ((SearchItemCard) ListView_Search.getItemAtPosition(i)).isChecked();
                }
                String result = "";
                for (int i = 0; i < cnt; i++)
                    if (feed_result[i])
                        result += "1";
                    else result += "0";
                Log.d("test", result);

                String keyword = ((EditText) cardView.findViewById(R.id.search_text)).getText().toString();
                Log.d("test", keyword);
                new SendTask(String.valueOf(userId), keyword, result).execute();

            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getResources().getConfiguration().locale.getCountry().equals("CN")) {
            ((HomeActivity) activity).onSectionAttached(FRAGMENT_TITLE_CN);
        } else {
            ((HomeActivity) activity).onSectionAttached(FRAGMENT_TITLE_EN);
        }
    }

    public class SearchBarCard extends Card {

        protected RelativeLayout SearchBar;
        protected EditText SearchText;
        protected Button SearchButton;

        public SearchBarCard(Context context) {
            super(context, R.layout.card_search);
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {
            SearchBar = (RelativeLayout) parent.findViewById(R.id.search_bar);
            SearchText = (EditText) SearchBar.findViewById(R.id.search_text);
            SearchButton = (Button) SearchBar.findViewById(R.id.search_button);

            SearchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String keyword = SearchText.getText().toString();
                    String userid = String.valueOf(userId);
                    new SearchTask(userid, keyword).execute();
                }
            });
        }
    }

    public class SearchItemCard extends Card {

        protected ImageView Thumbnail;
        protected CheckBox RightCheckbox;
        protected TextView NameTextView;
        protected TextView IDTextView;
        protected TextView StateNumTextView;
        protected TextView BlogNumTextView;

        private String Name;
        private String UserID;
        private Context context;
        private Boolean checked;

        private int CreateID;

        public SearchItemCard(Context context, String name, String userid) {
            super(context, R.layout.card_search_test);
            Name = name;
            UserID = userid;
            CreateID = getActivity().getSharedPreferences("account", Context.MODE_PRIVATE).getInt("userid", 0);
            this.context = context;
            checked = false;
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {
            Thumbnail = (ImageView) view.findViewById(R.id.thumbnail_image);
            RightCheckbox = (CheckBox) view.findViewById(R.id.right_checkbox);
            NameTextView = (TextView) view.findViewById(R.id.name);
            IDTextView = (TextView) view.findViewById(R.id.userid);
            StateNumTextView = (TextView) view.findViewById(R.id.state_number);
            BlogNumTextView = (TextView) view.findViewById(R.id.blog_number);

            NameTextView.setText(Name);
            IDTextView.setText(UserID);

            RightCheckbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checked = !checked;
                }
            });

            setOnClickListener(new OnCardClickListener() {
                @Override
                public void onClick(Card card, View view) {
                    Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                    intent.putExtra("name", Name);
                    intent.putExtra("userid", UserID);
                    intent.putExtra("keyword", getActivity().getSharedPreferences("search", Context.MODE_PRIVATE).getString("keyword", null));
                    getActivity().startActivity(intent);
                }
            });
        }

        public void setStateNum(Integer v) {
            StateNumTextView.setText(TestFragment.this.getActivity().getResources().getString(R.string.related_tweets) + String.valueOf(v)
                    + TestFragment.this.getActivity().getResources().getString(R.string.items));
        }

        public void setBlogNum(Integer v) {
            BlogNumTextView.setText(TestFragment.this.getActivity().getResources().getString(R.string.related_tweets) + String.valueOf(v)
                    + TestFragment.this.getActivity().getResources().getString(R.string.items));
        }

        public boolean isChecked() {
            return checked;
        }
    }

    public class CardAdapter extends CardArrayAdapter {
        public CardAdapter(Context context, List<Card> objects) {
            super(context, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = super.getView(position, convertView, parent);
            CheckBox checkbox = (CheckBox) convertView.findViewById(R.id.right_checkbox);
            if (((SearchItemCard) getItem(position)).isChecked()) checkbox.setChecked(true);
            else checkbox.setChecked(false);
            return convertView;
        }
    }

    public class SearchTask extends AsyncTask<Void, Void, Integer> {
        private String userId;
        private String keyWord;

        SearchTask(String userid, String keyword) {
            userId = userid;
            keyWord = keyword;
        }

        @Override
        protected Integer doInBackground(Void... param) {
            Integer retValue = -1;
            String respond = null;

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("userid", userId));
            params.add(new BasicNameValuePair("key", keyWord));
            try {
                respond = CustomerHttpClient.post(Constants.base_url + "contact/index.php/search/search", params);
            } catch (RuntimeException e) {
                respond = null;
            }
            if (respond == null) {
                retValue = -1;
            } else {
                try {
                    SearchItemCards.clear();
                    JSONObject jsonInfo = new JSONObject(respond);
                    int result = jsonInfo.getInt("result");
                    if (result == 0) retValue = 0;
                    else {
                        int count = jsonInfo.getInt("count");
                        JSONArray resultlist = jsonInfo.getJSONArray("users");
                        for (int i = 0; i < count; i++) {
                            SearchItemCard card = new SearchItemCard(getActivity(),
                                    resultlist.getJSONObject(i).getString("name"),
                                    resultlist.getJSONObject(i).getString("userid"));
                            SearchItemCards.add(card);
                        }
                        retValue = count;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return retValue;
        }

        @Override
        protected void onPostExecute(Integer success) {
            switch (success) {
                case -1:
                    Toast.makeText(getActivity(), R.string.network_error, Toast.LENGTH_SHORT).show();
                    break;
                case 0:
                    TextView_Item_Number.setText(TestFragment.this.getActivity().getResources().getString(R.string.no_related_user));
                    SearchAdapter.notifyDataSetChanged();
                    break;
                default:
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("search", Context.MODE_PRIVATE).edit();
                    editor.putString("keyword", keyWord);
                    editor.commit();
                    TextView_Item_Number.setText(TestFragment.this.getActivity().getResources().getString(R.string.found)
                            + String.valueOf(success)
                            + TestFragment.this.getActivity().getResources().getString(R.string.related_users));
                    SearchAdapter.notifyDataSetChanged();
                    break;
            }
        }
    }

    public class InsertGroupTask extends AsyncTask<Void, Void, Integer> {
        private Context context;
        private String GroupName;
        private int Host;
        private String HostName;
        private long Createdate;
        private int GroupID;
        private String UserName;
        private String UserID;
        private SimpleDateFormat sdf;
        private String msg;

        InsertGroupTask(Context context, String groupname, int host, String name, String userid) {
            this.context = context;
            GroupName = groupname;
            Host = host;
            sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            UserName = name;
            UserID = userid;
        }

        @Override
        protected Integer doInBackground(Void... param) {
            Integer retValue = -1;
            String respond = null;

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("name", GroupName));
            params.add(new BasicNameValuePair("num", "1"));
            params.add(new BasicNameValuePair("createuser", String.valueOf(Host)));
            params.add(new BasicNameValuePair("memberids", UserID));
            try {
                respond = CustomerHttpClient.post(Constants.base_url + "contact/index.php/group/addgroup", params);
            } catch (RuntimeException e) {
                respond = null;
            }
            if (respond == null) {
                retValue = -1;
            } else {
                try {
                    JSONObject jsonInfo = new JSONObject(respond);
                    int result = jsonInfo.getInt("result");
                    if (result == 1) {
                        GroupID = jsonInfo.getInt("groupid");
                        HostName = jsonInfo.getString("username");
                        Createdate = Long.valueOf(jsonInfo.getString("createtime"));

                        database.Open();
                        database.InsertGroup(GroupID, GroupName, HostName, sdf.format(new Date(Createdate * 1000L)));
                        database.InsertContacts(GroupID, UserName, Integer.parseInt(UserID), null, -1, null);
                    } else {
                        msg = jsonInfo.getString("msg");
                        retValue = 0;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return retValue;
        }

        @Override
        protected void onPostExecute(Integer retValue) {
            switch (retValue) {
                case 0:
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Toast.makeText(context, TestFragment.this.getActivity().getResources().getString(R.string.add_success), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }

    public class InsertContactToGroupTask extends AsyncTask<Void, Void, Integer> {
        private Context context;
        private String UserName;
        private String UserID;
        private int GroupID;

        InsertContactToGroupTask(Context context, String username, String userid, int groupid) {
            this.context = context;
            UserName = username;
            UserID = userid;
            GroupID = groupid;
        }

        @Override
        protected Integer doInBackground(Void... param) {
            Integer retValue = -1;
            String respond = null;

            List<NameValuePair> params;
            params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("groupid", String.valueOf(GroupID)));
            params.add(new BasicNameValuePair("userid", UserID));
            Log.d("!!!", UserName + String.valueOf(GroupID));
            try {
                respond = CustomerHttpClient.post(Constants.base_url + "contact/index.php/group/adduser", params);
            } catch (RuntimeException e) {
                respond = null;
            }
            if (respond == null) {
                retValue = -1;
            } else {
                try {
                    JSONObject jsonInfo = new JSONObject(respond);
                    retValue = jsonInfo.getInt("result");
                    if (retValue == 1) {
                        database.Open();
                        database.InsertContacts(GroupID, UserName, Integer.parseInt(UserID), null, -1, null);
                        Log.d("!!!", UserName + String.valueOf(GroupID));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return retValue;
        }

        @Override
        protected void onPostExecute(Integer retValue) {
            if (retValue == 0)
                Toast.makeText(context, TestFragment.this.getActivity().getResources().getString(R.string.add_failure), Toast.LENGTH_SHORT).show();
            else if (retValue == 1)
                Toast.makeText(context, TestFragment.this.getActivity().getResources().getString(R.string.add_success), Toast.LENGTH_SHORT).show();
        }
    }

    public class SendTask extends AsyncTask<Void, Void, Integer> {
        private String Data;
        private String userId;
        private String KeyWord;

        SendTask(String userid, String keyword, String result) {
            userId = userid;
            KeyWord = keyword;
            Data = result;
        }

        @Override
        protected Integer doInBackground(Void... param) {
            Integer retValue = -1;
            String respond = null;

            List<NameValuePair> params;
            params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("userid", userId));
            params.add(new BasicNameValuePair("key", KeyWord));
            params.add(new BasicNameValuePair("result", String.valueOf(Data)));
            try {
                respond = CustomerHttpClient.post(Constants.base_url + "contact/index.php/search/searchfeedback", params);
                Log.d("test", respond);
            } catch (RuntimeException e) {
                respond = null;
            }
            if (respond == null) {
                retValue = 0;
            } else {
                retValue = 1;
            }
            return retValue;
        }

        @Override
        protected void onPostExecute(Integer retValue) {
            if (retValue == 1)
                Toast.makeText(getActivity(), "上传成功", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getActivity(), "上传失败", Toast.LENGTH_SHORT).show();
        }
    }
}