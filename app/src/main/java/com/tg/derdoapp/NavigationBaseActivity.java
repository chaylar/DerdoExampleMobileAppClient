package com.tg.derdoapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import java.util.ArrayList;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.tg.globals.AppGlobals;
import com.tg.dataObject.NavActivity;
import com.tg.helper.CustomLoadingDialog;
import com.tg.helper.IGreetingsListItemEnabler;

public class NavigationBaseActivity extends BaseActivity {

    private NavActivity navActivity = null;

    protected CustomLoadingDialog customLoadingDialog = null;

    private int activeTag = 0;

    private InterstitialAd mInterstitialAd;

    private RewardedVideoAd mRewardedVideoAd;

    public NavigationBaseActivity(NavActivity navAct, int viewId) {
        super(viewId);
        navActivity = navAct;
    }

    protected void showAd() {

        if(mInterstitialAd == null) {
            Log.d("AMK_AD", "NULL AMK!");
            return;
        }

        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("AMK_AD", "The interstitial wasn't loaded yet.");
        }
    }

    public void showAdVideo() {

        if(mRewardedVideoAd == null) {
            Log.d("AMK_AD_V", "NULL AMK!");
            return;
        }

        if (mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
        } else {
            Log.d("AMK_AD_V", "The interstitial wasn't loaded yet.");
        }
    }

    protected void loadAd() {
        try {

            Log.d("AMK_AD", "starting");

            //String adMobUnit = getString(R.string.admob_unit);

            mInterstitialAd = new InterstitialAd(this);
            //mInterstitialAd.setAdUnitId(adMobUnit);
            mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
            mInterstitialAd.loadAd(new AdRequest.Builder().build());

            MobileAds.initialize(this, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                    //goToGoogleMapsWithCurrentSelected();//
                }
            });

            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    // Proceed to the next act.
                    //redirectToSingSong();
                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                }
            });

            Log.d("AMK_AD", "ending");
        }
        catch (Exception e) {
            Log.e("AMK_AD", e.getMessage() != null ? e.getMessage() : "NULL_MESSAGE");
        }
    }

    protected void loadAdVideo() {
        mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917",
                new AdRequest.Builder().build());
    }

    protected void loadVideoAd(IGreetingsListItemEnabler buttonEnabler) {
        try {

            Log.d("AMK_AD_V", "starting");

            //String adMobUnit = getString(R.string.admob_unit);

            MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");

            // Use an activity context to get the rewarded video instance.
            mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
            // Make sure that the MainActivity class implements the required
            // interface: RewardedVideoAdListener
            mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
                @Override
                public void onRewardedVideoAdLoaded() {
                    if(buttonEnabler != null) {
                        buttonEnabler.enableListItemOkButton();
                    }
                }

                @Override
                public void onRewardedVideoAdOpened() {

                }

                @Override
                public void onRewardedVideoStarted() {

                }

                @Override
                public void onRewardedVideoAdClosed() {

                }

                @Override
                public void onRewarded(RewardItem rewardItem) {

                }

                @Override
                public void onRewardedVideoAdLeftApplication() {

                }

                @Override
                public void onRewardedVideoAdFailedToLoad(int i) {

                }

                @Override
                public void onRewardedVideoCompleted() {
                    //loadVideoAd(buttonEnabler);
                    if(buttonEnabler != null) {
                        buttonEnabler.disableListItemOkButton();
                    }

                    loadAdVideo();
                }
            });

            //loadVideoAd(buttonEnabler);
            loadAdVideo();

            Log.d("AMK_AD_V", "ending");
        }
        catch (Exception e) {
            Log.e("AMK_AD_V", e.getMessage() != null ? e.getMessage() : "NULL_MESSAGE");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doNavbarActions();
    }

    private void doNavbarActions() {

        try {

            ArrayList<ImageButton> navbarButtons = getNavButtons();

            for (int i = 0; i < navbarButtons.size(); i++) {

                setButtonToDefault(navbarButtons.get(i));

                if(AppGlobals.isFirstTime) {
                    setNavButtonDisabledAlpha(navbarButtons.get(i));
                }
            }

            ImageButton activeButton = null;
            if (navActivity == NavActivity.PROFILE_PHOTO) {
                activeButton = (ImageButton) findViewById(R.id.nav_bar_profile_image_button);
            } else if (navActivity == NavActivity.SHOWCASE) {
                activeButton = (ImageButton) findViewById(R.id.nav_bar_showcase_image_button);
            }
            else if (navActivity == NavActivity.GREETINGS) {
                activeButton = (ImageButton) findViewById(R.id.nav_bar_greetings_image_button);
            }
            else if (navActivity == NavActivity.MESSAGES) {
                activeButton = (ImageButton) findViewById(R.id.nav_bar_messages_image_button);
            }

            if (activeButton != null) {
                setNavButtonActive(activeButton);
            }
        }
        catch (Exception e) {
            Log.d("doNavbarActions.E", "doNavbarActions.E : " + e.getMessage());
        }
    }

    private ArrayList<ImageButton> getNavButtons() {
        ImageButton profileImageButton = (ImageButton)findViewById(R.id.nav_bar_profile_image_button);
        ImageButton showcaseImageButton = (ImageButton)findViewById(R.id.nav_bar_showcase_image_button);
        ImageButton messagesImageButton = (ImageButton)findViewById(R.id.nav_bar_messages_image_button);
        ImageButton greetingsImageButton = (ImageButton)findViewById(R.id.nav_bar_greetings_image_button);

        profileImageButton.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                TransitionAnimationsHolder transitionHolder = getTransitionAnimations(activeTag, getNavButtonTag(v));
                redirectToWithAnimation(ProfileExtActivity.class, transitionHolder);
                //RedirectTo(ProfileExtActivity.class, R.anim.transition_right_to_left, R.anim.transition_right_to_left_out);
            }
        });

        showcaseImageButton.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                TransitionAnimationsHolder transitionHolder = getTransitionAnimations(activeTag, getNavButtonTag(v));
                redirectToWithAnimation(ShowCaseActivity.class, transitionHolder);
                //RedirectTo(ShowCaseActivity.class);
            }
        });

        greetingsImageButton.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                TransitionAnimationsHolder transitionHolder = getTransitionAnimations(activeTag, getNavButtonTag(v));
                redirectToWithAnimation(GreetingsActivity.class, transitionHolder);
                //RedirectTo(GreetingsActivity.class);
            }
        });

        messagesImageButton.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                TransitionAnimationsHolder transitionHolder = getTransitionAnimations(activeTag, getNavButtonTag(v));
                redirectToWithAnimation(MessagesActivity.class, transitionHolder);
                //RedirectTo(MessagesActivity.class);
            }
        });

        ArrayList<ImageButton> navButtons = new ArrayList();
        navButtons.add(profileImageButton);
        navButtons.add(showcaseImageButton);
        navButtons.add(greetingsImageButton);
        navButtons.add(messagesImageButton);

        return navButtons;
    }

    private void setButtonToDefault(ImageButton imgButton) {
        if(imgButton != null) {

            if (imgButton.getId() == R.id.nav_bar_profile_image_button) {
                imgButton.setImageResource(R.drawable.db_single_person_icon);
            }
            else if (imgButton.getId() == R.id.nav_bar_showcase_image_button) {
                imgButton.setImageResource(R.drawable.db_search_icon);
            }
            else if (imgButton.getId() == R.id.nav_bar_greetings_image_button) {
                imgButton.setImageResource(R.drawable.greetings_icon_grey);
            }
            else if (imgButton.getId() == R.id.nav_bar_messages_image_button) {
                imgButton.setImageResource(R.drawable.messages_icon_grey);
            }
        }
    }

    private void setNavButtonActive(ImageButton imgButton) {
        if(imgButton != null) {
            if (imgButton.getId() == R.id.nav_bar_profile_image_button) {
                imgButton.setImageResource(R.drawable.single_person_grn);
            }
            else if (imgButton.getId() == R.id.nav_bar_showcase_image_button) {
                imgButton.setImageResource(R.drawable.search_grn);
            }
            else if (imgButton.getId() == R.id.nav_bar_greetings_image_button) {
                imgButton.setImageResource(R.drawable.greetings_icon_green);
            }
            else if (imgButton.getId() == R.id.nav_bar_messages_image_button) {
                imgButton.setImageResource(R.drawable.messages_icon_green);
            }

            activeTag = getNavButtonTag(imgButton);
        }
    }

    private void setNavButtonDisabledAlpha(ImageButton imgButton) {
        if(imgButton != null) {
            if (imgButton.getId() == R.id.nav_bar_profile_image_button) {
                imgButton.setImageResource(R.drawable.db_single_person_icon);
            }
            else if (imgButton.getId() == R.id.nav_bar_showcase_image_button) {
                imgButton.setImageResource(R.drawable.db_search_icon);
            }
            else if (imgButton.getId() == R.id.nav_bar_greetings_image_button) {
                imgButton.setImageResource(R.drawable.greetings_icon_grey);
            }
            else if (imgButton.getId() == R.id.nav_bar_messages_image_button) {
                imgButton.setImageResource(R.drawable.messages_icon_grey);
            }
        }
    }
}
