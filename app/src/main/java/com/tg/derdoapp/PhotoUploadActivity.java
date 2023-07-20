package com.tg.derdoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tg.dataManager.ExceptionManager;
import com.tg.globals.AppGlobals;
import com.tg.helper.FileHelper;
import com.tg.helper.PhotoUploadHelper;
import com.tg.requestManager.HttpManager;
import com.tg.requestManager.ServiceResponseVO;

import java.io.File;
import java.net.URL;

public class PhotoUploadActivity extends BaseActivity {

    public PhotoUploadActivity() {
        super(R.layout.activity_photo_upload);
    }

    private Uri uploadPictureUri;
    private Class goTo;
    private Class backTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("UploadFilesTask", "onCreate");
        Log.d("UploadFilesTask", AppGlobals.uploadPhotoUri.getPath());

        Intent intent = getIntent();
        uploadPictureUri = AppGlobals.uploadPhotoUri;

        String goToClassName = intent.getStringExtra(PhotoUploadHelper.PHOTO_GO_TO);
        goTo = getClassFromString(goToClassName);

        String backToClassName = intent.getStringExtra(PhotoUploadHelper.PHOTO_BACK_TO);
        backTo = getClassFromString(backToClassName);

        new UploadFilesTask().execute(uploadPictureUri);
    }

    private Class getClassFromString(String className) {
        try {
            Class<?> act = Class.forName(className);
            return act;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    private ServiceResponseVO SaveAndRedirect(Uri uploadPictureUri) {
        Log.d("UploadFilesTask", uploadPictureUri.getPath());
        Log.d("UploadFilesTask", "SaveAndRedirect");
        ServiceResponseVO responseVO = UploadProfilePicture(uploadPictureUri);
        if (!responseVO.success) {
            ShowErrorToast();
        }

        return responseVO;
    }

    private class UploadFilesTask extends AsyncTask<Uri, Integer, ServiceResponseVO> {

        @Override
        protected ServiceResponseVO doInBackground(Uri... params) {
            return SaveAndRedirect(params[0]);
        }

        @Override
        protected void onPostExecute(ServiceResponseVO result) {
            if(!result.success) {
                ShowErrorToast();
                Bundle b = new Bundle();
                b.putString(PhotoUploadHelper.PHOTO_ERROR_KEY, "PHOTO_UPLOAD_ERROR");
                RedirectTo(backTo, b);
                return;
            }

            RedirectTo(goTo);
        }
    }
}
