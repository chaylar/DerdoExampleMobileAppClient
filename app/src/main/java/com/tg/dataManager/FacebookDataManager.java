package com.tg.dataManager;

import android.util.Log;

import com.tg.dataObject.FacebookUserData;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FacebookDataManager {

    public static FacebookUserData getUserDataFromGraphResponse(JSONObject object) throws JSONException {

        if(object == null) {
            return null;
        }

        FacebookUserData fbUserData = new FacebookUserData();

        if(object.has("id")) {
            fbUserData.userId = object.getString("id");
        }

        if(object.has("email")) {
            fbUserData.email = object.getString("email");
        }

        if(object.has("name")) {
            fbUserData.fullName = object.getString("name");
        }

        if(object.has("first_name")) {
            fbUserData.firstName = object.getString("first_name");
        }

        if(object.has("last_name")) {
            fbUserData.lastName = object.getString("last_name");
        }

        if(object.has("gender")) {
            fbUserData.gender = object.getString("gender");
        }

        if(object.has("birthday")) {
            //fbUserData.birthDate = object.getString("birthday");
            String fbBirthDateStr = object.getString("birthday");
            Date date = new Date(fbBirthDateStr);
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            fbUserData.birthDate = formatter.format(date);
        }

        try {
            if (object.has("picture")) {
                JSONObject pictureObject = object.getJSONObject("picture");
                if (pictureObject.has("data")) {
                    JSONObject pictureData = pictureObject.getJSONObject("data");
                    if (pictureData.has("url")) {
                        fbUserData.profilePictureUrl = pictureData.getString("url");
                    }
                }
            }
        }
        catch (Exception e) {
            Log.e("FbPicture.e", e.getMessage());
        }

        return fbUserData;
    }

}
