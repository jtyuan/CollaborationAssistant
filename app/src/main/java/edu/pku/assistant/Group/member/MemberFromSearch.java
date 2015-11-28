package edu.pku.assistant.Group.member;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import java.util.Calendar;
import java.util.List;

import edu.pku.assistant.R;
import edu.pku.assistant.Tool.Constants;
import edu.pku.assistant.Tool.Contact;
import edu.pku.assistant.Tool.CustomerHttpClient;
import edu.pku.assistant.Tool.Database;
import edu.pku.assistant.UserInfoActivity;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;
import it.gmariotti.cardslib.library.view.CardView;

@SuppressWarnings("unchecked")
public class MemberFromSearch extends FragmentActivity {
    private TextView TextView_Item_Number;
    private Card card;
    private CardView cardView;
    private CardListView ListView_Search;

    private SharedPreferences mConfig;
    private int userId;
    private ArrayList<Card> SearchItemCards;
    private CardArrayAdapter SearchAdapter;
    private Database database;

    private int groupId;

    private ArrayList<Contact> newMembers;
    private ArrayList<Contact> members;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_search);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            groupId = bundle.getInt("groupid");
            members = (ArrayList<Contact>) bundle.getSerializable("members");
        }

        newMembers = new ArrayList<Contact>();

        mConfig = this.getSharedPreferences("account", Context.MODE_PRIVATE);
        userId = mConfig.getInt("userid", -1);
        database = new Database(this);

        card = new SearchBarCard(this);
        cardView = (CardView) this.findViewById(R.id.search_card);
        cardView.setCard(card);

        TextView_Item_Number = (TextView) this.findViewById(R.id.search_item_number);

        SearchItemCards = new ArrayList<Card>();
        SearchAdapter = new CardArrayAdapter(this,SearchItemCards);

        ListView_Search = (CardListView) this.findViewById(R.id.search_list);
        ListView_Search.setAdapter(SearchAdapter);
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
            SearchBar = (RelativeLayout)parent.findViewById(R.id.search_bar);
            SearchText = (EditText)SearchBar.findViewById(R.id.search_text);
            SearchButton = (Button)SearchBar.findViewById(R.id.search_button);

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
        protected Button AddButton;
        protected TextView NameTextView;
        protected TextView IDTextView;
        protected TextView StateNumTextView;
        protected TextView BlogNumTextView;

        private String Name;
        private String UserID;
        private Context context;

        private String UserName;
        private Calendar calendar;
        private SimpleDateFormat sdf;

        public SearchItemCard(Context context, String name, String userid) {
            super(context, R.layout.card_search_item);
            Name = name;
            UserID = userid;
            UserName = getSharedPreferences("account", Context.MODE_PRIVATE).getString("username", null);
            calendar = Calendar.getInstance();
            sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            this.context = context;
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {
            Thumbnail = (ImageView) view.findViewById(R.id.thumbnail_image);
            AddButton = (Button) view.findViewById(R.id.add_button);
            NameTextView = (TextView) view.findViewById(R.id.name);
            IDTextView = (TextView) view.findViewById(R.id.userid);
            StateNumTextView = (TextView) view.findViewById(R.id.state_number);
            BlogNumTextView = (TextView) view.findViewById(R.id.blog_number);

            NameTextView.setText(Name);
            IDTextView.setText(UserID);

            AddButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Contact m = new Contact(Integer.parseInt(UserID), Name);
                    if (!members.contains(m)) {
                        newMembers.add(m);
                        members.add(m);
                        Toast.makeText(context, MemberFromSearch.this.getResources().getString(R.string.add_success), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(context, MemberFromSearch.this.getResources().getString(R.string.add_failure), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            setOnClickListener(new OnCardClickListener() {
                @Override
                public void onClick(Card card, View view) {
                    Intent intent = new Intent(MemberFromSearch.this, UserInfoActivity.class);
                    intent.putExtra("name", Name);
                    intent.putExtra("userid", UserID);
                    intent.putExtra("keyword", getSharedPreferences("search", Context.MODE_PRIVATE).getString("keyword", null));
                    startActivity(intent);
                    overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                }
            });
        }
        public void setStateNum(Integer v) { StateNumTextView.setText(MemberFromSearch.this.getResources().getString(R.string.related_tweets) + String.valueOf(v)
                + MemberFromSearch.this.getResources().getString(R.string.items)); }
        public void setBlogNum(Integer v) { BlogNumTextView.setText(MemberFromSearch.this.getResources().getString(R.string.related_posts) + String.valueOf(v)
                + MemberFromSearch.this.getResources().getString(R.string.items)); }
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
                            SearchItemCard card = new SearchItemCard(MemberFromSearch.this,
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
            switch(success) {
                case -1:
                    Toast.makeText(MemberFromSearch.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                    break;
                case 0:
                    TextView_Item_Number.setText(MemberFromSearch.this.getResources().getString(R.string.no_related_user));
                    SearchAdapter.notifyDataSetChanged();
                    break;
                default:
                    SharedPreferences.Editor editor = getSharedPreferences("search", Context.MODE_PRIVATE).edit();
                    editor.putString("keyword", keyWord);
                    editor.commit();
                    TextView_Item_Number.setText(MemberFromSearch.this.getResources().getString(R.string.found)
                            + String.valueOf(success)
                            + MemberFromSearch.this.getResources().getString(R.string.related_users));
                    SearchAdapter.notifyDataSetChanged();
                    break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable("new_members", newMembers);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
                overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("new_members", newMembers);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
        overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
    }

}
