package com.tg.helper;

import android.content.Context;
import android.util.Log;

import com.tg.VO.HandShakeVO;
import com.tg.VO.LoginVO;
import com.tg.dataManager.StorageManager;
import com.tg.requestManager.HttpManager;

public class HandshakeHelper {

    public static HandShakeVO doHandshake(String authToken, Context appContext) throws Exception {

        Log.d("doHandshake", "auth : " + authToken);

        HttpManager httpManager = new HttpManager(appContext);
        LoginVO loginResultVO = httpManager.getLoginResponseResult(authToken);

        if(loginResultVO == null && loginResultVO.authToken == null) {
            throw new Exception("loginResultVO : was null");
        }

        Boolean saveAuthTokenResult = StorageManager.SaveUserAuthToken(loginResultVO.authToken, appContext);
        if(!saveAuthTokenResult) {
            throw new Exception("saveAuthTokenResult : " + saveAuthTokenResult.toString());
        }

        //
        HandShakeVO handShakeVO = httpManager.getHandShakeResult(loginResultVO.authToken);
        Boolean saveAccessTokenResult = StorageManager.SaveUserAccessToken(handShakeVO.accessToken, appContext);
        if(!saveAccessTokenResult) {
            throw new Exception("saveAccessTokenResult : " + saveAccessTokenResult.toString());
        }

        return handShakeVO;
    }

}
