package com.tg.derdoapp;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.tg.VO.MatchGetInfoVO;
import com.tg.VO.MatchUserVO;
import com.tg.VO.SendInteractionVO;
import com.tg.dataManager.DistanceManager;
import com.tg.dataManager.MatchUsersDataManager;
import com.tg.dataObject.NavActivity;
import com.tg.globals.AppGlobals;
import com.tg.helper.RequestHelper;
import com.tg.requestManager.HttpMethods;

import java.util.HashMap;

public class ShowCaseActivity extends NavigationBaseActivity {

    public ShowCaseActivity() {
        super(NavActivity.SHOWCASE, R.layout.activity_show_case);
    }

    private ImageView profileImageView;
    private TextView showCaseMatchUserDistanceTextView;
    private TextView showCaseMatchUserNameTextView;

    private static CountDownTimer settingsUpDownTimer = null;
    private Boolean isPrivateModeOn = false;

    private int interactionCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!AppGlobals.hasLocationInfo) {
            RedirectToLocationInfo();
            return;
        }

        profileImageView = (ImageView)findViewById(R.id.profileImageViewButton);
        showCaseMatchUserDistanceTextView = (TextView)findViewById(R.id.showcaseDistanceText);
        showCaseMatchUserNameTextView = (TextView)findViewById(R.id.showcaseNameView);

        DoShowcaseActions();

        loadAd();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchResetTimerValueAndSet();
    }

    //TODO : DO IMAGE ACTIONS OUT OF MATCH_USERS LIST!
    private void DoShowcaseActions() {

        final android.widget.ImageButton selectProfilePhotoButton = findViewById(R.id.showcaseSettingsIcon);
        selectProfilePhotoButton.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                RedirectToSettings();
            }
        });

        final android.widget.ImageButton greetButton = findViewById(R.id.greetings_ok_button);
        greetButton.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                try {
                    sendInteraction(true);
                }
                catch (Exception ex) {
                    ShowErrorToast(ex);
                }
            }
        });

        final android.widget.ImageButton rejectButton = findViewById(R.id.greetings_nok_button);
        rejectButton.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                try {
                    sendInteraction(false);
                }
                catch (Exception ex) {
                    ShowErrorToast(ex);
                }
            }
        });

        if(!MatchUsersDataManager.Instance().hasAny()) {
            try {
                getNextMatch();
            }
            catch (Exception ex) {
                ShowErrorToast(ex);
            }
        }
        else {
            setMatchUserInfoOnAreas(MatchUsersDataManager.Instance().getNext());
            setGreetingsCountFromAppGlobals();
        }

        ImageButton privateModeSettinsButton = (ImageButton) findViewById(R.id.sc_private_mode_settings_switch_ib);
        if(privateModeSettinsButton != null) {
            privateModeSettinsButton.setOnClickListener(new android.view.View.OnClickListener() {
                public void onClick(android.view.View v) {
                    try {
                        changePrivateMode(privateModeSettinsButton);
                    } catch (Exception ex) {
                        ShowErrorToast(ex);
                    }
                }
            });
        }
    }

    private void setMatchUserInfoOnAreas(MatchUserVO matchUserVO) {
        displayImageOnView(matchUserVO.profilePictureUrl, profileImageView);

        ImageView locIcon = (ImageView)findViewById(R.id.imageLocationIco);
        if(matchUserVO.distance != null) {
            locIcon.setVisibility(View.VISIBLE);
            showCaseMatchUserDistanceTextView.setText(DistanceManager.GetDistanceForField(matchUserVO.distance));
            showCaseMatchUserDistanceTextView.setVisibility(View.VISIBLE);
        }
        else {
            locIcon.setVisibility(View.INVISIBLE);
            showCaseMatchUserDistanceTextView.setText(null);
            showCaseMatchUserDistanceTextView.setVisibility(View.INVISIBLE);
        }

        showCaseMatchUserNameTextView.setText(matchUserVO.userName + ", " + matchUserVO.age);
    }

    private void sendInteraction(Boolean isGreet) throws Exception {

        //TODO : ASYNC!
        if(isGreet) {
            SendInteractionVO sendInteractionResult = sendGreetToUser(MatchUsersDataManager.Instance().getNext());
            if(sendInteractionResult.isSubscrptionRequired) {
                RedirectToSubscription();
                return;
            }
        }
        else {
            sendRejectToUser(MatchUsersDataManager.Instance().getNext());
        }

        getNextMatch();

        if(interactionCounter >= 7) {
            showAd();
            interactionCounter = 0;
        }

        interactionCounter++;
    }

    private void getNextMatch() throws Exception {
        MatchUsersDataManager.Instance().removeFromList(MatchUsersDataManager.Instance().getNext());
        Boolean hasNext = getNextSetInfo();
        if(hasNext) {
            setMatchUserInfoOnAreas(MatchUsersDataManager.Instance().getNext());
        }
        else {

            if(this.isPrivateModeOn) {
                hideShowcaseSectionsShowPrivateModeInfo();
            }
            else {
                hideShowcaseSectionAndButtonsShowInfo();
            }
        }
    }

    private Boolean changePrivateMode(ImageButton privateModeImageButton) throws Exception {

        privateModeImageButton.setImageResource(R.drawable.switch_off);

        MatchUsersDataManager.Instance().resetList();

        RequestHelper rh = new RequestHelper(getApplicationContext());
        Boolean resultVO = rh.sendRequest("/profile/changeprivatemode", null, HttpMethods.POST, Boolean.class);

        if(resultVO != null) {
            RedirectTo(ShowCaseActivity.class);
        }

        return resultVO;
    }

    private void hideShowCaseSections() {
        View showCaseV = findViewById(R.id.showcase_show_section);
        View buttonsV = findViewById(R.id.linearLayout_ok_nok_container);

        showCaseV.setVisibility(View.GONE);
        buttonsV.setVisibility(View.GONE);
    }

    private void hideShowcaseSectionAndButtonsShowInfo() {
        hideShowCaseSections();

        View noResultView = findViewById(R.id.no_search_result_ll);
        noResultView.setVisibility(View.VISIBLE);

        setSettingsUpDownTimer();
    }

    private void hideShowcaseSectionsShowPrivateModeInfo() {
        hideShowCaseSections();

        View noResultView = findViewById(R.id.sc_private_mode_on_tb);
        noResultView.setVisibility(View.VISIBLE);

    }

    protected void setSettingsUpDownTimer()
    {
        if(settingsUpDownTimer != null) {
            settingsUpDownTimer.cancel();
        }

        ImageButton settingsImageButton = findViewById(R.id.showcaseSettingsIcon);
        settingsUpDownTimer = new CountDownTimer(800,800) {

            Boolean onNormal = true;

            public void onTick(long millisUntilFinished) {
                //float deg = settingsImageButton.getRotation() + 90F;
                //settingsImageButton.animate().rotation(deg).setInterpolator(new AccelerateDecelerateInterpolator());

                /*if(onNormal) {
                    settingsImageButton.setImageResource(R.drawable.db_settings_green);
                }
                else {
                    settingsImageButton.setImageResource(R.drawable.db_settings_icon);
                }

                onNormal = !onNormal;*/

                //settingsImageButton.setImageResource(R.drawable.db_settings_green);
                settingsImageButton.setImageResource(R.drawable.db_settings_icon);
            }

            public void onFinish() {
                settingsImageButton.setImageResource(R.drawable.db_settings_green);

                settingsUpDownTimer.start();
            }

        }.start();
    }

    private SendInteractionVO sendGreetToUser(MatchUserVO matchUserVO) throws Exception {
        HashMap<String, String> params = new HashMap();
        params.put("appuserid", matchUserVO.id);

        RequestHelper rh = new RequestHelper(getApplicationContext());
        SendInteractionVO resultVO = rh.sendRequest("/match/sendgreet", params, HttpMethods.POST, SendInteractionVO.class);

        return resultVO;
    }

    private Boolean sendRejectToUser(MatchUserVO matchUserVO) throws Exception {
        HashMap<String, String> params = new HashMap();
        params.put("appuserid", matchUserVO.id);

        RequestHelper rh = new RequestHelper(getApplicationContext());
        return rh.sendRequest("/match/sendreject", params, HttpMethods.POST).success;
    }

    private Boolean getNextSetInfo() throws Exception {
        return getNextSetInfo(false);
    }

    private Boolean getNextSetInfo(Boolean isFirstRequest) throws Exception {
        MatchGetInfoVO matchGetInfo = getMatches(false);
        setGreetingsCount(matchGetInfo.greetingsLeft);
        this.isPrivateModeOn = matchGetInfo.isPrivateModeOn;
        return MatchUsersDataManager.Instance().addToList(matchGetInfo.matchUsers);
        //return true;
    }

    private MatchGetInfoVO getMatches(Boolean isFirstRequest) throws Exception {
        HashMap<String, String> params = new HashMap();
        params.put("isFirstReq", String.valueOf(isFirstRequest));

        RequestHelper rh = new RequestHelper(getApplicationContext());
        MatchGetInfoVO resultVO = rh.sendRequest("/match/getnext", params, HttpMethods.GET, MatchGetInfoVO.class);

        return resultVO;
    }

    private void RedirectToSettings() {
        //RedirectTo(SettingsActivity.class);
        redirectToWithAnimation(SettingsActivity.class, new TransitionAnimationsHolder(R.anim.transition_up_to_down, R.anim.transition_up_to_down_out));
    }

    private void RedirectToLocationInfo() {
        RedirectTo(LocationInfoActivity.class);
    }
}
