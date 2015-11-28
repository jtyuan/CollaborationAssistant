package edu.pku.assistant.Fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.cardsui.Card;
import com.afollestad.cardsui.CardAdapter;
import com.afollestad.cardsui.CardBase;
import com.afollestad.cardsui.CardListView;
import com.jpardogo.android.googleprogressbar.library.FoldingCirclesDrawable;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import edu.pku.assistant.Group.GroupActivity;
import edu.pku.assistant.Group.util.Group;
import edu.pku.assistant.Group.util.TimeSeg;
import edu.pku.assistant.HomeActivity;
import edu.pku.assistant.IntroActivity;
import edu.pku.assistant.R;
import edu.pku.assistant.Tool.Constants;
import edu.pku.assistant.Tool.Contact;
import edu.pku.assistant.Tool.CustomerHttpClient;
import edu.pku.assistant.Tool.Database;

@SuppressWarnings("unchecked")
public class GroupFragment extends Fragment {

    private static final String FRAGMENT_TITLE_CN = "讨论组";//this.getResources().getString(R.string.title_fragment_group);;
    private static final String FRAGMENT_TITLE_EN = "Groups";

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    private ArrayList<Group> l = new ArrayList<Group>();
    private Bundle savedInstanceState;

    private int userId;

    private ProgressBar progressBar;
    private CardListView list;

    public static GroupFragment newInstance() {
        GroupFragment fragment = new GroupFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public GroupFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
//        if (getArguments() != null) {
//        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_group, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.savedInstanceState = savedInstanceState;

        SharedPreferences mConfig = getActivity().getSharedPreferences("account", Context.MODE_PRIVATE);
        userId = mConfig.getInt("userid", -1);

        progressBar = (ProgressBar) getActivity().findViewById(R.id.progressBar);
        progressBar.setIndeterminateDrawable(new FoldingCirclesDrawable.Builder(getActivity())
                //.colors(getResources().getIntArray(R.array.colors) //Array of 4 colors
                .build());

        list = (CardListView) getActivity().findViewById(R.id.card_list);
        CardAdapter adapter = new GroupCardAdapter(getActivity(), android.R.color.holo_blue_dark);
        list.setAdapter(adapter);
//        new FetchGroupTask(String.valueOf(userId)).execute();
        initView();
    }

    private void initView() {


        list = (CardListView) getActivity().findViewById(R.id.card_list);
        CardAdapter adapter = new GroupCardAdapter(getActivity(), android.R.color.holo_blue_dark);
        list.setAdapter(adapter);

        initDB();
        if (l.size() != 0) {
            for (Group g : l) {
                Card card = new Card(g.groupName, sdf.format(g.createDate.getTime()));
                adapter.add(card);
            }
        } else { // empty group to show intro card
            Card introCard = new IntroCard(getActivity(), R.id.tv_intro_title, R.id.tv_intro_text);
            adapter.add(introCard);
        }
        progressBar.setVisibility(View.INVISIBLE);
        list.setOnCardClickListener(new CardListView.CardClickListener() {
            @Override
            public void onCardClick(int index, CardBase card, View view) {
                if (l.size() != 0) {
                    Intent intent = new Intent(getActivity(), GroupActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("newGroup", false);
                    bundle.putSerializable("groupInfo", l.get(index));
                    intent.putExtras(bundle);
                    getActivity().startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                } else {
                    // TODO reaction
                    Toast.makeText(getActivity(), "oh~yeah", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initDB() {
        Database db = new Database(getActivity());
        db.Open();
        Cursor cursor = db.getGroups();
        cursor.moveToFirst();
        l.clear();
        while (!cursor.isAfterLast()) {
            Calendar calendar = Calendar.getInstance();
            String[] title_raw = cursor.getString(1).split(",");
            String beginTime = cursor.getString(5), endTime = cursor.getString(6);
            String[] beginTimes = null, endTimes = null;
            if (beginTime != null && endTime != null) {
                beginTimes = beginTime.split(",");
                endTimes = endTime.split(",");
            }
            calendar.setTime(new Date(Long.valueOf(cursor.getString(4) + "000")));
            Group group;
            ArrayList<TimeSeg> rendezvous = new ArrayList<TimeSeg>();
            if (beginTimes != null) {
                for (int i = 0; i < beginTimes.length; ++i) {
                    Calendar beginDate = Calendar.getInstance(), endDate = Calendar.getInstance();
                    beginDate.setTime(new Date(Long.valueOf(beginTimes[i] + "000")));
                    endDate.setTime(new Date(Long.valueOf(endTimes[i] + "000")));
                    String desc = getResources().getString(R.string.ts_description);
                    if (i+1 < title_raw.length)
                        desc = title_raw[i+1];
                    rendezvous.add(new TimeSeg(beginDate, endDate, desc));
                }
                group = new Group(cursor.getInt(0), title_raw[0], Integer.parseInt(cursor.getString(3)), calendar, rendezvous);
            } else {
                group = new Group(cursor.getInt(0), title_raw[0], Integer.parseInt(cursor.getString(3)), calendar);
            }

            l.add(group);
            cursor.moveToNext();
        }
        db.Close();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_group, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add: {
                Intent intent = new Intent(getActivity(), GroupActivity.class);
                Bundle bundle = new Bundle();
                bundle.putBoolean("newGroup", true);
                bundle.putSerializable("groupInfo", new Group(-1, userId, Calendar.getInstance()));
                intent.putExtras(bundle);
                getActivity().startActivityForResult(intent, 0);
                getActivity().overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public void onResume() {
        onActivityCreated(this.savedInstanceState);
    }

    private class IntroCard extends Card {
        private static final long serialVersionUID = 5192956631596646667L;

        public IntroCard(Context context, int title, int content) {
            super(context, title, content);
        }

        @Override
        public int getLayout() {
            return R.layout.card_intro;
        }
    }

    private class GroupCardAdapter extends CardAdapter {
        public GroupCardAdapter(Context context, int accentColorRes) {
            super(context, accentColorRes);
            registerLayout(R.layout.card_intro);
        }

        @Override
        public View onViewCreated(int index, View recycled, CardBase item) {
            View view = super.onViewCreated(index, recycled, item);
            Button button = (Button) view.findViewById(R.id.bt_intro);
            ImageView imageView = (ImageView) view.findViewById(R.id.img_add);
            if (button != null) {
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), IntroActivity.class);
                        intent.putExtra("finishflag", false);
                        getActivity().startActivity(intent);
                    }
                });
            }
            if (imageView != null) {
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), GroupActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("newGroup", true);
                        bundle.putSerializable("groupInfo", new Group(-1, userId, Calendar.getInstance()));
                        intent.putExtras(bundle);
                        getActivity().startActivityForResult(intent, 0);
                        getActivity().overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                    }
                });
            }
            return view;
        }
    }
}