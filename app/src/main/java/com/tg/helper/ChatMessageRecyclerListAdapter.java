package com.tg.helper;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.tg.VO.MatchUserVO;
import com.tg.dataManager.DistanceManager;
import com.tg.dataManager.MatchUsersDataManager;
import com.tg.derdoapp.BaseActivity;
import com.tg.derdoapp.NavigationBaseActivity;
import com.tg.derdoapp.R;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatMessageRecyclerListAdapter extends RecyclerView.Adapter<ChatMessageRecyclerListAdapter.ChatMessageViewHolder> {

    private List<UserMessageDBModel> list;

    private BaseActivity appContext;

    public ChatMessageRecyclerListAdapter(List<UserMessageDBModel> messages, BaseActivity appContextBase) {
        this.list = messages == null ? new ArrayList<>() : messages;
        this.appContext = appContextBase;
    }

    public static class ChatMessageViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public LinearLayout linearLayout;
        public ChatMessageViewHolder(LinearLayout v) {
            super(v);
            linearLayout = v;
        }
    }

    @Override
    public ChatMessageRecyclerListAdapter.ChatMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_content_item, parent, false);
        ChatMessageRecyclerListAdapter.ChatMessageViewHolder vh = new ChatMessageRecyclerListAdapter.ChatMessageViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ChatMessageRecyclerListAdapter.ChatMessageViewHolder holder, final int position) {

        final UserMessageDBModel relatedObject = list.get(position);
        LinearLayout v = holder.linearLayout;

        TableLayout contentHolder = v.findViewById(R.id.ci_tb_container);

        TextView messageContent = (TextView) v.findViewById(R.id.ci_text_message_body);
        messageContent.setText(relatedObject.messageContent);


        String strHours = new SimpleDateFormat("dd/MM/yy HH:mm").format(relatedObject.createdAt);
        TextView hourTextView = (TextView) v.findViewById(R.id.ci_hour_tv);
        hourTextView.setText(strHours);

        if(relatedObject.isMyMessage) {
            contentHolder.setBackground(ContextCompat.getDrawable(appContext, R.drawable.chat_message_item_sent_rectangle));
            v.setGravity(Gravity.RIGHT);
        }
        else {
            contentHolder.setBackground(ContextCompat.getDrawable(appContext, R.drawable.chat_message_item_received_rectangle));
            v.setGravity(Gravity.LEFT);
        }
    }

    //TODO : GO TO CHAT WITH THIS USER!

    @Override
    public int getItemCount() {
        return list.size();
    }

    public int addMessageToList(UserMessageDBModel umDBModel) {
        return addItem(umDBModel);
    }

    private int addItem(UserMessageDBModel umDBModel) {
        list.add(0, umDBModel);
        int position = 0;
        notifyItemInserted(position);
        notifyItemRangeChanged(position, list.size());

        return list.size();
    }

}
