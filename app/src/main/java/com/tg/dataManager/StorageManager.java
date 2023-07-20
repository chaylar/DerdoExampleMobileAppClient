package com.tg.dataManager;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.tg.dataObject.StorageMeta;
import com.tg.dataObject.UserProfileDataObject;

public class StorageManager {

    public static Boolean SaveUserAccessToken(String userAccessToken, final Context appContext) {

        SharedPreferences sharedPref = appContext.getSharedPreferences(StorageMeta.APP_GEN_SHARED_PREF_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor spEditor = sharedPref.edit();

        spEditor.putString(StorageMeta.APP_USER_SHARED_AT_KEY, userAccessToken);
        Boolean result = spEditor.commit();

        return result;
    }

    public static Boolean SaveUserAuthToken(String userAuthToken, final Context appContext) {

        SharedPreferences sharedPref = appContext.getSharedPreferences(StorageMeta.APP_GEN_SHARED_PREF_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor spEditor = sharedPref.edit();

        spEditor.putString(StorageMeta.APP_USER_SHARED_AUTH_KEY, userAuthToken);
        Boolean result = spEditor.commit();

        return result;
    }

    public static Boolean DeleteUserAuthToken(final Context appContext) {

        SharedPreferences sharedPref = appContext.getSharedPreferences(StorageMeta.APP_GEN_SHARED_PREF_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor spEditor = sharedPref.edit();

        spEditor.remove(StorageMeta.APP_USER_SHARED_AUTH_KEY);
        Boolean result = spEditor.commit();

        return result;
    }

    public static Boolean DeleteUserAccessToken(final Context appContext) {

        SharedPreferences sharedPref = appContext.getSharedPreferences(StorageMeta.APP_GEN_SHARED_PREF_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor spEditor = sharedPref.edit();

        spEditor.remove(StorageMeta.APP_USER_SHARED_AT_KEY);
        Boolean result = spEditor.commit();

        return result;
    }

    public static String GetUserAccessToken(final Context appContext) {

        SharedPreferences sharedPref = appContext.getSharedPreferences(StorageMeta.APP_GEN_SHARED_PREF_KEY, Context.MODE_PRIVATE);
        String resourceValue = null;
        resourceValue = sharedPref.getString(StorageMeta.APP_USER_SHARED_AT_KEY, resourceValue);

        return resourceValue;
    }

    public static String GetUserAuthToken(final Context appContext) {

        SharedPreferences sharedPref = appContext.getSharedPreferences(StorageMeta.APP_GEN_SHARED_PREF_KEY, Context.MODE_PRIVATE);
        String resourceValue = null;
        resourceValue = sharedPref.getString(StorageMeta.APP_USER_SHARED_AUTH_KEY, resourceValue);

        return resourceValue;
    }

    //TODO : ??
    public static Boolean SaveUserProfileData(UserProfileDataObject userProfileData, final Context appContext) {

        SharedPreferences sharedPref = appContext.getSharedPreferences(StorageMeta.APP_GEN_SHARED_PREF_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor spEditor = sharedPref.edit();

        String jsonSerializeString = SerializeUserProfileData(userProfileData);
        spEditor.putString(StorageMeta.APP_USER_SHARED_PREF_KEY, jsonSerializeString);
        Boolean result = spEditor.commit();

        return result;
    }

    //TODO : ??
    public static UserProfileDataObject GetUserProfileData(final Context appContext) {

        SharedPreferences sharedPref = appContext.getSharedPreferences(StorageMeta.APP_GEN_SHARED_PREF_KEY, Context.MODE_PRIVATE);
        String resourceValue = null;
        resourceValue = sharedPref.getString(StorageMeta.APP_USER_SHARED_PREF_KEY, resourceValue);
        UserProfileDataObject result = DeserializeUserProfileData(resourceValue);

        return result;
    }

    private static String SerializeUserProfileData(UserProfileDataObject userProfileData) {
        Gson gson = new Gson();
        return gson.toJson(userProfileData);
    }

    private static UserProfileDataObject DeserializeUserProfileData(String jsonStoredData) {
        Gson gson = new Gson();
        return gson.fromJson(jsonStoredData, UserProfileDataObject.class);
    }
}
