package com.tg.derdoapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.tg.VO.HandShakeVO;
import com.tg.globals.AppGlobals;
import com.tg.dataManager.ExceptionManager;
import com.tg.dataManager.StorageManager;
import com.tg.helper.HandshakeHelper;

public class MainActivity extends BaseActivity {

    private static final int PERMISSION_REQUEST_CODE = 1001;

    public MainActivity() {
        super(R.layout.activity_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //testSockect();
        RedirectToRelatedActivity();
    }

    private Boolean PermissionsGranted() {
        if (androidx.core.app.ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            //File write logic here
            return true;
        }

        return false;
    }

    private void RedirectToRelatedActivity() {

        Boolean isGranted = PermissionsGranted();
        if(!isGranted) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            return;
        }

        Thread welcomeThread = new Thread() {
            @Override
            public void run() {
                try {

                    //android.content.Intent redirectPage = null;
                    Class<? extends AppCompatActivity> redirectToClass = null;
                    String authToken = StorageManager.GetUserAuthToken(getBaseContext());
                    AppGlobals.hasLoginData = false;
                    if(authToken != null && authToken != "") {
                        try {
                            HandShakeVO handshakeVO = HandshakeHelper.doHandshake(authToken, getBaseContext());

                            AppGlobals.hasLoginData = true;
                            AppGlobals.hasProfileData = handshakeVO.isRegisteredUser == null ? false : handshakeVO.isRegisteredUser;
                            AppGlobals.hasProfilePhoto = handshakeVO.hasProfileImage == null ? false : handshakeVO.hasProfileImage;
                        }
                        catch (Exception e) {

                            AppGlobals.hasLoginData = false;

                            if(e.getMessage() != null && e.getStackTrace() != null) {
                                Log.e("MainActivity.e", e.getMessage());
                                ExceptionManager.LogExceptionStackTrace("MainActivity.st", e);
                            }
                            else {
                                Log.e("FacebookDataManager.e", "messsage was null");
                            }

                            //Toast.makeText(getApplicationContext(), "Bir Hata Oluştu", Toast.LENGTH_LONG).show();
                        }
                    }

                    if(!AppGlobals.hasLoginData) {
                        redirectToClass = LoginActivity.class;
                    }
                    else if(!AppGlobals.hasProfileData) {
                        redirectToClass = ProfileNameActivity.class;
                    }
                    /*else if(!AppGlobals.hasProfilePhoto) {
                        redirectToClass = ProfilePhotoActivity.class;
                    }*/
                    else if(!AppGlobals.hasLocationInfo) {
                        redirectToClass = LocationInfoActivity.class;
                    }
                    else {
                        //TODO : FETCH MESSAGES BEFORE SHOWCASE ONCE!
                        redirectToClass = ShowCaseActivity.class;
                    }

                    if(redirectToClass != null) {
                        RedirectTo(redirectToClass);
                    }

                } catch (Exception e) {
                    ShowErrorToast(e);
                } finally {

                }
            }
        };

        welcomeThread.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    RedirectToRelatedActivity();
                }

                break;
            }
        }
    }
}
