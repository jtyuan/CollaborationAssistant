package edu.pku.assistant;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.pku.assistant.Tool.Constants;
import edu.pku.assistant.Tool.CustomerHttpClient;

public class SignUpActivity extends Activity {

    private UserSignupTask mAuthTask = null;

    private EditText mNicknameView;
    private EditText mPhoneView;
    private EditText mPasswordView;
    private EditText mConfirmPasswordView;
    private EditText mEmailView;
    private View mSignupFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mNicknameView = (EditText) findViewById(R.id.nickname);
        mPhoneView = (EditText) findViewById(R.id.phone);
        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mConfirmPasswordView = (EditText) findViewById(R.id.confirm_password);

        Button mSignupButton = (Button) findViewById(R.id.sign_up_button);
        mSignupButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mSignupFormView = findViewById(R.id.email_singup_form);
    }

    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        mNicknameView.setError(null);
        mPhoneView.setError(null);
        mPasswordView.setError(null);
        mConfirmPasswordView.setError(null);
        mEmailView.setError(null);

        String nickname = mNicknameView.getText().toString();
        String phone = mPhoneView.getText().toString();
        String password = mPasswordView.getText().toString();
        String cpassword = mConfirmPasswordView.getText().toString();
        String email = mEmailView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(nickname)) {
            mNicknameView.setError(getString(R.string.error_field_required));
            focusView = mNicknameView;
            cancel = true;
        }
        if (TextUtils.isEmpty(phone)) {
            mPhoneView.setError(getString(R.string.error_field_required));
            focusView = mPhoneView;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(cpassword)) {
            mConfirmPasswordView.setError(getString(R.string.error_field_required));
            focusView = mConfirmPasswordView;
            cancel = true;
        }

        else if (!isPasswordValid(password, cpassword)) {
            mConfirmPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mConfirmPasswordView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mAuthTask = new UserSignupTask(phone, nickname, password, email);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isPasswordValid(String password, String cpassword) {
        return password.equals(cpassword);
    }

    public class UserSignupTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPhone;
        private final String mNickname;
        private final String mPassword;

        UserSignupTask(String phone, String nickname, String password, String email) {
            mPhone = phone;
            mNickname = nickname;
            mPassword = password;
            mEmail = email;
        }

        @Override
        protected Boolean doInBackground(Void... param) {
            Boolean retValue;
            String respond;

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("phone", mPhone));
            params.add(new BasicNameValuePair("username", mNickname));
            params.add(new BasicNameValuePair("password", mPassword));
            params.add(new BasicNameValuePair("email", mEmail));
            try {
                respond = CustomerHttpClient.post(Constants.base_url + "contact/index.php/contact/register", params);
            } catch (RuntimeException e) {
                respond = null;
                return false;
            }
            if (respond == null) {
                retValue = false;
            } else {
                try {
                    JSONObject jsonInfo = new JSONObject(respond);
                    int result = jsonInfo.getInt("result");
                    switch (result) {
                        case 0:
                            retValue = false;
                            break;
                        case 1:
                            int userId = jsonInfo.getInt("userid");
                            String userName = jsonInfo.getString("username");
                            retValue = true;
                            break;
                        default:
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                Toast.makeText(SignUpActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(SignUpActivity.this, "注册失败，请重试", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }
}



