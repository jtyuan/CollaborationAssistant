package edu.pku.assistant.Tool;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import edu.pku.assistant.R;

public class DrawerAdapter extends BaseAdapter{

    private Context context;
    private String[] drawerName;
    private LayoutInflater inflater;


    public DrawerAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        drawerName = new String[]{
            context.getString(R.string.drawer_group),
            context.getString(R.string.drawer_contact),
            context.getString(R.string.drawer_search),
            context.getString(R.string.drawer_notice),
            "搜索测试",
            context.getString(R.string.drawer_bind),
            context.getString(R.string.drawer_help),
            context.getString(R.string.drawer_logout)};

    }
    @Override
    public int getCount() {
        return drawerName.length;
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.drawer_list_item, null);
            holder.icon = (ImageView) convertView.findViewById(R.id.item_logo);
            holder.name = (TextView) convertView.findViewById(R.id.item_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.name.setText(drawerName[position]);
        switch(position)
        {
            case 0:
                holder.icon.setImageResource(R.drawable.drawer_group);
                break;
            case 1:
                holder.icon.setImageResource(R.drawable.drawer_contact);
                break;
            case 2:
                holder.icon.setImageResource(R.drawable.drawer_search);
                break;
            case 3:
                holder.icon.setImageResource(R.drawable.ic_action_email);
                break;
            default:

                holder.icon.setVisibility(View.GONE);
        }
        return convertView;
    }
    public final class ViewHolder{
        public ImageView icon;
        public TextView name;
    }
}
