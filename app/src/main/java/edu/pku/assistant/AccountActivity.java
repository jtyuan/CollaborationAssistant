package edu.pku.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.Attributes;


public class AccountActivity extends Activity {

    ImageView ThumbnailView;
    TextView NameTextView;
    TextView PhoneTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        ThumbnailView = (ImageView) findViewById(R.id.thumbnail);
        NameTextView = (TextView) findViewById(R.id.name);
        PhoneTextView = (TextView) findViewById(R.id.phone);

        SharedPreferences sh = getSharedPreferences("account", Context.MODE_PRIVATE);
        setTitle(sh.getString("username", null));
        NameTextView.setText(sh.getString("username", null));
        PhoneTextView.setText(sh.getString("phone", null));
        Bitmap thumbnail = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath() + "/Assistant/thumbnail/me.jpg");
        if (thumbnail != null) ThumbnailView.setImageBitmap(thumbnail);
        NameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText editText = new EditText(AccountActivity.this);
                AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.this);
                builder.setTitle(getResources().getString(R.string.account_change_name))
                        .setView(editText)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                NameTextView.setText(editText.getText());
                                setTitle(editText.getText());
                                //TODO interact with server
                                SharedPreferences.Editor editor = getSharedPreferences("account", Context.MODE_PRIVATE).edit();
                                editor.putString("username", editText.getText().toString()).apply();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .create().show();
            }
        });

        ThumbnailView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 0);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (data != null) startPhotoZoom(data.getData());
        } else if (requestCode == 1) {
            if (data != null) {
                getImageToView(data);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void startPhotoZoom(Uri uri) {

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 320);
        intent.putExtra("outputY", 320);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, 1);
    }
    private void getImageToView(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            saveToStorage(photo);
            Drawable drawable = new BitmapDrawable(photo);
            ThumbnailView.setImageDrawable(drawable);
        }
    }

    private void saveToStorage(Bitmap mBitmap) {

        String sdStatus = Environment.getExternalStorageState();
        String pathName = Environment.getExternalStorageDirectory().getPath() + "/Assistant/thumbnail/";
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
            return;
        }

        String fileName = pathName + "me.jpg";
        FileOutputStream b = null;
        File path = new File(pathName);
        if (!path.exists()) {
            path.mkdirs();
        }
        File file = new File(fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            b = new FileOutputStream(fileName);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                b.flush();
                b.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
