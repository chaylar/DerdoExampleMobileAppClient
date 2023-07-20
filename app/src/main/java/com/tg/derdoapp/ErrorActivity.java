package com.tg.derdoapp;

import android.content.res.Resources;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.tg.dataObject.ErrorDataObject;
import com.tg.dataObject.UserProfileDataObject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;

public class ErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        String errorText = null;
        if(ErrorDataObject.errorText != null) {
            errorText = ErrorDataObject.errorText;
        }
        else {
            errorText = getResources().getString(R.string.error_activity_standard_text);
        }

        final android.widget.TextView errorTextView = findViewById(R.id.lower_info_text);
        errorTextView.setText(errorText);

        ErrorDataObject.errorText = null;
    }

}
