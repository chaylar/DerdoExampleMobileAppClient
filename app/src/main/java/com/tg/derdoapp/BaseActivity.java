package com.tg.derdoapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tg.dataManager.ExceptionManager;
import com.tg.dataObject.ErrorDataObject;
import com.tg.dataObject.NavActivity;
import com.tg.globals.AppGlobals;
import com.tg.helper.CustomDialog;
import com.tg.helper.CustomLoadingDialog;
import com.tg.helper.FileHelper;
import com.tg.helper.RequestHelper;
import com.tg.imageManager.ImageLoader;
import com.tg.requestManager.HttpManager;
import com.tg.requestManager.HttpMethods;
import com.tg.requestManager.RequestPackage;
import com.tg.requestManager.ServiceResponseVO;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class BaseActivity extends AppCompatActivity {

    protected final int IMAGE_LOADER = R.drawable.loading1;
    private int relatedViewId;

    private static CountDownTimer resetTimer = null;

    public BaseActivity() {
    }

    public BaseActivity(int viewId) {
        relatedViewId = viewId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(relatedViewId);
    }

    //NOTE : DISABLE BACK BUTTON ON ALL ACTIVITIES
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    //TODO : LOADING!!!
    public void doLoadingAction() {
        //View contentView = findViewById(android.R.id.content);
        //contentView.setVisibility(View.INVISIBLE);

        //LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //View loadingView = layoutInflater.inflate(R.layout.helper_page_loader_layout, null, false);
        //loadingView.bringToFront();

        new CustomLoadingDialog(this).show();
    }

    public void doUnLoadingAction() {
        //View contentView = findViewById(android.R.id.content);
        //contentView.setVisibility(View.VISIBLE);
    }

    public class LoadingInitializer extends AsyncTask<String, Void, Object> {

        @Override
        protected Void doInBackground(String... params) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.i("LoadingInitializer", "Init");
                        doLoadingAction();
                    }
                    catch (Exception ex) {
                        Log.w("LoadingInitializer.EX", ex.getMessage());
                    }
                }
            });

            return null;
        }
    }

    public class LoadingInitializerUnload extends AsyncTask<String, Void, Object> {

        @Override
        protected Void doInBackground(String... params) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.i("LoadingInitializer", "Init");
                        doUnLoadingAction();
                    }
                    catch (Exception ex) {
                        Log.w("LoadingInitializer.EX", ex.getMessage());
                    }
                }
            });

            return null;
        }
    }

    protected void ShowErrorActivity(String errorText) {

        if(errorText != null) {
            ErrorDataObject.errorText = errorText;
        }

        ShowErrorActivity();
    }

    protected String generateFetchImageUrl(String imageName) {

        return imageName;
        /*String appServerAddress = getResources().getString(R.string.app_server_address);
        String imageUrl = appServerAddress + "/profile/image?name=" + imageName;

        return imageUrl;*/
    }

    public void displayImageOnView(String baseImageUrl, ImageView profileImageView) {

        if(baseImageUrl == null || baseImageUrl.equals("")) {
            profileImageView.setImageResource(R.drawable.profile_icon_test);
        }
        else {
            String imageUrl = generateFetchImageUrl(baseImageUrl);
            ImageLoader imgLoader = new ImageLoader(getApplicationContext());
            imgLoader.DisplayImage(imageUrl, IMAGE_LOADER, profileImageView);
        }

    }

    protected void ShowErrorActivity() {
        hideLoadingPanel();
        RedirectTo(ErrorActivity.class);
    }

    public void ShowErrorToast(Exception ex) {
        Log.e("ERROR_ON_TOAST", ex.getMessage() != null ? ex.getMessage() : "MESSAGE_WAS_NULL");
        ShowErrorToast();
    }

    public void ShowErrorToast() {
        hideLoadingPanel();
        Toast.makeText(getApplicationContext(), "Bir Hata Oluştu", Toast.LENGTH_LONG).show();
    }

    protected void ShowErrorToast(String errorText) {
        Toast.makeText(this.getBaseContext(), errorText, Toast.LENGTH_LONG);
    }

    public void RedirectToSubscription() {
        Toast.makeText(this, "SUBSCRIPTION REQUIRED", Toast.LENGTH_LONG).show();
    }

    public void setGreetingsCount(int greetingsCount) {
        final TextView remainingGreetingsTextView = findViewById(R.id.showcase_daily_remaining);
        if(remainingGreetingsTextView != null) {
            AppGlobals.greetingsCount = greetingsCount;
            remainingGreetingsTextView.setText(Integer.toString(greetingsCount));
        }
    }

    protected void fetchResetTimerValueAndSet() {
        try {
            RequestHelper rh = new RequestHelper(getApplicationContext());
            ServiceResponseVO resultVO = rh.sendRequest("/match/getresettimervalue", null, HttpMethods.GET);
            long seconds = ((Number) resultVO.data).longValue();

            setCountdownToReset(seconds);
        }
        catch (Exception ex) {
            Log.w("resetTimer", ex.getMessage() != null ? ex.getMessage() : "MESSAGE NULL");
        }
    }

    protected void setCountdownToReset(long secondsToReset)
    {
        AppGlobals.secondsLeftToReset = secondsToReset;

        if(resetTimer != null) {
            resetTimer.cancel();
        }

        resetTimer = new CountDownTimer((secondsToReset * 1000), 1000) {
            public void onTick(long millisUntilFinished) {
                AppGlobals.secondsLeftToReset--;
                Log.i("resetTimer.time", String.valueOf(AppGlobals.secondsLeftToReset));
                TextView resetTimerTextView = findViewById(R.id.showcase_daily_remaining_reset_timer);
                if (resetTimerTextView != null) {
                    String formatted = String.format("(%02d:%02d:%02d)", TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % TimeUnit.HOURS.toMinutes(1),
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % TimeUnit.MINUTES.toSeconds(1));

                    Log.i("resetTimer.formatted", formatted);
                    resetTimerTextView.setText(formatted);
                }
            }

            public void onFinish() {
                fetchResetTimerValueAndSet();
            }

        }.start();
    }

    protected void setGreetingsCountFromAppGlobals() {
        setGreetingsCount(AppGlobals.greetingsCount);
    }

    protected void showLoadingPanel() {
        Log.w("loadingPanel", "showLoadingPanel");
        RelativeLayout loadingPanel = findViewById(R.id.loadingPanel);
        if(loadingPanel != null) {
            Log.w("loadingPanel", "showLoadingPanelNN");
            loadingPanel.setVisibility(View.VISIBLE);

            if(!loadingPanel.hasOnClickListeners()) {
                loadingPanel.setOnClickListener(new android.view.View.OnClickListener() {
                    public void onClick(android.view.View v) {
                    }
                });
            }
        }
    }

    protected void hideLoadingPanel() {
        Log.w("loadingPanel", "hideLoadingPanel");
        RelativeLayout loadingPanel = findViewById(R.id.loadingPanel);
        if(loadingPanel != null) {
            Log.w("loadingPanel", "hideLoadingPanelNN");
            loadingPanel.setVisibility(View.GONE);
        }
    }

    protected void redirectToWithAnimation(Class<? extends AppCompatActivity> kClass, TransitionAnimationsHolder transitionHolder) {
        if(transitionHolder == null) {
            RedirectTo(kClass);
        }
        else {
            RedirectTo(kClass, transitionHolder.animationIn, transitionHolder.animationOut);
        }
    }

    private void RedirectTo(Class<? extends AppCompatActivity> kClass, int animationIn, int animationOut) {

        if(kClass == ShowCaseActivity.class && !AppGlobals.hasLocationInfo) {
            android.content.Intent redirect = new android.content.Intent(getBaseContext(), LocationInfoActivity.class);
            startActivity(redirect);
            return;
        }

        android.content.Intent redirect = new android.content.Intent(getBaseContext(), kClass);
        startActivity(redirect);
        overridePendingTransition(animationIn, animationOut);
    }

    protected void RedirectTo(Class<? extends AppCompatActivity> kClass, Bundle b) {

        if(kClass == ShowCaseActivity.class && !AppGlobals.hasLocationInfo) {
            android.content.Intent redirect = new android.content.Intent(getBaseContext(), LocationInfoActivity.class);
            redirect.putExtras(b);
            startActivity(redirect);
            return;
        }

        android.content.Intent redirect = new android.content.Intent(getBaseContext(), kClass);
        redirect.putExtras(b);
        startActivity(redirect);
    }

    protected void RedirectTo(Class<? extends AppCompatActivity> kClass) {

        if(kClass == ShowCaseActivity.class && !AppGlobals.hasLocationInfo) {
            android.content.Intent redirect = new android.content.Intent(getBaseContext(), LocationInfoActivity.class);
            startActivity(redirect);
            return;
        }

        android.content.Intent redirect = new android.content.Intent(getBaseContext(), kClass);
        startActivity(redirect);
    }

    protected TransitionAnimationsHolder getTransitionAnimations(int activeTag, int onClickTag) {

        TransitionAnimationsHolder result = null;

        if(activeTag == 0 || onClickTag == 0) {
            result = null;
        }
        else if(onClickTag == 10) {
            result = new TransitionAnimationsHolder(R.anim.transition_up_to_down, R.anim.transition_up_to_down_out);
        }
        else if(activeTag < onClickTag) {
            result = new TransitionAnimationsHolder(R.anim.transition_right_to_left, R.anim.transition_right_to_left_out);
        }
        else if(activeTag > onClickTag) {
            result = new TransitionAnimationsHolder(R.anim.transition_left_to_right, R.anim.transition_left_to_right_out);
        }

        return result;
    }

    protected int getNavButtonTag(View navButton) {
        try {
            if(navButton.getTag() != null) {
                return Integer.parseInt(navButton.getTag().toString());
            }
        }
        catch (Exception ex) {
            //
        }

        return 0;
    }

    protected class TransitionAnimationsHolder {

        public TransitionAnimationsHolder(int animIn, int animOut) {
            this.animationIn = animIn;
            this.animationOut = animOut;
        }

        public int animationIn;

        public int animationOut;

    }

    protected void showWelcomeDialog() {
        if(AppGlobals.showWelcomeSnack) {
            new CustomDialog(this, getResources().getString(R.string.general_dialog_delete_welcome), R.drawable.welcome, false, null, false, false,5, null).show();
            AppGlobals.showWelcomeSnack = false;
        }
    }

    protected ServiceResponseVO UploadProfilePicture(Uri profilePictureUri) {

        Log.d("UploadFilesTask", profilePictureUri.getPath());

        //Uri filePathFromActivity = Uri.parse(FileHelper.getRealPathFromUri(this, profilePictureUri));
        Uri filePathFromActivity = Uri.parse(FileHelper.getPathFromURI(this, profilePictureUri));
        File file = new File(filePathFromActivity.getPath());
        HttpManager httpManager = new HttpManager(this.getApplicationContext());
        ServiceResponseVO responseVO = null;

        Log.d("Uploader.file.name", file.getName());
        Log.d("Uploader.file.length", String.valueOf(file.length()));

        try {
            responseVO = httpManager.uploadFileSync(file);
            Log.d("Uploader.res", String.valueOf(responseVO.success));
        }
        catch (Exception e) {
            Log.e("Uploader", e.getMessage() != null ? e.getMessage() : "MESSAGE_NULL");
            ExceptionManager.LogExceptionStackTrace("UploadPP", e);
            ShowErrorActivity();
        }

        Log.d("Uploader.rvo", responseVO != null ? String.valueOf(responseVO.success) : "NULL");

        return responseVO;
    }

    protected class UploaderTaskOnUI extends AsyncTask<Uri, Void, ServiceResponseVO> {

        @Override
        protected void onPreExecute() {
            showLoadingPanel();
        }

        @Override
        protected void onPostExecute(ServiceResponseVO result) {
            hideLoadingPanel();
        }

        @Override
        protected ServiceResponseVO doInBackground(Uri... uris) {
            try {
                return UploadProfilePicture(uris[0]);
            }
            catch (Exception ex) {
                Log.w("Uploader.EX : ", ex.getMessage());
            }

            return null;
        }
    }
}
