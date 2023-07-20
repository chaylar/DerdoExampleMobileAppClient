package com.tg.helper;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.tg.derdoapp.R;

import java.util.Random;

public class CustomLoadingDialog extends Dialog {

    private String[] loadingSentences = new String[]{ "Yükleniyor...", "Bi sn..." };

    public CustomLoadingDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_loading_dialog);

        //TextView textView = (TextView) findViewById(R.id.custom_loading_dialog_text_view);

        /*Random r = new Random();
        int randomTxt = r.nextInt(loadingSentences.length);
        randomTxt -= 1;

        String loadingText = loadingSentences[randomTxt];
        textView.setText(loadingText);*/
    }
}
