package com.tg.derdoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tg.globals.AppGlobals;
import com.tg.dataObject.UserProfileDataObject;
import com.tg.helper.CustomDialog;

import java.util.Calendar;

public class ProfileNameActivity extends BaseActivity {

    private final String allowedCharsStr = "abcçdefghıijklmnoöprsştuüvyzABCÇDEFGHIİJKLMNOÖPRSŞTUÜVYZ";

    private EditText userNameEditText = null;

    private static final int TEXT_MAX_LENGTH = 10;

    public ProfileNameActivity() {
        super(R.layout.activity_profile_name);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(AppGlobals.facebookUserData != null) {
            TextView profileNameTextView = findViewById(R.id.user_name_input);
            profileNameTextView.setText(AppGlobals.facebookUserData.firstName);
        }

        final android.widget.Button profileNameOk = findViewById(R.id.profileNameOK);
        profileNameOk.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                GetProfileNameAndRedirect();
            }
        });

        userNameEditText = (EditText) findViewById(R.id.user_name_input);

        InputFilter charFilter = new InputFilter()
        {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend)
            {
                for (int i = start; i < end; i++) {
                    if (!Character.isLetter(source.charAt(i)) && !Character.isSpaceChar(source.charAt(i))) {
                        showPreventionDialog();
                        return "";
                    }
                }

                return null;
            }
        };

        InputFilter lengthFilter = new InputFilter.LengthFilter(TEXT_MAX_LENGTH);
        InputFilter[] inputFDilters = new InputFilter[]{ charFilter, lengthFilter };
        userNameEditText.setFilters(inputFDilters);

        TextWatcher tw = new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!allowedCharsStr.contains(charSequence)) {
                    showPreventionDialog();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!allowedCharsStr.contains(s)) {
                    showPreventionDialog();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        //userNameEditText.addTextChangedListener(tw);
    }

    private void showPreventionDialog() {
        new CustomDialog(this, getResources().getString(R.string.profile_name_activity_prevention_dialog), R.drawable.warning, true, null, false, false,0, null).show();
    }

    private void GetProfileNameAndRedirect() {
        //TODO : GET LOGIN DATA!!!
        AppGlobals.hasLoginData = true;
        //

        EditText usernameEditText = (EditText) findViewById(R.id.user_name_input);
        String userName = usernameEditText.getText().toString();

        if(userName == null || userName.equals("")) {
            return;
        }

        UserProfileDataObject.userName = userName;

        RedirectTo(ProfileBirthDateActivity.class);
    }
}
