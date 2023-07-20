package com.tg.derdoapp;

import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.snackbar.Snackbar;
import com.tg.VO.MatchUserVO;
import com.tg.VO.ProfileInfoVO;
import com.tg.VO.SendInteractionVO;
import com.tg.dataManager.MatchUsersDataManager;
import com.tg.globals.AppGlobals;
import com.tg.dataManager.ExceptionManager;
import com.tg.dataManager.StorageManager;
import com.tg.dataObject.NavActivity;
import com.tg.helper.CustomDialog;
import com.tg.helper.CustomLoadingDialog;
import com.tg.helper.FileHelper;
import com.tg.helper.IDialogFunctionPasser;
import com.tg.helper.PhotoUploadHelper;
import com.tg.helper.RequestHelper;
import com.tg.imageManager.ImageLoader;
import com.tg.requestManager.HttpManager;
import com.tg.requestManager.HttpMethods;
import com.tg.requestManager.RequestPackage;
import com.tg.requestManager.ServiceResponseVO;

import android.app.AlertDialog;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

public class ProfileExtActivity extends NavigationBaseActivity {

    public static final int PICK_IMAGE_RESULT = 1;
    public static final int EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 1001;

    private Button profileContinueButton = null;

    public ProfileExtActivity() {
        super(NavActivity.PROFILE_PHOTO, R.layout.activity_profile_ext);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ProfileInfoVO profileInfoVO = null;
        try {
           profileInfoVO = fetchUserProfileData();
        }
        catch (Exception e) {
            Log.e("fetchUserProfileData", e.getMessage());
            ExceptionManager.LogExceptionStackTrace("fetchUserProfileDa.st", e);
            ShowErrorActivity();
            return;
        }

        final android.widget.TextView profileShortNameAndTitle = findViewById(R.id.input_name_title_textView);
        profileShortNameAndTitle.setText(profileInfoVO.userName);

        final android.widget.TextView profileBirthDate = findViewById(R.id.input_birthdate_textView);
        profileBirthDate.setText(profileInfoVO.birthDate);

        final android.widget.TextView profileGenderTextView = findViewById(R.id.input_gender_textView);
        profileGenderTextView.setText(profileInfoVO.gender.equals("female") ? "Kadın" : "Erkek");

        final android.widget.TextView profileEmailTextView = findViewById(R.id.input_email_textView);
        profileEmailTextView.setText(profileInfoVO.email);

        ImageButton notificationSettingsButton = (ImageButton) findViewById(R.id.notifications_settings_switch_ib);
        CheckNSetSwitch(notificationSettingsButton, profileInfoVO.notificationsEnabled);//First Time Setup
        notificationSettingsButton.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                try {
                    NotificationsSettingAction((ImageButton) v);
                }
                catch (Exception ex) {
                    ShowErrorToast(ex);
                }
            }
        });

        ImageButton privateModeSettinsButton = (ImageButton) findViewById(R.id.private_mode_settings_switch_ib);
        CheckNSetSwitch(privateModeSettinsButton, profileInfoVO.isPrivateModeEnabled);//First Time Setup
        privateModeSettinsButton.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                try {
                    PrivateModeSettingAction((ImageButton) v);
                }
                catch (Exception ex) {
                    ShowErrorToast(ex);
                }
            }
        });

        final android.widget.ImageButton selectProfilePhotoButton = findViewById(R.id.profileImageViewButton);

        final android.widget.LinearLayout selectProfilePhoto = findViewById(R.id.profile_change_photo);
        selectProfilePhoto.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                SelectProfilePicture();
            }
        });

        final android.widget.LinearLayout deleteProfilePhoto = findViewById(R.id.profile_delete_photo);
        deleteProfilePhoto.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                //SelectProfilePicture();

                try {
                    RequestHelper rh = new RequestHelper(getApplicationContext());
                    ServiceResponseVO resultVO = rh.sendRequest("/profile/deleteprofileimage", null, HttpMethods.POST);
                    if (!resultVO.success) {
                        ShowErrorToast();
                    }
                    else {
                        android.widget.ImageButton imageView = findViewById(R.id.profileImageViewButton);
                        imageView.setImageResource(R.drawable.profile_icon_test);
                    }
                }
                catch (Exception e) {
                    Log.w("deleteProfilePhoto", e.getMessage() != null ? e.getMessage() : "MESSAGE NULL");
                }
            }
        });

        //TODO : Profile Picture ACTIONS
        if (profileInfoVO.profilePictureUrl != null && profileInfoVO.profilePictureUrl != "") {

            try {

                String imageUrl = generateFetchImageUrl(profileInfoVO.profilePictureUrl);

                ImageLoader imgLoader = new ImageLoader(getApplicationContext());
                imgLoader.DisplayImage(imageUrl, IMAGE_LOADER, selectProfilePhotoButton);
            }
            catch (Exception ex) {
                if(ex != null) {

                    if(ex.getMessage() != null) {
                        Log.w("profilePictureUrl", ex.getMessage());
                    }

                    if(ex.getStackTrace() != null) {
                        ExceptionManager.LogExceptionStackTrace("AMK", ex);
                    }
                }

                ShowErrorToast("Profil Fotoğrafı Yüklenirken Bir Hata Oluştu");
            }
        }

        final android.widget.LinearLayout logoutButton = findViewById(R.id.logout_layout);
        logoutButton.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                generateCustomDialog(getResources().getString(R.string.profile_ext_logout_warning), R.drawable.warning, true, "Evet",true, true, 0, new LogoutFunctionPasser()).show();
            }
        });

        final android.widget.LinearLayout deleteAccount = findViewById(R.id.profile_ext_delete_acc_buttn);
        deleteAccount.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                generateCustomDialog(getResources().getString(R.string.profile_ext_delete_acc_warning), R.drawable.warning, true, "Evet",true, true,0, new DeleteAccountFunctionPasser()).show();
            }
        });

        Intent intent = getIntent();
        if(intent.hasExtra(PhotoUploadHelper.PHOTO_ERROR_KEY)) {
            ShowErrorToast();
        }
    }

    private CustomDialog generateCustomDialog(String text, Integer iconResource,Boolean hasOkButton, String okButtonText, Boolean hasNokButton, Boolean isFatal, long showSeconds, IDialogFunctionPasser functionPasser) {
        return new CustomDialog(this, text, iconResource, hasOkButton, okButtonText, hasNokButton, isFatal, showSeconds, functionPasser);
    }

    private ProfileInfoVO fetchUserProfileData() throws Exception {

        RequestHelper rh = new RequestHelper(getApplicationContext());
        ProfileInfoVO resultVO = rh.sendRequest("/profile/getinfo", null, HttpMethods.GET, ProfileInfoVO.class);

        return resultVO;
    }

    private Boolean changeNotificationsMode() throws Exception {

        RequestHelper rh = new RequestHelper(getApplicationContext());
        Boolean resultVO = rh.sendRequest("/profile/changenotificationsmode", null, HttpMethods.POST, Boolean.class);

        return resultVO;
    }

    private Boolean changePrivateMode() throws Exception {

        MatchUsersDataManager.Instance().resetList();

        RequestHelper rh = new RequestHelper(getApplicationContext());
        Boolean resultVO = rh.sendRequest("/profile/changeprivatemode", null, HttpMethods.POST, Boolean.class);

        return resultVO;
    }

    private void CheckNSetSwitch(ImageButton ib, Boolean isActive) {
        if(isActive) {
            ib.setImageResource(R.drawable.switch_on);
        }
        else {
            ib.setImageResource(R.drawable.switch_off);
        }
    }

    private void NotificationsSettingAction(ImageButton ib) throws Exception {
        Boolean changeTo = changeNotificationsMode();
        CheckNSetSwitch(ib, changeTo);
    }

    private void PrivateModeSettingAction(ImageButton ib) throws Exception {
        Boolean changeTo = changePrivateMode();
        CheckNSetSwitch(ib, changeTo);
    }

    private void Logout() {
        StorageManager.DeleteUserAuthToken(this);
        StorageManager.DeleteUserAccessToken(this);

        RedirectTo(MainActivity.class);
    }

    private void DeleteAccount() {

        try {
            RequestHelper rh = new RequestHelper(getApplicationContext());
            Boolean resultDeleteAccount = rh.sendRequest("/profile/deleteaccount", null, HttpMethods.POST, Boolean.class);
            if (!resultDeleteAccount) {
                ShowErrorToast();
                return;
            }
        }
        catch (Exception ex) {
            ShowErrorToast(ex);
            return;
        }

        StorageManager.DeleteUserAuthToken(this);
        StorageManager.DeleteUserAccessToken(this);

        RedirectTo(MainActivity.class);
    }

    private void showLoadingDialog() {
        this.customLoadingDialog = new CustomLoadingDialog(this);
        this.customLoadingDialog.show();
    }

    private void dismissLoadingDialog() {
        if(this.customLoadingDialog != null) {
            this.customLoadingDialog.dismiss();
        }
    }

    private void ProfilePictureQuestionDialog(final Uri fileUri) {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:

                        //RedirectToPhotoUpload(fileUri);

                        new UploaderTaskOnUI().execute(fileUri);

                        /*ServiceResponseVO responseVO = UploadProfilePicture(fileUri);
                        if(!responseVO.success) {
                            Log.d("UPP.Res", responseVO.success.toString());
                            ShowErrorToast();
                        }*/

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        SelectProfilePicture();
                        break;
                }
            }
        };

        String messageText = getResources().getString(R.string.gallery_choose_picture_dialog_box_message);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(messageText).setPositiveButton("Evet", dialogClickListener).setNegativeButton("Hayır", dialogClickListener).show();
    }

    private void RedirectToPhotoUpload(Uri profilePictureUri) {

        Bundle b = new Bundle();
        AppGlobals.uploadPhotoUri = profilePictureUri;
        //b.putParcelable(PhotoUploadHelper.PHOTO_PARCABLE_KEY, profilePictureUri);
        b.putString(PhotoUploadHelper.PHOTO_BACK_TO, ProfileExtActivity.class.getName());
        b.putString(PhotoUploadHelper.PHOTO_GO_TO, ProfileExtActivity.class.getName());

        RedirectTo(PhotoUploadActivity.class, b);
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

                    ProfilePictureQuestionDialog(selectedImage);

                    //TODO : REMOVE
                    if(profileContinueButton != null) {
                        profileContinueButton.setEnabled(true);
                    }

                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
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

    class LogoutFunctionPasser implements IDialogFunctionPasser {

        @Override
        public void passOK() {
            Logout();
        }

        @Override
        public void passCancel() {

        }

    }

    //TODO :
    class DeleteAccountFunctionPasser implements IDialogFunctionPasser {

        @Override
        public void passOK() {
            DeleteAccount();
        }

        @Override
        public void passCancel() {

        }

    }

}
