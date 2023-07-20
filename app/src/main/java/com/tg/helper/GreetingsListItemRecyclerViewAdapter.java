package com.tg.helper;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.tg.VO.MatchUserVO;
import com.tg.VO.SendInteractionVO;
import com.tg.dataManager.DistanceManager;
import com.tg.dataManager.MatchUsersDataManager;
import com.tg.derdoapp.NavigationBaseActivity;
import com.tg.derdoapp.R;
import com.tg.requestManager.HttpMethods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GreetingsListItemRecyclerViewAdapter extends RecyclerView.Adapter<GreetingsListItemRecyclerViewAdapter.GreetingsListViewHolder> implements IGreetingsListItemEnabler {

    private List<MatchUserVO> list;

    private NavigationBaseActivity appContext;

    private List<ImageButton> okButtons;

    public GreetingsListItemRecyclerViewAdapter(List<MatchUserVO> greetingsMatchUserVOList, NavigationBaseActivity appContextBase) {
        this.list = greetingsMatchUserVOList;
        this.appContext = appContextBase;
        this.okButtons = new ArrayList<>();
    }

    @Override
    public void enableListItemOkButton() {
        for (int i = 0; i < okButtons.size(); i++) {
            okButtons.get(i).setEnabled(true);
            okButtons.get(i).setImageDrawable(ContextCompat.getDrawable(appContext, R.drawable.greet_0305));
            Log.w("AMK_AD", "enableListItemOkButton");
        }
    }

    @Override
    public void disableListItemOkButton() {
        for (int i = 0; i < okButtons.size(); i++) {
            okButtons.get(i).setEnabled(false);
            okButtons.get(i).setImageDrawable(ContextCompat.getDrawable(appContext, R.drawable.greetings_icon_grey));
            Log.w("AMK_AD", "enableListItemOkButton");
        }
    }

    public static class GreetingsListViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public LinearLayout linearLayout;
        public GreetingsListViewHolder(LinearLayout v) {
            super(v);
            linearLayout = v;
        }
    }

    @Override
    public GreetingsListItemRecyclerViewAdapter.GreetingsListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.helper_greetings_list_item, parent, false);

        GreetingsListItemRecyclerViewAdapter.GreetingsListViewHolder vh = new GreetingsListItemRecyclerViewAdapter.GreetingsListViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(GreetingsListItemRecyclerViewAdapter.GreetingsListViewHolder holder, final int position) {

        final MatchUserVO relatedObject = list.get(position);

        LinearLayout v = holder.linearLayout;

        CircleImageView imgView = (CircleImageView) v.findViewById(R.id.helper_greetings_list_item_profile_image);
        appContext.displayImageOnView(relatedObject.profilePictureUrl, imgView);
        imgView.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                try {
                    View customDialogView = LayoutInflater.from(appContext).inflate(R.layout.custom_image_dialog, null);
                    ImageView profileImageView = customDialogView.findViewById(R.id.custom_image_dialog_image_view);
                    appContext.displayImageOnView(relatedObject.profilePictureUrl, profileImageView);
                    new CustomImageDialog(appContext, customDialogView).show();
                }
                catch (Exception ex) {
                    appContext.ShowErrorToast(ex);
                }
            }
        });

        TextView userNameTextView = (TextView) v.findViewById(R.id.helper_greetings_list_item_user_name);
        userNameTextView.setText(relatedObject.userName + ", " + relatedObject.age);

        TextView distanceText = (TextView) v.findViewById(R.id.helper_greetings_list_item_distance);

        ImageView locIcon = (ImageView)v.findViewById(R.id.imageLocationIco);
        if(relatedObject.distance != null) {
            locIcon.setVisibility(View.VISIBLE);
            distanceText.setText(DistanceManager.GetDistanceForField(relatedObject.distance));
            distanceText.setVisibility(View.VISIBLE);
        }
        else {
            locIcon.setVisibility(View.INVISIBLE);
            distanceText.setText(null);
            distanceText.setVisibility(View.INVISIBLE);
        }

        final android.widget.ImageButton nokButton = v.findViewById(R.id.helper_greetings_list_item_nok_imgbutton);
        nokButton.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                try {
                    removeItem(position, relatedObject);
                    sendRejectToUser(relatedObject);
                }
                catch (Exception ex) {
                    appContext.ShowErrorToast(ex);
                }
            }
        });

        final android.widget.ImageButton okButton = v.findViewById(R.id.helper_greetings_list_item_ok_imgbutton);
        okButton.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                try {
                    SendInteractionVO resultVO = sendGreetToUser(relatedObject);
                    appContext.setGreetingsCount(resultVO.greetingsLeft);

                    if(resultVO.isSubscrptionRequired) {
                        appContext.RedirectToSubscription();
                        return;
                    }

                    removeItem(position, relatedObject);
                }
                catch (Exception ex) {
                    appContext.ShowErrorToast(ex);
                }

                appContext.showAdVideo();
            }
        });

        okButton.setImageDrawable(ContextCompat.getDrawable(appContext, R.drawable.greetings_icon_grey));
        okButton.setEnabled(false);
        okButtons.add(okButton);
    }

    private SendInteractionVO sendGreetToUser(MatchUserVO matchUserVO) throws Exception {
        HashMap<String, String> params = new HashMap();
        params.put("appuserid", matchUserVO.id);

        RequestHelper rh = new RequestHelper(appContext);
        SendInteractionVO resultVO = rh.sendRequest("/match/sendgreet", params, HttpMethods.POST, SendInteractionVO.class);

        return resultVO;
    }

    private Boolean sendRejectToUser(MatchUserVO matchUserVO) throws Exception {
        HashMap<String, String> params = new HashMap();
        params.put("appuserid", matchUserVO.id);

        RequestHelper rh = new RequestHelper(appContext);
        return rh.sendRequest("/match/sendreject", params, HttpMethods.POST).success;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void removeItem(int position, MatchUserVO matchUserVO) {
        list.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, list.size());

        MatchUsersDataManager.Instance().removeFromList(matchUserVO);
    }
}
