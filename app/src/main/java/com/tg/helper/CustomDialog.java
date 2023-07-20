package com.tg.helper;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.tg.derdoapp.R;
import com.tg.globals.AppGlobals;

import java.util.concurrent.TimeUnit;

public class CustomDialog extends Dialog {

    private Button okButton = null;
    private Button nOkButton = null;
    private String textViewText;
    private Boolean hasOkButtton = true;
    private Boolean hasNokButton = false;
    private long showSeconds = 0;
    private Integer iconResourceId = null;
    private Activity activity;
    private String okButtonText = null;
    private Boolean isFatalWarning = false;

    private IDialogFunctionPasser functionPasser = null;

    private static CountDownTimer endTimer = null;

    public CustomDialog(Activity act, String text, Integer iconResource,Boolean hasOkButton, String okButtonTText, Boolean hasNokButton, Boolean isFatalWarning, long showSeconds, IDialogFunctionPasser functionPasser) {
        super(act);
        this.activity = act;
        this.textViewText = text;
        this.hasOkButtton = hasOkButton;
        this.showSeconds = showSeconds;
        this.iconResourceId = iconResource;
        this.hasNokButton = hasNokButton;
        this.functionPasser = functionPasser;
        this.okButtonText = okButtonTText;
        this.isFatalWarning = isFatalWarning;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog);

        TextView textView = (TextView) findViewById(R.id.custom_dialog_text_view);
        textView.setText(textViewText);

        okButton = (Button) findViewById(R.id.custom_dialog_ok);
        if(hasOkButtton) {

            if(okButtonText != null && !okButtonText.equals("")) {
                okButton.setText(okButtonText);
            }

            if(isFatalWarning) {
                okButton.setTextColor(Color.parseColor("#8b0000"));
            }

            okButton.setOnClickListener(new android.view.View.OnClickListener() {
                public void onClick(android.view.View v) {

                    if(functionPasser != null) {
                        functionPasser.passOK();
                    }

                    dismiss();
                }
            });
        }
        else {
            okButton.setVisibility(View.GONE);
        }

        nOkButton = (Button) findViewById(R.id.custom_dialog_nok);
        if(hasNokButton) {

            if(isFatalWarning) {
                nOkButton.setTextColor(Color.parseColor("#06a94d"));
            }

            nOkButton.setOnClickListener(new android.view.View.OnClickListener() {
                public void onClick(android.view.View v) {

                    if(functionPasser != null) {
                        functionPasser.passCancel();
                    }

                    dismiss();
                }
            });
        }
        else {
            nOkButton.setVisibility(View.GONE);
        }

        ImageView icon = (ImageView)findViewById(R.id.custom_dialog_image_view);
        if(iconResourceId != null) {
            icon.setImageDrawable(activity.getResources().getDrawable(iconResourceId));
        }
        else {
            icon.setVisibility(View.GONE);
        }


        if(showSeconds > 0) {
            setCountdownToDismiss(showSeconds);
        }
    }

    protected void setCountdownToDismiss(long secondsToReset)
    {
        if(endTimer != null) {
            endTimer.cancel();
        }

        endTimer = new CountDownTimer((secondsToReset * 1000), 1000) {
            public void onTick(long millisUntilFinished) {
                Log.i("DISMISS_TIMER", "TICK");
            }

            public void onFinish() {
                if(endTimer != null) {
                    endTimer.cancel();
                }

                dismiss();
            }

        }.start();
    }
}
