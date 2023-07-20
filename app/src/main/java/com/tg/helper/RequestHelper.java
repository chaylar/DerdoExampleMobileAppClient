package com.tg.helper;

import android.content.Context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tg.VO.MatchUserVO;
import com.tg.VO.SendInteractionVO;
import com.tg.requestManager.HttpManager;
import com.tg.requestManager.HttpMethods;
import com.tg.requestManager.RequestPackage;
import com.tg.requestManager.ServiceResponseVO;

import java.util.HashMap;

public class RequestHelper {

    protected Context appContext;

    public RequestHelper(Context appContextBase) {
        this.appContext = appContextBase;
    }

    public ServiceResponseVO sendRequest(String requestUrl, HashMap<String, String> params, String httpMethod) throws Exception {

        RequestPackage requestPackage = new RequestPackage(requestUrl, httpMethod, params);
        HttpManager httpManager = new HttpManager(appContext);
        ServiceResponseVO responseVO = httpManager.getServiceResponseResult(requestPackage);

        return responseVO;
    }

    public <K> K sendRequest(String requestUrl, HashMap<String, String> params, String httpMethod, Class<K> classType) throws Exception {

        RequestPackage requestPackage = new RequestPackage(requestUrl, httpMethod, params);
        HttpManager httpManager = new HttpManager(appContext);
        ServiceResponseVO responseVO = httpManager.getServiceResponseResult(requestPackage);

        ObjectMapper om = new ObjectMapper();
        K resultVO = om.convertValue(responseVO.data, classType);

        return resultVO;
    }

}
