package com.tg.derdoapp;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ianpinto.androidrangeseekbar.rangeseekbar.RangeSeekBar;
import com.tg.VO.AppUserSettingsVO;
import com.tg.VO.MatchUserVO;
import com.tg.VO.SendInteractionVO;
import com.tg.VO.SettingsVO;
import com.tg.dataManager.ExceptionManager;
import com.tg.dataManager.MatchUsersDataManager;
import com.tg.dataManager.NuisancesDataManager;
import com.tg.globals.AppGlobals;
import com.tg.helper.NuisanceListViewAdapter;
import com.tg.dataObject.NuisanceType;
import com.tg.dataObject.SettingsDataObject;
import com.tg.globals.SettingsGlobals;
import com.tg.helper.RequestHelper;
import com.tg.requestManager.HttpManager;
import com.tg.requestManager.HttpMethods;
import com.tg.requestManager.RequestPackage;
import com.tg.requestManager.ServiceResponseVO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.ErrorManager;

import javax.security.auth.login.LoginException;

public class SettingsActivity extends NavigationBaseActivity implements NuisanceListViewAdapter.IAppUserSettingNuisanceSetter {

    private List<Button> nuisanceButtons;

    private RangeSeekBar ageRsb;
    private TextView ageSeekBarMinText;
    private TextView ageSeekBarMaxText;

    private AppUserSettingsVO userSettingsVO;

    public SettingsActivity() {
        super(null, R.layout.activity_settings);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nuisanceButtons = new ArrayList<>();

        if(SettingsGlobals.settingsData == null) {
            try {
                SettingsGlobals.settingsData = fetchSettingsConstants();
            }
            catch (Exception ex) {
                ShowErrorToast(ex);
                return;
            }
        }

        userSettingsVO = null;
        try {
            userSettingsVO = fetchUserSettings();
        }
        catch (Exception ex) {
            ShowErrorActivity();
        }

        AppGlobals.showWelcomeSnack = userSettingsVO.isFirstTimeLogin;

        //fillNuisances();
        fillNuisancesDoublePrepData(SettingsGlobals.settingsData.nuisanceTypes);

        ImageButton settingBackButton = (ImageButton)findViewById(R.id.settings_back_button);
        settingBackButton.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                GoToShowcaseAction();
            }
        });

        ageSeekBarMinText = (TextView) findViewById(R.id.age_seek_min);
        ageSeekBarMaxText = (TextView) findViewById(R.id.age_seek_max);

        setSeekBarTexts(userSettingsVO.minAge, userSettingsVO.maxAge);

        ageRsb = (RangeSeekBar)findViewById(R.id.ageRange);

        ageRsb.setRangeValues(SettingsGlobals.settingsData.minAge, SettingsGlobals.settingsData.maxAge);

        //ageRsb.setSelectedMaxValue(SettingsGlobals.settingsData.maxAge);
        //ageRsb.setMinRange(SettingsGlobals.settingsData.minAge);

        ageRsb.setSelectedMaxValue(userSettingsVO.maxAge);
        ageRsb.setSelectedMinValue(userSettingsVO.minAge);
        ageRsb.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
                setSeekBarTexts((Integer)minValue, (Integer)maxValue);
            }
        });

        Button femaleSelectButton = (Button)findViewById(R.id.settings_gender_select_female);
        femaleSelectButton.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                doSelectAction(v, "female");
            }
        });

        Button maleSelectButton = (Button)findViewById(R.id.settings_gender_select_male);
        maleSelectButton.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                doSelectAction(v, "male");
            }
        });

        Button noPrefSelectButton = (Button)findViewById(R.id.settings_gender_select_nopref);
        noPrefSelectButton.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                doSelectAction(v, "no_preference");
            }
        });

        if(userSettingsVO.gender.equals("female")) {
            femaleSelectButton.callOnClick();
        }
        else if(userSettingsVO.gender.equals("male")) {
            maleSelectButton.callOnClick();
        }
        else {
            noPrefSelectButton.callOnClick();
        }

        Button saveSettingsButon = findViewById(R.id.saveSettingsButton);
        saveSettingsButon.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                try {
                    userSettingsVO = updateUserSettings(userSettingsVO);
                    MatchUsersDataManager.Instance().resetList();
                    GoToShowcaseAction();
                }
                catch (Exception e) {
                    ShowErrorToast(e);
                }
            }
        });

        showWelcomeDialog();
    }

    private void setSeekBarTexts(int minText, int maxText) {
        userSettingsVO.maxAge = maxText;
        userSettingsVO.minAge = minText;

        ageSeekBarMinText.setText(Integer.toString(minText));
        ageSeekBarMaxText.setText(Integer.toString(maxText));
    }

    private void doSelectAction(View v, String genderCode) {
        userSettingsVO.gender = genderCode;
        doButtonPressedStateAction();
        setSelectGenderButtonToSelected((Button)v);
    }

    private void doButtonPressedStateAction() {
        Button femaleSelectButton = (Button)findViewById(R.id.settings_gender_select_female);
        Button maleSelectButton = (Button)findViewById(R.id.settings_gender_select_male);
        Button noPrefSelectButton = (Button)findViewById(R.id.settings_gender_select_nopref);

        setSelectGenderButtonToNormal(femaleSelectButton);
        setSelectGenderButtonToNormal(maleSelectButton);
        setSelectGenderButtonToNormal(noPrefSelectButton);
    }

    private void setSelectGenderButtonToNormal(Button butt) {
        butt.setBackground(ContextCompat.getDrawable(this.getBaseContext(), (R.drawable.button_bg_raw_rc)));
        //butt.setBackgroundColor(R.drawable.button_bg_raw_rc);
        //butt.setTextColor(0xFFFFFF);
    }

    private void setSelectGenderButtonToSelected(Button butt) {
        butt.setBackground(ContextCompat.getDrawable(this.getBaseContext(), (R.drawable.button_bg_raw_rc_selected)));
        //butt.setBackgroundColor(R.drawable.button_bg_rounded_corners);
        //butt.setTextColor(0x000000);
    }

    private void fillNuisancesDoublePrepData(List<NuisanceType> nuisanceTypes) {
        //ArrayList<NuisanceType> list = NuisancesDataManager.GetNuisances();
        int listSize = nuisanceTypes.size();
        int listSizeHalf = listSize / 2;

        ArrayList<NuisanceType> list1 = new ArrayList<NuisanceType>();
        ArrayList<NuisanceType> list2 = new ArrayList<NuisanceType>();

        for (int i = 0; i < listSize; i++) {
            NuisanceType lItem = nuisanceTypes.get(i);

            if(i < listSizeHalf) {
                list1.add(lItem);
            }
            else {
                list2.add(lItem);
            }
        }

        ListView listView1 = (ListView) findViewById(R.id.nuisances_list);
        ListView listView2 = (ListView) findViewById(R.id.nuisances_list_sec);

        fillNuisancesDouble(listView1, list1);
        fillNuisancesDouble(listView2, list2);
    }

    private void fillNuisancesDouble(final ListView listView, ArrayList<NuisanceType> list) {
        NuisanceListViewAdapter itemsAdapter =
                new NuisanceListViewAdapter(this, list, this, userSettingsVO.nuisanceTypeCode);
        listView.setAdapter(itemsAdapter);
    }

    private void onNuisanceSelect(NuisanceType selectedItem, View view) {
        Log.w("onNuisanceSelect", String.valueOf(selectedItem.nuisanceName));
        Log.println(Log.DEBUG, "SELECTED_NUISANCE", "Nuisance.id : " + selectedItem.typeCode);
        userSettingsVO.nuisanceTypeCode = selectedItem.typeCode;
        //TODO : VIEW ACTIONS
    }

    private void GoToShowcaseAction() {
        RedirectTo(ShowCaseActivity.class);
    }

    private SettingsVO fetchSettingsConstants() throws Exception {

        RequestHelper rh = new RequestHelper(getApplicationContext());
        SettingsVO resultVO = rh.sendRequest("/settings/getsettingsconstants", null, HttpMethods.GET, SettingsVO.class);

        return resultVO;
    }

    private AppUserSettingsVO fetchUserSettings() throws Exception {

        RequestHelper rh = new RequestHelper(getApplicationContext());
        AppUserSettingsVO resultVO = rh.sendRequest("/settings/getusersettings", null, HttpMethods.GET, AppUserSettingsVO.class);

        return resultVO;
    }

    private AppUserSettingsVO updateUserSettings(AppUserSettingsVO userSettinfgsVO) throws Exception {

        HashMap<String, String> params = new HashMap();
        params.put("nuisance", String.valueOf(userSettinfgsVO.nuisanceTypeCode));
        params.put("minage", String.valueOf(userSettinfgsVO.minAge));
        params.put("maxage", String.valueOf(userSettinfgsVO.maxAge));
        params.put("gender", String.valueOf(userSettinfgsVO.gender));

        RequestHelper rh = new RequestHelper(getApplicationContext());
        AppUserSettingsVO resultVO = rh.sendRequest("/settings/setusersettings", params, HttpMethods.POST, AppUserSettingsVO.class);

        return resultVO;
    }

    @Override
    public void setNuisance(int nuisanceTypeCode, Button button) {

        Log.w("nuisanceButtons.size", String.valueOf(nuisanceButtons.size()));
        for (Button listItem : nuisanceButtons) {
            listItem.setBackground(ContextCompat.getDrawable(this, (R.drawable.button_bg_raw_rc)));
        }

        button.setBackground(ContextCompat.getDrawable(this, (R.drawable.button_bg_raw_rc_selected)));
        Log.w("setNuisance", String.valueOf(nuisanceTypeCode));
        this.userSettingsVO.nuisanceTypeCode = nuisanceTypeCode;
    }

    @Override
    public void addToButtonList(Button b) {
        if(nuisanceButtons == null) {
            nuisanceButtons = new ArrayList<>();
        }

        nuisanceButtons.add(b);
    }
}
