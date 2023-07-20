package com.tg.requestManager;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.FileUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.facebook.HttpMethod;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpPost;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.koushikdutta.async.http.body.MultipartFormDataBody;
import com.tg.VO.HandShakeVO;
import com.tg.VO.LoginVO;
import com.tg.VO.SignupSocialResultVO;
import com.tg.dataManager.StorageManager;
import com.tg.derdoapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class HttpManager {

    private Context appContext;
    private Boolean isLoginRequest = false;
    public Boolean isHandShakeRequst = false;

    public HttpManager(Context appContext) {
        this.appContext = appContext;
    }

    public ServiceResponseVO uploadFileSync(File file) throws Exception {

        String resultStr = getDataFileUpload(file);
        ObjectMapper mapper = new ObjectMapper();
        ServiceResponseVO result = mapper.readValue(resultStr, ServiceResponseVO.class);
        SetIncomingAccessTokenToStorage(result);

        return result;
    }

    public ServiceResponseVO getServiceResponseResult(RequestPackage requestPackage) throws Exception {

        String resultStr = new Downloader().execute(requestPackage).get();
        ObjectMapper mapper = new ObjectMapper();
        ServiceResponseVO result = mapper.readValue(resultStr, ServiceResponseVO.class);
        SetIncomingAccessTokenToStorage(result);

        return result;
    }

    public LoginVO getLoginResponseResult(String authToken) throws Exception {

        isLoginRequest = true;

        String url = "/login/login";
        HashMap<String, String> loginParams = new HashMap<>();
        loginParams.put("authtoken", authToken);

        RequestPackage requestPackage = new RequestPackage(url, HttpMethods.POST, loginParams);

        String resultStr = new Downloader().execute(requestPackage).get();
        ObjectMapper mapper = new ObjectMapper();
        ServiceResponseVO result = mapper.readValue(resultStr, ServiceResponseVO.class);

        if(!result.success) {
            throw new Exception("FAIL RESULT");
        }

        ObjectMapper om = new ObjectMapper();
        LoginVO resultVO = om.convertValue(result.data, LoginVO.class);

        return resultVO;
    }

    public HandShakeVO getHandShakeResult(String authTokenTemp) throws Exception {

        isHandShakeRequst = true;

        String url = "/login/handshake";
        HashMap<String, String> handShakeParams = new HashMap<>();
        handShakeParams.put("authtoken", authTokenTemp);

        RequestPackage requestPackage = new RequestPackage(url, HttpMethods.POST, handShakeParams);

        String resultStr = new Downloader().execute(requestPackage).get();
        ObjectMapper mapper = new ObjectMapper();
        ServiceResponseVO result = mapper.readValue(resultStr, ServiceResponseVO.class);

        if(!result.success) {
            throw new Exception();
        }

        ObjectMapper om = new ObjectMapper();
        HandShakeVO resultVO = om.convertValue(result.data, HandShakeVO.class);

        return resultVO;
    }

    private String getDataFileUpload(File file) throws Exception {

        String fileUploadUrl = "/profile/uploadprofileimage";
        String uri = appContext.getResources().getString(R.string.app_server_address);
        uri += fileUploadUrl;

        AsyncHttpPost post = new AsyncHttpPost(uri);
        MultipartFormDataBody body = new MultipartFormDataBody();
        body.addFilePart("ppfile", file);

        post.setBody(body);

        //NOTE : ACCESS_TOKEN OPERATIONS
        String accessToken = StorageManager.GetUserAccessToken(appContext);
        if(accessToken != null && accessToken != "") {
            post.setHeader(RequestTokenKeys.ACESS_TOKEN, accessToken);
        }
        //

        String result = AsyncHttpClient.getDefaultInstance().executeString(post, new AsyncHttpClient.StringCallback(){
            @Override
            public void onCompleted(Exception ex, AsyncHttpResponse source, String result) {
                if (ex != null) {
                    ex.printStackTrace();
                    return;
                }

                System.out.println("Server says: " + result);
            }
        }).get();

        return result;
    }

    //TODO : GET DATA AS STRING
    private String getData(RequestPackage requestPackage) throws Exception {

        String uri = appContext.getResources().getString(R.string.app_server_address);

        BufferedReader reader = null;
        uri += requestPackage.getUrl();

        if (requestPackage.getMethod().equals(HttpMethods.GET)) {

            String encodedParams = requestPackage.getEncodedParams();
            if(encodedParams != null && encodedParams != "") {
                uri += "?" + encodedParams;
            }

            //As mentioned before, this only executes if the request method has been
            //set to GET
        }

        try {
            URL url = new URL(uri);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            //con.setConnectTimeout(30000);
            con.setRequestMethod(requestPackage.getMethod());

            //NOTE : SET ACCESS || AUTH TOKENS
            if(isLoginRequest || isHandShakeRequst) {
                con = SetAuthTokenToOutGoingRequest(con);
            }
            else {
                con = SetAccessTokenToOutGoingRequest(con);
            }
            //

            //TODO : ADD OTHER METHODS IF NECESSARY
            if (requestPackage.getMethod().equals(HttpMethods.POST) || requestPackage.getMethod().equals(HttpMethods.PUT) || requestPackage.getMethod().equals(HttpMethods.DELETE)) {
                con.setDoOutput(true);
                String encodedParams = requestPackage.getEncodedParams();
                if(encodedParams != null && encodedParams != "") {
                    OutputStreamWriter writer =
                            new OutputStreamWriter(con.getOutputStream());
                    writer.write(encodedParams);
                    writer.flush();
                }
            }

            StringBuilder sb = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            return sb.toString();

        } catch (Exception e) {
            throw e;
            //return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
    }

    private Boolean SetIncomingAccessTokenToStorage(ServiceResponseVO serviceResponseVO) {
        if(serviceResponseVO.accessToken != null && serviceResponseVO.accessToken != "") {
            StorageManager.SaveUserAccessToken(serviceResponseVO.accessToken, appContext);
            return true;
        }

        return false;
    }

    //TODO : SET OUTGOING ACCESS && AUTH TOKENS TO REQUEST
    private HttpURLConnection SetAccessTokenToOutGoingRequest(HttpURLConnection con) {
        String accessToken = StorageManager.GetUserAccessToken(appContext);

        if(accessToken != null && accessToken != "") {
            Log.w("SetAccessTokenToOutGoin", accessToken);
            con.setRequestProperty(RequestTokenKeys.ACESS_TOKEN, accessToken);
        }

        return con;
    }

    //TODO : WILL ONLY BE USED ON LOGIN REQUEST
    private HttpURLConnection SetAuthTokenToOutGoingRequest(HttpURLConnection con) {
        String authToken = StorageManager.GetUserAuthToken(appContext);

        if(authToken != null && authToken != "") {
            con.setRequestProperty(RequestTokenKeys.AUTH_TOKEN, authToken);
        }

        return con;
    }

    private class Downloader extends AsyncTask<RequestPackage, Void, String> {

        @Override
        protected String doInBackground(RequestPackage... params) {
            try {
                //return new JSONObject(HttpManager.getData(params[0]));
                return getData(params[0]);
            }
            catch (Exception ex) {
                Log.w("Downloader.EX : ", ex.getMessage());
            }

            return null;
        }
    }
}
