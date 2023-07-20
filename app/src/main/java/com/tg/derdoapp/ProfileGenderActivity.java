package com.tg.derdoapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tg.VO.MatchUserVO;
import com.tg.VO.SendInteractionVO;
import com.tg.dataObject.NavActivity;
import com.tg.globals.AppGlobals;
import com.tg.dataManager.GenderDataManager;
import com.tg.dataObject.UserProfileDataObject;
import com.tg.helper.RequestHelper;
import com.tg.requestManager.HttpManager;
import com.tg.requestManager.HttpMethods;
import com.tg.requestManager.RequestPackage;
import com.tg.requestManager.ServiceResponseVO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProfileGenderActivity extends BaseActivity {

    private Button continueButton = null;

    private List<Button> genderButtons = null;

    public ProfileGenderActivity() {
        super(R.layout.activity_profile_gender);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        genderButtons = new ArrayList<>();

        continueButton = findViewById(R.id.profileGenderOK);
        continueButton.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {

                try {
                    ServiceResponseVO responseVO = saveSignupInfo(UserProfileDataObject.userName, UserProfileDataObject.userBirthDate, UserProfileDataObject.userGenderName);
                    if(responseVO.success) {
                        RedirectToProfilePhotoActivity();
                    }
                }
                catch (Exception e) {
                    Log.w("saveSignupInfo", e.getMessage());
                    Log.w("saveSignupInfo.st", e.getStackTrace().toString());

                    Toast.makeText(getApplicationContext(), "Bir Hata Oluştu", Toast.LENGTH_LONG).show();
                }
            }
        });

        continueButton.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners_disabled));
        continueButton.setEnabled(false);

        final android.widget.Button femaleButton = findViewById(R.id.profileGenderFemale);
        femaleButton.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                onGenderSelect(v, 1);
            }
        });

        final android.widget.Button maleButton = findViewById(R.id.profileGenderMale);
        maleButton.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                onGenderSelect(v, 2);
            }
        });

        genderButtons.add(femaleButton);
        genderButtons.add(maleButton);

        setUncheckedGenderButton(femaleButton);
        setUncheckedGenderButton(maleButton);

        if(AppGlobals.facebookUserData != null && AppGlobals.facebookUserData.gender != null && AppGlobals.facebookUserData.gender != "") {
            Log.w("FB_GEN", AppGlobals.facebookUserData.gender);
            if (AppGlobals.facebookUserData.gender.contains("female")) {
                femaleButton.performClick();
            } else if (AppGlobals.facebookUserData.gender.contains("male")) {
                maleButton.performClick();
            }
        }
    }

    private void EnableContinueButton() {

        if(continueButton == null) {
            continueButton = findViewById(R.id.profileGenderOK);
        }

        continueButton.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners));
        continueButton.setEnabled(true);
    }

    public void setUncheckedGenderButton(final Button b) {
        Drawable img = getResources().getDrawable(	android.R.drawable.checkbox_off_background);
        img.setBounds(0, 0, 32, 32);
        b.setCompoundDrawables(img, null, null, null);
    }

    public void setCheckedGenderButton(final Button b) {
        Drawable img = getResources().getDrawable(	android.R.drawable.checkbox_on_background);
        img.setBounds(0, 0, 32, 32);
        b.setCompoundDrawables(img, null, null, null);
    }

    private void onGenderSelect(View selfView, int id) {

        Log.println(Log.DEBUG, "SELECTED_GENDER", "Gender.id : " + id);

        for(int i = 0; i < genderButtons.size(); i++) {
            Button b = genderButtons.get(i);
            b.setBackground(ContextCompat.getDrawable(this, R.drawable.button_bg_raw));
            setUncheckedGenderButton(b);
        }

        Button meButton = (Button)selfView;
        meButton.setBackground(ContextCompat.getDrawable(this, R.drawable.button_bg_raw_selected));
        setCheckedGenderButton(meButton);

        /*Drawable img = getResources().getDrawable(	android.R.drawable.checkbox_on_background);
        img.setBounds(8, 0, 32, 32);
        meButton.setCompoundDrawables(img, null, null, null);*/

        GenderDataManager.SelectGenderById(id);

        //TODO : change this part
        if(id == 1) {
            UserProfileDataObject.userGenderName = "female";
        }
        else {
            UserProfileDataObject.userGenderName = "male";
        }

        //TODO : fetch related people on showcase
        //RedirectToProfilePhoto();

        EnableContinueButton();
    }

    private ServiceResponseVO saveSignupInfo(String userName, String birthDate, String gender) throws Exception {
        HashMap<String, String> params = new HashMap<>();
        params.put("username", userName);
        params.put("birthdate", birthDate);
        params.put("gender", gender);

        RequestHelper rh = new RequestHelper(getApplicationContext());
        ServiceResponseVO resultVO = rh.sendRequest("/login/signup", params, HttpMethods.PUT);

        return resultVO;
    }

    private void RedirectToProfilePhotoActivity() {
        RedirectTo(ProfilePhotoActivity.class);
    }
}
