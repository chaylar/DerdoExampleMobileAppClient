package com.tg.derdoapp;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;

import com.tg.VO.FriendsVO;
import com.tg.VO.MatchUserVO;
import com.tg.VO.UserMessageVO;
import com.tg.dataManager.DbManager;
import com.tg.dataManager.DistanceManager;
import com.tg.dataObject.FreshListDataObject;
import com.tg.dataObject.NavActivity;
import com.tg.helper.FreshListRecyclerViewAdapter;
import com.tg.helper.MatchUserMessageContainer;
import com.tg.helper.MessagesRequestHelper;
import com.tg.helper.MessagesListItemRecyclerViewAdapter;
import com.tg.helper.RequestHelper;
import com.tg.helper.UserMessageDBModel;
import com.tg.requestManager.HttpMethods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MessagesActivity extends NavigationBaseActivity {

    public MessagesActivity() {
        super(NavActivity.MESSAGES, R.layout.activity_messages);
    }

    private DbManager localDbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            MessagesRequestHelper mh = new MessagesRequestHelper(this);
            mh.fetchMessagesNSaveReturnCount();
        }
        catch (Exception ex) {
            //Log.e("MS_FR_LIST", ex.getMessage() != null ? ex.getMessage() : "EX NULL");
            ShowErrorToast(ex);
        }

        localDbManager = new DbManager(this);

        //TODO : Messages redirect | Add this to each item on messages list
        //View messageItemView = findViewById(R.id.message_test_1);
        //MessageItemOnClick(messageItemView);
        //
        doMessageActivityActions();
    }

    private void doMessageActivityActions() {

        List<MatchUserVO> friendsList = null;

        //TODO : UNREAD MESSAGES!
        //TODO : MOVE TO A GLOBAL LOCATION!
        try {
            FriendsVO friendsVO = getFriends();
            friendsList = friendsVO.matchUsers;
        }
        catch (Exception ex) {
            Log.e("MS_FR_LIST", ex.getMessage() != null ? ex.getMessage() : "EX NULL");
            //ShowErrorToast(ex);
        }

        List<MatchUserVO> freshList = new ArrayList<>();
        List<MatchUserMessageContainer> messagesLlist = new ArrayList<>();
        HashMap<String, Integer> unreadMap = localDbManager.getUnreadMessages();

        for(String s : unreadMap.keySet()) {
            Log.i("RAW_Q_SET", s + " | " + unreadMap.get(s));
        }

        List<UserMessageDBModel> lastMessages = localDbManager.getLatestMessagesOpt();
        for (int i = 0; i < friendsList.size(); i++) {
            MatchUserVO currentFriend = friendsList.get(i);
            UserMessageDBModel friendLastMessage = null;
            for (int li = 0; li < lastMessages.size(); li++) {
                UserMessageDBModel lm = lastMessages.get(li);
                if(currentFriend.id.equals(lm.relatedUserId)) {
                    friendLastMessage = lm;
                    break;
                }
            }

            if(friendLastMessage != null) {
                MatchUserMessageContainer mumC = new MatchUserMessageContainer();
                mumC.matchUser = currentFriend;
                mumC.userMessageModel = friendLastMessage;

                int unreadMessageCount = 0;
                if(unreadMap.containsKey(currentFriend.id)) {
                    unreadMessageCount = unreadMap.get(currentFriend.id);
                }

                mumC.unreadMessagesCount = unreadMessageCount;

                messagesLlist.add(mumC);
            }
            else {
                freshList.add(currentFriend);
            }
        }

        /*
        Comparator<MatchUserMessageContainer> compareByCreatedAt = (MatchUserMessageContainer o1, MatchUserMessageContainer o2) ->
                o2.userMessageModel.createdAt.compareTo( o1.userMessageModel.createdAt );

        Collections.sort(messagesLlist, compareByCreatedAt);
        Collections.sort(messagesLlist, Collections.reverseOrder());
        */

        //TODO : FETCH MESSAGES FROM LOCAL DB!
        FillFreshListDataRecycler(freshList);//TODO : SEND UNMESSAGED FRIENDS TO THIS
        FillMessagesListDataRecycler(messagesLlist);

        if(friendsList == null || friendsList.size() <= 0) {
            TableLayout noContentTable = findViewById(R.id.no_content_result_ll);
            noContentTable.setVisibility(View.VISIBLE);

            TableLayout allContentHolder = findViewById(R.id.allContentHolder);
            allContentHolder.setVisibility(View.GONE);
        }
    }

    private void FillMessagesListDataRecycler(List<MatchUserMessageContainer> userMessages) {

        if(userMessages == null || userMessages.size() <= 0) {
            return;
        }

        Collections.sort(userMessages);

        RecyclerView messagesListViewRecycler = findViewById(R.id.messages_list_view_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, true);
        messagesListViewRecycler.setLayoutManager(layoutManager);

        MessagesListItemRecyclerViewAdapter greetingsViewAdapter = new MessagesListItemRecyclerViewAdapter(userMessages, this);

        messagesListViewRecycler.setAdapter(greetingsViewAdapter);
    }

    private void FillFreshListDataRecycler(List<MatchUserVO> friendsList) {

        if(friendsList == null || friendsList.size() <= 0) {
            return;
        }

        RecyclerView freshListViewItem = (RecyclerView)findViewById(R.id.messages_ac_fresh_listview);
        freshListViewItem.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        freshListViewItem.setLayoutManager(layoutManager);

        List<FreshListDataObject> freshList = new ArrayList<>();
        for(int i = 0; i < friendsList.size(); i++) {

            MatchUserVO muvo = friendsList.get(i);

            FreshListDataObject dob = new FreshListDataObject();
            dob.userId = muvo.id;
            dob.age = muvo.age;
            dob.locationName = "Beyoğlu";//TODO :
            dob.profileImageUrl = muvo.profilePictureUrl;
            dob.name = muvo.userName;
            dob.distanceText = DistanceManager.GetDistanceForField( muvo.distance);
            dob.nuisanceTypeName = muvo.nuisanceTypeName;

            freshList.add(dob);
        }

        FreshListRecyclerViewAdapter itemsAdapter = new FreshListRecyclerViewAdapter(this, freshList);
        freshListViewItem.setAdapter(itemsAdapter);
    }

    private void onFreshListItemSelect() {
        //TODO :
    }

    private void MessageItemOnClick(View v) {
        v.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                GetMesagesRedirect();
            }
        });
    }

    private FriendsVO getFriends() throws Exception {
        RequestHelper rh = new RequestHelper(getApplicationContext());
        FriendsVO resultVO = rh.sendRequest("/message/getfriends", null, HttpMethods.GET, FriendsVO.class);

        return resultVO;
    }

    private void GetMesagesRedirect() {
        RedirectTo(ChatActivity.class);
    }
}
