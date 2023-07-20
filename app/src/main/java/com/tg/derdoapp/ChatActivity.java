package com.tg.derdoapp;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.tg.VO.MatchUserVO;
import com.tg.VO.SendInteractionVO;
import com.tg.dataManager.DbManager;
import com.tg.dataObject.NavActivity;
import com.tg.globals.AppGlobals;
import com.tg.helper.ChatMessageRecyclerListAdapter;
import com.tg.helper.CustomDialog;
import com.tg.helper.CustomImageDialog;
import com.tg.helper.IDialogFunctionPasser;
import com.tg.helper.MatchUserMessageContainer;
import com.tg.helper.MessagesListItemRecyclerViewAdapter;
import com.tg.helper.RequestHelper;
import com.tg.helper.UserMessageDBModel;
import com.tg.requestManager.HttpMethods;

import java.io.Console;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends BaseActivity {

    private DbManager localDbManager;

    private ChatMessageRecyclerListAdapter chatMessagesViewAdapter;

    private RecyclerView messagesListViewRecycler;

    private LinearLayoutManager layoutManager;

    private EditText chatEditText;
    //private Button sendButton;
    private ImageButton sendButton;

    private BaseActivity currentActivity;

    public ChatActivity() {
        super(R.layout.activity_chat);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentActivity = this;

        localDbManager = new DbManager(this);
        chatEditText = findViewById(R.id.ca_edittext_chatbox);
        sendButton = findViewById(R.id.ca_button_chatbox_send);

        ImageButton backButton = findViewById(R.id.chat_back_button);
        backButton.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                GetMesagesRedirect();
            }
        });

        ImageButton deleteButton = findViewById(R.id.chat_delete_button);
        deleteButton.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                generateCustomDialog(getResources().getString(R.string.chat_delete_history_warning), R.drawable.warning, true, "Evet",true, true, 0, new DeleteFunctionPasser()).show();
            }
        });
        //

        //Button sendButton = findViewById(R.id.ca_button_chatbox_send);
        ImageButton sendButton = findViewById(R.id.ca_button_chatbox_send);
        sendButton.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                getMessageNSend();
            }
        });

        CircleImageView imgView = (CircleImageView) findViewById(R.id.ca_chat_user_profilew_image);
        displayImageOnView(AppGlobals.chatUserProfileImage, imgView);

        View customDialogView = LayoutInflater.from(currentActivity).inflate(R.layout.custom_image_dialog, null);
        ImageView profileImageView = customDialogView.findViewById(R.id.custom_image_dialog_image_view);
        displayImageOnView(AppGlobals.chatUserProfileImage, profileImageView);
        CustomImageDialog popupDialog = new CustomImageDialog(currentActivity, customDialogView);

        imgView.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                try {
                    popupDialog.show();
                }
                catch (Exception ex) {
                    ShowErrorToast(ex);
                }
            }
        });

        TextView userNameTV = (TextView) findViewById(R.id.ca_chat_user_user_name);
        userNameTV.setText(AppGlobals.chatUserUserName);

        TextView nuisanceTypeName = (TextView) findViewById(R.id.ca_user_nuisance_name);
        String nuisanceSentence = "";
        if(AppGlobals.chatUserNuisanceTypeName != null && AppGlobals.chatUserNuisanceTypeName != "") {
            nuisanceSentence = "İkiniz de \"" + AppGlobals.chatUserNuisanceTypeName + "\" üzerine dertleşmek istediniz...";
        }

        nuisanceTypeName.setText(nuisanceSentence);

        List<UserMessageDBModel> userMessages = localDbManager.getMessagesRelatedTo(AppGlobals.chatUserId);
        FillMessagesListDataRecycler(userMessages);

        localDbManager.updateIsReadStatus(AppGlobals.chatUserId, true);

        sendButton.setEnabled(false);
        sendButton.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.send_but_10_inact));

        TextWatcher tw = new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //Log.d("ATC_C", editable.toString());
                if(editable.length() > 0) {
                    sendButton.setEnabled(true);
                    sendButton.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.send_but_10));
                }
                else {
                    sendButton.setEnabled(false);
                    sendButton.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.send_but_10_inact));
                }
            }
        };

        chatEditText.addTextChangedListener(tw);
    }

    private void GetMesagesRedirect() {
        RedirectTo(MessagesActivity.class);
    }

    private CustomDialog generateCustomDialog(String text, Integer iconResource, Boolean hasOkButton, String okButtonText, Boolean hasNokButton, Boolean isFatal, long showSeconds, IDialogFunctionPasser functionPasser) {
        return new CustomDialog(this, text, iconResource, hasOkButton, okButtonText, hasNokButton, isFatal, showSeconds, functionPasser);
    }

    private void FillMessagesListDataRecycler(List<UserMessageDBModel> userMessages) {

        if(userMessages == null) {
            userMessages = new ArrayList<>();
        }

        messagesListViewRecycler = findViewById(R.id.ca_reyclerview_message_list);
        layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, true);
        messagesListViewRecycler.setLayoutManager(layoutManager);

        chatMessagesViewAdapter = new ChatMessageRecyclerListAdapter(userMessages, this);
        messagesListViewRecycler.setAdapter(chatMessagesViewAdapter);
    }

    private void getMessageNSend() {
        EditText et = findViewById(R.id.ca_edittext_chatbox);
        String messageContent = et.getText().toString();

        if(messageContent == null || messageContent.equals("")) {
            return;
        }

        sendMessage(AppGlobals.chatUserId, messageContent);
        et.getText().clear();

        sendButton.setEnabled(false);
    }

    //TODO : SEND MESSAGE TO BACKEND
    private void sendMessage(String toUserId, String messageContent) {
        long id = localDbManager.insertUserMesssage(null, toUserId, messageContent, true);
        UserMessageDBModel umDBModel = localDbManager.getById(id);
        int listSize = chatMessagesViewAdapter.addMessageToList(umDBModel);

        layoutManager.smoothScrollToPosition(messagesListViewRecycler, null, 0);
        try {
            //NOTE : SENT!
            String newMessageId = sendMessageToServer(messageContent, toUserId);
        }
        catch (Exception ex) {
            //NOTE : COULD NOT SEND MESSAGE!!
            ShowErrorToast(ex);
        }
    }

    private String sendMessageToServer(String messageContent, String toUserId) throws Exception {
        HashMap<String, String> params = new HashMap();
        params.put("content", messageContent);
        params.put("userid", toUserId);

        RequestHelper rh = new RequestHelper(getApplicationContext());
        return rh.sendRequest("/message/send", params, HttpMethods.POST, String.class);
    }

    private void deleteChatHistoryWithUser() {
        localDbManager.deleteChatHistoryWithUser(AppGlobals.chatUserId);
        FillMessagesListDataRecycler(new ArrayList<>());

        try {
            sendBlock();
            GetMesagesRedirect();
        }
        catch (Exception ex) {
            ShowErrorToast(ex);
        }
    }

    private Boolean sendBlock() throws Exception {
        HashMap<String, String> params = new HashMap();
        params.put("appuserid", AppGlobals.chatUserId);

        RequestHelper rh = new RequestHelper(getApplicationContext());
        return rh.sendRequest("/match/sendblock", params, HttpMethods.POST).success;
    }

    class DeleteFunctionPasser implements IDialogFunctionPasser {

        @Override
        public void passOK() {
            deleteChatHistoryWithUser();
        }

        @Override
        public void passCancel() {

        }

    }


}
