package edu.pku.assistant.Group.util.adapter.MemberList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import edu.pku.assistant.Group.member.MemberFromContacts;
import edu.pku.assistant.R;
import edu.pku.assistant.Tool.Constants;
import edu.pku.assistant.Tool.Contact;
import edu.pku.assistant.Tool.CustomerHttpClient;
import edu.pku.assistant.Tool.Database;

public class ContactAdapter extends ArrayAdapter<Contact> {

    private Context context;

    private LayoutInflater inflater;

    public ContactAdapter(Context context, int textViewResourceId, List<Contact> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Contact contact = getItem(position);
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.contact_item, null);
            holder.Thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail_image);
            holder.Name = (TextView) convertView.findViewById(R.id.name);
            holder.Befriend = (Button) convertView.findViewById(R.id.befriend);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }



        holder.Name.setText(contact.getName());
        holder.Thumbnail.setImageBitmap(contact.getThumbnail());
        if (contact.getUserId() != -1) {
            final int index = position;
            holder.Befriend.setText("添加到");
            holder.Befriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Contact m = new Contact(((MemberFromContacts) context).Contacts.get(index).getId(), ((MemberFromContacts) context).Contacts.get(index).getName()); // User ID
                    if (!((MemberFromContacts) context).members.contains(m)) {
                        ((MemberFromContacts) context).newMembers.add(m);
                        ((MemberFromContacts) context).members.add(m);
                        Toast.makeText(context, context.getResources().getString(R.string.add_success), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(context, context.getResources().getString(R.string.add_failure), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            holder.Befriend.setText("邀请");
            holder.Befriend.setOnClickListener(null);
        }
        return convertView;
    }

    public final class ViewHolder{
        public ImageView Thumbnail;
        public TextView Name;
        public Button Befriend;
    }

}