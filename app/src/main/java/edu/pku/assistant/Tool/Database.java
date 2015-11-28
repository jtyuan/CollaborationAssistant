package edu.pku.assistant.Tool;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;


public class Database
{
    static final String KEY_CONTACT_NAME = "name";
    static final String KEY_CONTACT_USERID = "userid";
    static final String KEY_CONTACT_RENRENID = "renrenid";
    static final String KEY_CONTACT_USERNAME = "username";
    static final String KEY_CONTACT_PHONE = "phone";

    static final String KEY_GROUP_ID = "_id";
    static final String KEY_GROUP_NAME = "name";
    static final String KEY_GROUP_NUM = "num";
    static final String KEY_GROUP_HOST = "host";
    static final String KEY_GROUP_CREATEDATE = "createdate";
    static final String KEY_GROUP_BEGINDATE = "begindate";
    static final String KEY_GROUP_ENDDATE = "enddate";

    static final String KEY_RELATION_ID = "_id";
    static final String KEY_RELATION_GROUP = "groupid";
    static final String KEY_RELATION_CONTACT = "contactid";

    static final String KEY_FRIEND_USERID = "userid";
    static final String KEY_FRIEND_NAME = "name";
    static final String KEY_FRIEND_PHONE = "phone";

    static final String DATABASE_NAME = "contactsdb";
    static final String TABLE_GROUP = "groups";
    static final String TABLE_CONTACT = "contact";
    static final String TABLE_RELATION = "relation";
    static final String TABLE_FRIEND = "friend";
    static final int VERSION = 1;

    Context context = null;
    DatabaseHelper DBHelper;
    SQLiteDatabase db;

    public class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            try
            {
                db.execSQL("CREATE TABLE friend ( userid integer primary key, name text not null, phone text not null);");
                db.execSQL("CREATE TABLE contact ( _id integer primary key autoincrement, renrenid integer, " +
                        "name text not null, userid integer not null, username text, phone text);");
                db.execSQL("CREATE TABLE groups ( _id integer primary key, " +
                        "name text not null, num integer not null, host text not null, " +
                        "createdate text not null, begindate text, enddate text);");
                db.execSQL("CREATE TABLE relation ( _id integer primary key, " +
                        "groupid integer not null, contactid integer not null);");
            }
            catch(SQLException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            try
            {
                db.execSQL("DROP TABLE IF EXISTS contact;");
                db.execSQL("DROP TABLE IF EXISTS groups;");
                db.execSQL("DROP TABLE IF EXISTS relation;");
                db.execSQL("DROP TABLE IF EXISTS friend;");
            }
            catch(SQLException e)
            {
                e.printStackTrace();
            }
            onCreate(db);
        }
    }
    public Database (Context context)
    {
        this.context = context;
        DBHelper = new DatabaseHelper(context);
    }

    public void Clear()
    {
        db.execSQL("DELETE FROM contact;");
        db.execSQL("DELETE FROM groups;");
        db.execSQL("DELETE FROM relation;");
        db.execSQL("DELETE FROM friend;");
    }

    public Database Open() throws SQLException
    {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    public void Close()
    {
        DBHelper.close();
    }

    public Cursor getContacts()
    {
        Cursor cursor = db.query(TABLE_CONTACT, new String[]{KEY_CONTACT_RENRENID, KEY_CONTACT_NAME, KEY_CONTACT_USERID, KEY_CONTACT_USERNAME, KEY_CONTACT_PHONE}, null,
                null, null, null, KEY_CONTACT_NAME + " ASC");
        return cursor;
    }

    public Cursor getFriends()
    {
        Cursor cursor = db.query(TABLE_CONTACT, new String[]{KEY_CONTACT_RENRENID, KEY_CONTACT_NAME, KEY_CONTACT_USERID, KEY_CONTACT_USERNAME, KEY_CONTACT_PHONE},
                KEY_CONTACT_USERID + "!='-1'", null, null, null, KEY_CONTACT_NAME + " ASC");
        return cursor;
    }

    public Cursor getContactById(int id)
    {
        return db.query(TABLE_CONTACT, new String[]{KEY_CONTACT_NAME, KEY_CONTACT_RENRENID}, KEY_CONTACT_RENRENID + "='" + id + "'",
                null, null, null, null);
    }

    public Cursor getGroupMembers(String groupname)
    {
        int groupid;
        Cursor cursor = db.query(TABLE_GROUP, new String[]{KEY_GROUP_ID}, KEY_GROUP_NAME + "='" + groupname + "'",
                null, null, null, null);
        cursor.moveToFirst();
        groupid = cursor.getInt(0);
        cursor = db.query(TABLE_RELATION, new String[]{KEY_RELATION_CONTACT}, KEY_RELATION_GROUP + "='" + groupid + "'",
                null ,null, null, null);
        return cursor;
    }

    public Cursor getGroups()
    {
        Cursor cursor = db.query(TABLE_GROUP, new String[]{KEY_GROUP_ID, KEY_GROUP_NAME, KEY_GROUP_NUM, KEY_GROUP_HOST,
                KEY_GROUP_CREATEDATE, KEY_GROUP_BEGINDATE, KEY_GROUP_ENDDATE}, null,
                null, null, null, null);
        return cursor;
    }

    public boolean InsertContacts(int group, String name, int renrenid, String username, int userid, String phone)
    {
        Cursor cursor;
        cursor = db.query(TABLE_CONTACT, new String[]{KEY_CONTACT_RENRENID},
                KEY_CONTACT_RENRENID + "='" + String.valueOf(renrenid) + "'", null, null, null, null);
        if (renrenid == -1 || !cursor.moveToFirst()) {
            ContentValues initialValues = new ContentValues();
            initialValues.put(KEY_CONTACT_NAME, name);
            initialValues.put(KEY_CONTACT_RENRENID, renrenid);
            initialValues.put(KEY_CONTACT_USERID, userid);
            initialValues.put(KEY_CONTACT_USERNAME, username);
            initialValues.put(KEY_CONTACT_PHONE, phone);
            db.insert(TABLE_CONTACT, null, initialValues);
        }

        if (group != -1) {
            ContentValues newValues = new ContentValues();
            newValues.put(KEY_RELATION_CONTACT, renrenid);
            newValues.put(KEY_RELATION_GROUP, group);
            db.insert(TABLE_RELATION, null, newValues);

            cursor = db.query(TABLE_GROUP, new String[]{KEY_GROUP_NUM},
                    KEY_GROUP_ID + "='" + group + "'", null, null, null, null);
            cursor.moveToFirst();
            int groupnum = cursor.getInt(0);
            newValues = new ContentValues();
            newValues.put(KEY_GROUP_NUM, groupnum + 1);
            db.update(TABLE_GROUP, newValues, KEY_GROUP_ID + "=" + group, null);
        }

        return true;
    }

    public void DeleteContactsFromGroup (String userid, String groupname)
    {
        int groupid, groupnum;
        Cursor cursor = db.query(TABLE_GROUP, new String[]{KEY_GROUP_ID, KEY_GROUP_NUM},
                KEY_GROUP_NAME + "='" + groupname + "'" ,null, null, null, null);
        cursor.moveToFirst();
        groupid = cursor.getInt(0);
        groupnum = cursor.getInt(1);
        db.delete(TABLE_RELATION, KEY_RELATION_GROUP + "='" + groupid + "' and " + KEY_RELATION_CONTACT + "='" + userid + "'", null);
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_GROUP_NUM, groupnum - 1);
        db.update(TABLE_GROUP, newValues, KEY_GROUP_ID + "=" + groupid, null);
    }

    public boolean InsertGroup(int id, String name, String host, String createdate)
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_GROUP_ID, id);
        initialValues.put(KEY_GROUP_NAME, name);
        initialValues.put(KEY_GROUP_NUM, 0);
        initialValues.put(KEY_GROUP_HOST, host);
        initialValues.put(KEY_GROUP_CREATEDATE, createdate);
        db.insert(TABLE_GROUP, null, initialValues);
        return true;
    }

    public void DeleteGroup(String name)
    {
        int id;
        Cursor cursor = db.query(TABLE_GROUP, new String[]{KEY_GROUP_ID}, KEY_GROUP_NAME + "='" + name + "'",
                null, null, null, null, null);
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
            cursor = db.query(TABLE_RELATION, new String[]{KEY_RELATION_CONTACT, KEY_RELATION_GROUP},
                    KEY_RELATION_GROUP + "='" + id + "'", null, null, null, null, null);
            if (cursor.moveToFirst()) {
                for (int i = 0; i < cursor.getCount(); i++)
                    db.delete(TABLE_RELATION, KEY_RELATION_GROUP + "='" + id + "'", null);
            }
            db.delete(TABLE_GROUP, KEY_GROUP_ID + "='" + id + "'", null);
        }
    }

    public boolean UpdateGroup(String oldname, String newname)
    {
        ContentValues newValues;
        newValues = new ContentValues();
        newValues.put(KEY_GROUP_NAME, newname);
        db.update(TABLE_GROUP, newValues, KEY_GROUP_NAME + "='" + oldname +"'", null);
        return true;
    }

    public boolean UpdateGroupTime(int groupid, String begintime, String endtime)
    {
        ContentValues newValues;
        newValues = new ContentValues();
        newValues.put(KEY_GROUP_BEGINDATE, begintime);
        newValues.put(KEY_GROUP_ENDDATE, endtime);
        db.update(TABLE_GROUP, newValues, KEY_GROUP_ID + "='" + groupid + "'", null);
        return true;
    }

    public String getFullPinYin(String source) {
        ArrayList<HanziToPinyin.Token> tokens = HanziToPinyin.getInstance().get(source);
        StringBuilder sb = new StringBuilder();
        if (tokens != null && tokens.size() > 0) {
            for (HanziToPinyin.Token token : tokens) {
                if (HanziToPinyin.Token.PINYIN == token.type) {
                    sb.append(token.target);
                } else {
                    sb.append(token.source);
                }
            }
        }
        return sb.toString().toLowerCase();
    }
}
