package com.tg.derdoapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.tg.VO.SignupSocialResultVO;
import com.tg.globals.AppGlobals;
import com.tg.dataManager.ExceptionManager;
import com.tg.dataManager.FacebookDataManager;
import com.tg.dataObject.FacebookUserData;
import com.tg.helper.HandshakeHelper;
import com.tg.helper.RequestHelper;
import com.tg.requestManager.HttpMethods;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;

public class LoginActivity extends BaseActivity {

    public LoginActivity() {
        super(R.layout.activity_login);
    }

    private CallbackManager facebookCallbackManager;

    private int RC_SIGN_IN = 1010;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.fullyInitialize();

        //TODO : FOR TEST PURPOSES ONLYT
        LoginManager.getInstance().logOut();
        //

        setContentView(R.layout.activity_login);

        final android.widget.ImageButton fConnect = findViewById(R.id.login_f_connect_imgbutton);

        final android.widget.ImageButton gConnect = findViewById(R.id.login_g_connect_imgbutton);
        /*gConnect.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                SignupSocialResultVO mockSS = new SignupSocialResultVO();
                mockSS.isRegisteredUser = false;
                mockSS.isFirstLogin = true;
                mockSS.authToken = null;

                GetLoginAndRedirect(mockSS);
            }
        });*/

        final android.widget.ImageButton iConnect = findViewById(R.id.login_i_connect_imgbutton);
        /*iConnect.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                SignupSocialResultVO mockSS = new SignupSocialResultVO();
                mockSS.isRegisteredUser = false;
                mockSS.isFirstLogin = true;
                mockSS.authToken = null;

                GetLoginAndRedirect(mockSS);
            }
        });*/

        facebookCallbackManager = CallbackManager.Factory.create();
        final LoginButton loginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        //loginButton.setReadPermissions(Arrays.asList("email"));

        //LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile,user_location,user_birthday"));
        loginButton.registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(
                    loginResult.getAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(
                                JSONObject object,
                                GraphResponse response) {
                            Log.d("FB_RAW", object.toString());
                            try {
                                SignupSocialResultVO facebookSignupResultVO = saveFacebookData(object);
                                FacebookUserData fbUserData = FacebookDataManager.getUserDataFromGraphResponse(object);
                                AppGlobals.facebookUserData = fbUserData;
                                GetLoginAndRedirect(facebookSignupResultVO);
                            }
                            catch (Exception e) {
                                Log.e("FacebookDataManager.e", e.getMessage());
                                ExceptionManager.LogExceptionStackTrace("FacebookDataManager.st", e);
                                Toast.makeText(getApplicationContext(), "Bir Hata Oluştu", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,link,email,birthday,first_name,last_name,gender,location,picture");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.d("fbOnCancel", "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("fbOnError", "facebook:onError", error);
                // ...
            }
        });

        fConnect.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                TriggerFacebookLogin();
            }
        });

        gConnect.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                TriggerGoogleConnect();
            }
        });
    }

    private void TriggerGoogleConnect() {

        /*GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);*/

    }

    private SignupSocialResultVO saveFacebookData(JSONObject facebookinfo) throws Exception {
        HashMap<String, String> params = new HashMap<>();
        params.put("facebookinfo", facebookinfo.toString());

        RequestHelper rh = new RequestHelper(getApplicationContext());
        SignupSocialResultVO resultVO = rh.sendRequest("/login/signupfacebook", params, HttpMethods.PUT, SignupSocialResultVO.class);

        HandshakeHelper.doHandshake(resultVO.authToken, this);

        return resultVO;
    }

    private void TriggerFacebookLogin() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email,public_profile,user_gender,user_location,user_birthday"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.w("requestCode", String.valueOf(requestCode));
        if (requestCode == RC_SIGN_IN) {
            /*Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);*/
        }
        else {
            //NOTE : fb_req : 64206 ??
            facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
            Log.w("fbLoginOnActivityResult", "data : " + data);

        }
    }

    /*private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String email = account.getEmail();
            Log.d("handleGoogle.email", email);

        } catch (ApiException e) {
            Log.e("handleGoogle.ex", e.getMessage() != null ? e.getMessage() : "MESSSAGE_WAS_NULL");

            ShowErrorToast();
        }
    }*/

    private void handleFacebookAccessToken(Object at) {
        Log.w("hfbl", "handleFacebookAccessToken : " + at.toString());
    }

    private void GetLoginAndRedirect(SignupSocialResultVO signupResultVO) {
        AppGlobals.hasLoginData = true;
        Class<? extends AppCompatActivity> redirectToClass = null;
        if(signupResultVO.isFirstLogin || (!signupResultVO.isRegisteredUser)) {
            redirectToClass = ProfileNameActivity.class;
        }
        else if(!signupResultVO.isFirstLogin && !signupResultVO.hasProfilePhoto) {
            redirectToClass = ProfilePhotoActivity.class;
        }
        else {
            redirectToClass = ShowCaseActivity.class;
        }

        //startActivity(redirectTo);
        RedirectTo(redirectToClass);
    }
}
