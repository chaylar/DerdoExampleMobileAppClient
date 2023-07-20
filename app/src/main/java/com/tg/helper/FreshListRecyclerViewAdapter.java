package com.tg.helper;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.tg.VO.SendInteractionVO;
import com.tg.dataObject.FreshListDataObject;
import com.tg.derdoapp.ChatActivity;
import com.tg.derdoapp.NavigationBaseActivity;
import com.tg.derdoapp.R;
import com.tg.globals.AppGlobals;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FreshListRecyclerViewAdapter extends RecyclerView.Adapter<FreshListRecyclerViewAdapter.FreshListAdapterViewHolder> {

    private List<FreshListDataObject> list;

    private NavigationBaseActivity navBaseActivity;

    public static class FreshListAdapterViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TableLayout tableLayout;
        public FreshListAdapterViewHolder(TableLayout v) {
            super(v);
            tableLayout = v;
        }
    }

    public FreshListRecyclerViewAdapter(NavigationBaseActivity baseActivity, List<FreshListDataObject> freshListData) {
        this.list = freshListData;
        this.navBaseActivity = baseActivity;
    }

    @Override
    public FreshListRecyclerViewAdapter.FreshListAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        TableLayout v = (TableLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.helper_messages_fresh_list, parent, false);

        FreshListAdapterViewHolder vh = new FreshListAdapterViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(FreshListAdapterViewHolder holder, int position) {
        //holder.textView.setText(mDataset[position]);

        FreshListDataObject relatedObject = list.get(position);
        TableLayout v = holder.tableLayout;

        v.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                try {
                    redirectToChatActivity(relatedObject.userId, relatedObject.profileImageUrl, relatedObject.name + ", " + relatedObject.age, relatedObject.nuisanceTypeName);
                }
                catch (Exception ex) {

                    navBaseActivity.ShowErrorToast(ex);
                }
            }
        });

        CircleImageView imgView = (CircleImageView) v.findViewById(R.id.helper_fresh_list_profile_img);
        navBaseActivity.displayImageOnView(relatedObject.profileImageUrl, imgView);

        TextView userNameTextView = (TextView) v.findViewById(R.id.hmfl_user_name);
        userNameTextView.setText(relatedObject.name);

        //TextView distanceTextView = (TextView) v.findViewById(R.id.hmfl_distance);
        //distanceTextView.setText(relatedObject.distanceText);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return list.size();
    }

    private void redirectToChatActivity(String userId, String profileImageUrl, String userNameNAge, String nuisanceTypeName) {
        AppGlobals.chatUserId = userId;
        AppGlobals.chatUserProfileImage = profileImageUrl;
        AppGlobals.chatUserUserName = userNameNAge;
        AppGlobals.chatUserNuisanceTypeName = nuisanceTypeName;

        android.content.Intent redirect = new android.content.Intent(navBaseActivity, ChatActivity.class);
        navBaseActivity.startActivity(redirect);
    }

}
