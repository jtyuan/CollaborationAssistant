package edu.pku.assistant.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
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


public class SearchFragment extends Fragment {

    private static final String FRAGMENT_TITLE_CN = "搜索";
    private static final String FRAGMENT_TITLE_EN = "Search";

    private TextView TextView_Item_Number;
    private Card card;
    private CardView cardView;
    private CardListView ListView_Search;

    private SharedPreferences mConfig;
    private int userId;
    private ArrayList<Card> SearchItemCards;
    private CardArrayAdapter SearchAdapter;
    private Database database;


    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public SearchFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
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
        SearchAdapter = new CardArrayAdapter(getActivity(), SearchItemCards);

        ListView_Search = (CardListView) getActivity().findViewById(R.id.search_list);
        ListView_Search.setAdapter(SearchAdapter);
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
        protected Button AddButton;
        protected TextView NameTextView;
        protected TextView IDTextView;
        protected TextView StateNumTextView;
        protected TextView BlogNumTextView;

        private String Name;
        private String UserID;
        private Context context;

        private int CreateID;

        public SearchItemCard(Context context, String name, String userid) {
            super(context, R.layout.card_search_item);
            Name = name;
            UserID = userid;
            CreateID = getActivity().getSharedPreferences("account", Context.MODE_PRIVATE).getInt("userid", 0);
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
                    database.Open();
                    Cursor cursor = database.getGroups();
                    PopupMenu pop = new PopupMenu(context, view);
                    pop.inflate(R.menu.group_select);
                    Menu menu = pop.getMenu();
                    final ArrayList<String> groupNameInDB = new ArrayList<String>();
                    if (cursor.moveToFirst()) {
                        for (int i = 0; i < cursor.getCount(); i++, cursor.moveToNext()) {
                            groupNameInDB.add(cursor.getString(1));
                            menu.add(R.id.group_select, cursor.getInt(0), Menu.NONE, groupNameInDB.get(i).split(",")[0]);
                        }
                    }
                    menu.add(R.id.group_add, Menu.NONE, Menu.NONE, SearchFragment.this.getActivity().getResources().getString(R.string.new_group));
                    pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(final MenuItem menuItem) {
                            Log.d("!!!", String.valueOf(menuItem.getItemId()));
                            if (menuItem.getGroupId() == R.id.group_add) {
                                final EditText groupEditText = new EditText(context);
                                new AlertDialog.Builder(context).setTitle(SearchFragment.this.getActivity().getResources().getString(R.string.please_group))
                                        .setView(groupEditText)
                                        .setPositiveButton(SearchFragment.this.getActivity().getResources().getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                String groupname = groupEditText.getText().toString();
                                                new InsertGroupTask(getActivity(), groupname, CreateID, Name, UserID).execute();
                                            }
                                        })
                                        .setNegativeButton(SearchFragment.this.getActivity().getResources().getString(R.string.button_cancel), null)
                                        .show();
                            } else {
                                int GroupID = menuItem.getItemId();
                                new InsertContactToGroupTask(getActivity(), Name, UserID, GroupID).execute();
                            }
                            return true;
                        }
                    });
                    pop.show();
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
            StateNumTextView.setText(SearchFragment.this.getActivity().getResources().getString(R.string.related_tweets) + String.valueOf(v)
                    + SearchFragment.this.getActivity().getResources().getString(R.string.items));
        }

        public void setBlogNum(Integer v) {
            BlogNumTextView.setText(SearchFragment.this.getActivity().getResources().getString(R.string.related_tweets) + String.valueOf(v)
                    + SearchFragment.this.getActivity().getResources().getString(R.string.items));
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
                    TextView_Item_Number.setText(SearchFragment.this.getActivity().getResources().getString(R.string.no_related_user));
                    SearchAdapter.notifyDataSetChanged();
                    break;
                default:
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("search", Context.MODE_PRIVATE).edit();
                    editor.putString("keyword", keyWord);
                    editor.commit();
                    TextView_Item_Number.setText(SearchFragment.this.getActivity().getResources().getString(R.string.found)
                            + String.valueOf(success)
                            + SearchFragment.this.getActivity().getResources().getString(R.string.related_users));
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
                        int MemberID = jsonInfo.getJSONArray("membersinfo").getJSONObject(0).getInt("userid");
                        String MemberName = jsonInfo.getJSONArray("membersinfo").getJSONObject(0).getString("username");
                        String Phone = jsonInfo.getJSONArray("membersinfo").getJSONObject(0).getString("phone");

                        database.Open();
                        database.InsertGroup(GroupID, GroupName, String.valueOf(Host), String.valueOf(Createdate));
                        database.InsertContacts(GroupID, UserName, Integer.parseInt(UserID), MemberName, MemberID, Phone);
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
                    Toast.makeText(context, SearchFragment.this.getActivity().getResources().getString(R.string.add_success), Toast.LENGTH_SHORT).show();
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
                    String username = jsonInfo.getString("username");
                    int userid = jsonInfo.getInt("userid");
                    String phone = jsonInfo.getString("phone");

                    if (retValue == 1) {
                        database.Open();
                        database.InsertContacts(GroupID, UserName, Integer.parseInt(UserID), username, userid, phone);
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
                Toast.makeText(context, SearchFragment.this.getActivity().getResources().getString(R.string.add_failure), Toast.LENGTH_SHORT).show();
            else if (retValue == 1)
                Toast.makeText(context, SearchFragment.this.getActivity().getResources().getString(R.string.add_success), Toast.LENGTH_SHORT).show();
        }
    }
}