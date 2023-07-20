package com.tg.helper;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.tg.VO.MatchUserVO;
import com.tg.VO.SendInteractionVO;
import com.tg.dataManager.DistanceManager;
import com.tg.dataManager.MatchUsersDataManager;
import com.tg.derdoapp.ChatActivity;
import com.tg.derdoapp.NavigationBaseActivity;
import com.tg.derdoapp.R;
import com.tg.globals.AppGlobals;
import com.tg.requestManager.HttpMethods;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesListItemRecyclerViewAdapter extends RecyclerView.Adapter<MessagesListItemRecyclerViewAdapter.MessageListViewHolder> {

    private List<MatchUserMessageContainer> list;

    private NavigationBaseActivity appContext;

    public MessagesListItemRecyclerViewAdapter(List<MatchUserMessageContainer> messages, NavigationBaseActivity appContextBase) {
        this.list = messages;
        this.appContext = appContextBase;
    }

    public static class MessageListViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public LinearLayout linearLayout;
        public MessageListViewHolder(LinearLayout v) {
            super(v);
            linearLayout = v;
        }
    }

    @Override
    public MessagesListItemRecyclerViewAdapter.MessageListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.helper_messages_list_item, parent, false);

        MessagesListItemRecyclerViewAdapter.MessageListViewHolder vh = new MessagesListItemRecyclerViewAdapter.MessageListViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MessagesListItemRecyclerViewAdapter.MessageListViewHolder holder, final int position) {

        final MatchUserMessageContainer relatedObject = list.get(position);
        MatchUserVO relatedUser = relatedObject.matchUser;
        UserMessageDBModel relatedMessage = relatedObject.userMessageModel;

        LinearLayout v = holder.linearLayout;

        CircleImageView imgView = (CircleImageView) v.findViewById(R.id.hmli_profile_picture);
        appContext.displayImageOnView(relatedUser.profilePictureUrl, imgView);

        TextView userNameTextView = (TextView) v.findViewById(R.id.hmli_user_name);
        userNameTextView.setText(relatedUser.userName + ", " + relatedUser.age);

        //TextView distanceText = (TextView) v.findViewById(R.id.hmli_distance_text);

        /*ImageView locIcon = (ImageView)v.findViewById(R.id.imageLocationIco);
        if(relatedUser.distance != null) {
            locIcon.setVisibility(View.VISIBLE);
            distanceText.setText(DistanceManager.GetDistanceForField(relatedUser.distance));
            distanceText.setVisibility(View.VISIBLE);
        }
        else {
            locIcon.setVisibility(View.INVISIBLE);
            distanceText.setText(null);
            distanceText.setVisibility(View.INVISIBLE);
        }*/

        TextView messageContentTextView = (TextView) v.findViewById(R.id.hmli_message_text);
        messageContentTextView.setText(relatedMessage.messageContent);

        v.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                try {
                    redirectToChatActivity(relatedUser.id, relatedUser.profilePictureUrl, relatedUser.userName + ", " + relatedUser.age, relatedUser.nuisanceTypeName);
                }
                catch (Exception ex) {
                    appContext.ShowErrorToast(ex);
                }
            }
        });

        ImageView sentIcon = (ImageView)v.findViewById(R.id.hml_sent_icon);
        if(relatedMessage.isMyMessage) {
            sentIcon.setVisibility(View.VISIBLE);
        }
        else {
            sentIcon.setVisibility(View.GONE);
        }

        TextView unreadText = v.findViewById(R.id.hmli_unread_count);
        if(relatedObject.unreadMessagesCount > 0) {

            String unreadMessageCountStr;
            if(relatedObject.unreadMessagesCount > 99) {
                unreadMessageCountStr = "99+";
            }
            else {
                unreadMessageCountStr = String.valueOf(relatedObject.unreadMessagesCount);
            }

            unreadText.setText(unreadMessageCountStr);
            unreadText.setVisibility(View.VISIBLE);
        }
        else {
            unreadText.setVisibility(View.INVISIBLE);
        }
    }

    //TODO : GO TO CHAT WITH THIS USER!

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void redirectToChatActivity(String userId, String profileImageUrl, String userNameNAge, String nuisanceTypeName) {
        AppGlobals.chatUserId = userId;
        AppGlobals.chatUserProfileImage = profileImageUrl;
        AppGlobals.chatUserUserName = userNameNAge;
        AppGlobals.chatUserNuisanceTypeName = nuisanceTypeName;

        android.content.Intent redirect = new android.content.Intent(appContext, ChatActivity.class);
        appContext.startActivity(redirect);
    }

}
