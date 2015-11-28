package edu.pku.assistant.Tool;

import java.util.Vector;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import edu.pku.assistant.BindActivity;
import edu.pku.assistant.HomeActivity;
import edu.pku.assistant.R;
import edu.pku.assistant.TokenActivity;

public class BindAdapter extends BaseAdapter{
    private Context context;
    private LayoutInflater inflater;
    private Vector<App> apps = new Vector<App>();
    public void init(){
        apps.add(new App("微信",R.drawable.logo_wechat));
        apps.add(new App("微博", R.drawable.logo_weibo));
        apps.add(new App("人人",R.drawable.logo_renren));
        apps.add(new App("Google", R.drawable.logo_google));
    }

    public BindAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        init();
    }

    @Override
    public int getCount() {
        return apps.size();
    }

    @Override
    public Object getItem(int position) {
        return apps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null){
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.bind_item, null);
            holder.app_logo = (ImageView) convertView.findViewById(R.id.app_logo);
            holder.app_name = (TextView) convertView.findViewById(R.id.app_name);
            holder.button = (Button) convertView.findViewById(R.id.bind_button);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        holder.app_logo.setImageResource(apps.get(position).getLogo());
        holder.app_name.setText(apps.get(position).getAppName());
        SharedPreferences mConfig = context.getSharedPreferences("bind", Context.MODE_PRIVATE);
        if (mConfig.getInt(apps.get(position).getAppName() + "status", 0) == 1) {
            holder.button.setText("已绑定");
            holder.button.setClickable(false);
        }
        else {
            holder.button.setText("未绑定");
            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, TokenActivity.class);
                    intent.putExtra("type", apps.get(position).getAppName());
                    ((BindActivity)context).startActivityForResult(intent, 1);
                }
            });
        }
        return convertView;
    }


    public final class ViewHolder{
        public ImageView app_logo;
        public TextView app_name;
        public Button button;
    }

    public class App {
        private String appName;
        private int logo;
        public App(String appName_, int logo_) {
            appName = appName_;
            logo = logo_;
        }
        public String getAppName(){
            return this.appName;
        }
        public int getLogo(){
            return this.logo;
        }
    }
}