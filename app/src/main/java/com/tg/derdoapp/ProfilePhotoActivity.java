package com.tg.derdoapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tg.dataManager.ExceptionManager;
import com.tg.dataObject.NavActivity;
import com.tg.dataObject.UserProfileDataObject;
import com.tg.globals.AppGlobals;
import com.tg.helper.FileHelper;
import com.tg.helper.PhotoUploadHelper;
import com.tg.requestManager.HttpManager;
import com.tg.requestManager.ServiceResponseVO;

import java.io.File;

public class ProfilePhotoActivity extends BaseActivity {

    public static final int PICK_IMAGE_RESULT = 1;
    public static final int EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 1001;

    private Button continueButton = null;

    private Uri selectedImageUri;

    //SaveNRedirect saveNRedirectTask;

    public ProfilePhotoActivity() {
        super(R.layout.activity_profile_photo);
    }

    /*@Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel running task(s) to avoid memory leaks
        if (saveNRedirectTask != null)
            saveNRedirectTask.cancel(true);
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        continueButton = findViewById(R.id.photo_ok_button);
        continueButton.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                //SaveAndRedirectToPhotoUpload();

                //SaveAndRedirect();

                RedirectToPhotoUpload(selectedImageUri);

                /*if (saveNRedirectTask != null) {
                    saveNRedirectTask.cancel(true);
                }

                saveNRedirectTask = new SaveNRedirect();
                saveNRedirectTask.execute(selectedImageUri);*/
            }
        });

        //continueButton.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners_disabled));
        //continueButton.setEnabled(false);

        /*final android.widget.ImageButton selectProfilePhotoButton = findViewById(R.id.profileImageViewButton);
        selectProfilePhotoButton.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                SelectProfilePicture();
            }
        });

        final android.widget.ImageButton profileImagePhotoIcon = findViewById(R.id.profileImagePhotoIcon);
        profileImagePhotoIcon.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                SelectProfilePicture();
            }
        });*/

        final android.widget.Button profilePhotoSelectButton = findViewById(R.id.photo_nok_button);
        profilePhotoSelectButton.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                SelectProfilePicture();
            }
        });

        Intent intent = getIntent();
        if(intent.hasExtra(PhotoUploadHelper.PHOTO_ERROR_KEY)) {
            ShowErrorToast();
        }
    }

    private Boolean ExternalStoragePermissionsGranted() {
        if (androidx.core.app.ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            //File write logic here
            return true;
        }

        return false;
    }

    protected void SelectProfilePicture() {
        Boolean isGranted = ExternalStoragePermissionsGranted();
        if(!isGranted) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
            return;
        }

        android.content.Intent intent = new android.content.Intent();
        intent.setType("image/*");
        intent.setAction(android.content.Intent.ACTION_GET_CONTENT);
        startActivityForResult(android.content.Intent.createChooser(intent, "Fotoğraf Seçin"), PICK_IMAGE_RESULT);
    }

    private void SaveAndRedirect() {
        Log.d("SNRR", "INIT");

        ServiceResponseVO responseVO = UploadProfilePicture(selectedImageUri);
        if (!responseVO.success) {
            Log.d("UPP.Res", responseVO.success.toString());
            ShowErrorToast();
        }

        //RedirectToShowCase();
        RedirectToSettings();
    }

    private void SaveAndRedirectToShowcase(Uri selectedImageUri) {

        Log.d("SNRR", "INIT");

        ServiceResponseVO responseVO = UploadProfilePicture(selectedImageUri);
        if (!responseVO.success) {
            Log.d("UPP.Res", responseVO.success.toString());
            ShowErrorToast();
        }

        hideLoadingPanel();

        //RedirectToShowCase();
    }

    private void RedirectToPhotoUpload(Uri profilePictureUri) {

        if(profilePictureUri != null) {
            Bundle b = new Bundle();
            AppGlobals.uploadPhotoUri = profilePictureUri;
            //b.putParcelable(PhotoUploadHelper.PHOTO_PARCABLE_KEY, profilePictureUri);
            b.putString(PhotoUploadHelper.PHOTO_BACK_TO, ProfilePhotoActivity.class.getName());
            b.putString(PhotoUploadHelper.PHOTO_GO_TO, SettingsActivity.class.getName());

            RedirectTo(PhotoUploadActivity.class, b);
        }
        else {
            RedirectTo(SettingsActivity.class);
        }
    }

    private void RedirectToSettings() {
        RedirectTo(SettingsActivity.class);
    }

    private void RedirectToShowCase() {
        RedirectTo(ShowCaseActivity.class);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case PICK_IMAGE_RESULT:

                    //data.getData returns the content URI for the selected Image
                    Uri selectedImage = data.getData();

                    android.widget.ImageButton imageView = findViewById(R.id.profileImageViewButton);
                    imageView.setImageURI(selectedImage);

                    selectedImageUri = selectedImage;
                    //continueButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg_rounded_corners));
                    continueButton.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners));
                    continueButton.setEnabled(true);

                    //ProfilePictureQuestionDialog(selectedImage);
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SelectProfilePicture();
                }

                break;
            }
        }
    }

    private final class SaveNRedirect extends AsyncTask<Uri, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoadingPanel();
        }

        @Override
        protected String doInBackground(Uri... params) {
            try {
                SaveAndRedirectToShowcase(params[0]);
            } catch (Exception e) {
                Log.e("doInBackground", e.getMessage() != null ? e.getMessage() : "MESSAGE_WAS_NULL");
                ShowErrorToast();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            //RedirectToShowCase();
            RedirectToSettings();
        }
    }
}
