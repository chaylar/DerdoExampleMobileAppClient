package com.tg.helper;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.tg.derdoapp.BaseActivity;
import com.tg.derdoapp.NavigationBaseActivity;
import com.tg.derdoapp.R;

import java.util.ArrayList;
import java.util.List;

public class CustomImageDialog extends Dialog {

    private BaseActivity appContext;
    private View v;

    public CustomImageDialog(BaseActivity act, View dialogView) {
        super(act);
        this.appContext = act;
        this.v = dialogView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //setContentView(R.layout.custom_image_dialog);
        setContentView(v);

        //ImageButton closeButton = findViewById(R.id.custom_image_close_dialog_button);
        Button closeButton = findViewById(R.id.custom_image_close_dialog_button);
        closeButton.getBackground().setAlpha(1);
        closeButton.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                dismiss();
            }
        });
    }
}
