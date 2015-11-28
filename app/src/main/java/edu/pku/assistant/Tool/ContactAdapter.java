package edu.pku.assistant.Tool;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
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
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import edu.pku.assistant.ContactInfoActivity;
import edu.pku.assistant.Fragment.SearchFragment;
import edu.pku.assistant.R;

public class ContactAdapter extends ArrayAdapter<Contact> {

    private Context context;

    private LayoutInflater inflater;

    private Database database;

    public ContactAdapter(Context context, int textViewResourceId, List<Contact> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        inflater = LayoutInflater.from(context);
        database = new Database(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Contact contact = getItem(position);
        ViewHolder holder;
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
            holder.Befriend.setText(context.getResources().getString(R.string.add_new));
            holder.Befriend.setOnClickListener(new View.OnClickListener() {
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
                    menu.add(R.id.group_add, Menu.NONE, Menu.NONE, context.getResources().getString(R.string.new_group));
                    pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            Log.d("!!!", String.valueOf(menuItem.getItemId()));
                            if (menuItem.getGroupId() == R.id.group_add) {
                                final EditText groupEditText = new EditText(context);
                                new AlertDialog.Builder(context).setTitle(context.getResources().getString(R.string.please_group))
                                        .setView(groupEditText)
                                        .setPositiveButton(context.getResources().getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                String groupname = groupEditText.getText().toString();
                                                int CreateID = context.getSharedPreferences("account", Context.MODE_PRIVATE).getInt("userid", 0);
                                                new InsertGroupTask(context, groupname, CreateID, contact.getName(), String.valueOf(contact.getId())).execute();
                                            }
                                        })
                                        .setNegativeButton(context.getResources().getString(R.string.button_cancel), null)
                                        .show();
                            } else {
                                int GroupID = menuItem.getItemId();
                                new InsertContactToGroupTask(context, contact.getName(), String.valueOf(contact.getId()), GroupID).execute();
                            }
                            return true;
                        }
                    });
                    pop.show();
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
                    Toast.makeText(context, context.getResources().getString(R.string.add_success), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(context, context.getResources().getString(R.string.add_failure), Toast.LENGTH_SHORT).show();
            else if (retValue == 1)
                Toast.makeText(context, context.getResources().getString(R.string.add_success), Toast.LENGTH_SHORT).show();
        }
    }
}