package com.tg.globals;

import android.app.Application;
import android.net.Uri;

import com.tg.dataObject.FacebookUserData;
import com.tg.derdoapp.R;

public class AppGlobals extends Application {

    public static Uri uploadPhotoUri;

    public static String chatUserId = null;

    public static String chatUserProfileImage = null;

    public static String chatUserUserName = null;

    public static String chatUserNuisanceTypeName = null;

    public static long secondsLeftToReset = -1;

    public static Boolean hasLocationInfo = false;

    public static Boolean hasLoginData = false;

    public static Boolean hasProfileData = false;

    public static Boolean hasProfilePhoto = false;

    public static Boolean isFirstTime = true;

    //public static Boolean isNotificationsActive = true;

    //public static Boolean isPrivateModeActive = false;

    public static int greetingsCount = -20;

    public static String UserId = "TestUserId";

    public static FacebookUserData facebookUserData = null;

    public static Boolean showWelcomeSnack = true;

}
