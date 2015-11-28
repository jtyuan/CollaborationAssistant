package edu.pku.assistant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.print.PageRange;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

import edu.pku.assistant.Tool.ViewPagerAdapter;


public class IntroActivity extends Activity implements View.OnClickListener,ViewPager.OnPageChangeListener {

    private Button startButton;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private ArrayList<View> views;
    private static final int[] pics = {R.drawable.help_home,R.drawable.help_welcome,R.drawable.help_addgroup,R.drawable.help_search, R.drawable.help_addmember};
    private ImageView[] points;
    private int currentIndex;
    private Boolean finishFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        finishFlag = getIntent().getBooleanExtra("finishflag", false);
        views = new ArrayList<View>();
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPagerAdapter = new ViewPagerAdapter(views);
        startButton = (Button) findViewById(R.id.btn_start);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (finishFlag) finish();
                else {
                    Intent intent = new Intent(IntroActivity.this, HomeActivity.class);
                    startActivity(intent);
                }
            }
        });

        LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        for(int i=0; i<pics.length; i++) {
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(mParams);
            imageView.setImageResource(pics[i]);
            views.add(imageView);
        }
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOnPageChangeListener(this);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.dot_layout);

        points = new ImageView[pics.length];

        for (int i = 0; i < pics.length; i++) {
            points[i] = (ImageView) linearLayout.getChildAt(i);
            points[i].setEnabled(true);
            points[i].setOnClickListener(this);
            points[i].setTag(i);
        }

        currentIndex = 0;
        points[currentIndex].setEnabled(false);
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }

    @Override
    public void onPageSelected(int position) {
        setCurDot(position);
        if (position == pics.length-1) startButton.setVisibility(View.VISIBLE);
        else startButton.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onClick(View v) {
        int position = (Integer)v.getTag();
        setCurView(position);
        setCurDot(position);
    }

    private void setCurView(int position){
        if (position < 0 || position >= pics.length) {
            return;
        }
        viewPager.setCurrentItem(position);
    }

    private void setCurDot(int positon){
        if (positon < 0 || positon > pics.length - 1 || currentIndex == positon) {
            return;
        }
        points[positon].setEnabled(false);
        points[currentIndex].setEnabled(true);

        currentIndex = positon;
    }
}
