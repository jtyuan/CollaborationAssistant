package edu.pku.assistant.Group.recommendation;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.cardsui.CardListView;
import com.beardedhen.androidbootstrap.BootstrapButton;

import java.text.SimpleDateFormat;

import edu.pku.assistant.Group.util.TimeSeg;
import edu.pku.assistant.Group.util.adapter.TimeList.TimeItem;
import edu.pku.assistant.Group.util.adapter.TimeListDetail.TimeDetailAdapter;
import edu.pku.assistant.R;


public class ResultConfirm extends FragmentActivity {

    TimeItem item;
    TimeSeg ts;

    private boolean isFastScroll = false;

    public CardListView list;
    public TimeDetailAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list_with_button);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            item = (TimeItem)bundle.getSerializable("item");
            ts = item.getTimeSeg();
            SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm");
            SimpleDateFormat sdf2 = new SimpleDateFormat(" - HH:mm");
            this.setTitle(sdf.format(ts.beg.getTime()) + sdf2.format(ts.end.getTime()));
        }

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        list = (CardListView) findViewById(R.id.card_list);
        BootstrapButton button = (BootstrapButton) findViewById(R.id.button_confirm);

        initializeAdapter();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable("result", ts);
                intent.putExtras(bundle);
                ResultConfirm.this.setResult(RESULT_OK, intent);
                ResultConfirm.this.finish();
                overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NewApi")
    private void initializeAdapter() {

        list.setFastScrollEnabled(isFastScroll);
        if (isFastScroll && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            list.setFastScrollAlwaysVisible(true);
        }

        adapter = new TimeDetailAdapter(this, android.R.color.holo_blue_dark, Recommendation.members, this.item.silkid, this.ts.beg, this.ts.end);
        list.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
    }
}
