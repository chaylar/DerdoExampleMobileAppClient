package com.tg.derdoapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tg.VO.MatchGetInfoVO;
import com.tg.VO.MatchUserVO;
import com.tg.dataManager.MatchUsersDataManager;
import com.tg.dataObject.NavActivity;
import com.tg.helper.GreetingsListItemRecyclerViewAdapter;
import com.tg.helper.RequestHelper;
import com.tg.requestManager.HttpMethods;

import java.util.HashMap;
import java.util.List;

public class GreetingsActivity extends NavigationBaseActivity {

    public GreetingsActivity() {
        super(NavActivity.GREETINGS, R.layout.activity_greetings);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*Button activeSocialButton = findViewById(R.id.social_greetings_button);
        setSocialNavButtonActive(activeSocialButton);*/

        //setGreetingsCount();

        doGreetingsActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchResetTimerValueAndSet();
    }

    private void doGreetingsActions() {

        MatchGetInfoVO matchGetInfoVO = null;
        try {
            matchGetInfoVO = getWaiting();
        }
        catch (Exception ex) {
            ShowErrorToast(ex);
            return;
        }

        setGreetingsCount(matchGetInfoVO.greetingsLeft);

        RecyclerView greetingsListViewRecycler = findViewById(R.id.greetings_list_view_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        greetingsListViewRecycler.setLayoutManager(layoutManager);

        List<MatchUserVO> matchUsers = matchGetInfoVO.matchUsers;
        GreetingsListItemRecyclerViewAdapter greetingsViewAdapter = new GreetingsListItemRecyclerViewAdapter(matchUsers, this);

        greetingsListViewRecycler.setAdapter(greetingsViewAdapter);

        loadVideoAd(greetingsViewAdapter);
    }

    private MatchGetInfoVO getWaiting() throws Exception {
        RequestHelper rh = new RequestHelper(getApplicationContext());
        MatchGetInfoVO resultVO = rh.sendRequest("/match/getwaiting", null, HttpMethods.GET, MatchGetInfoVO.class);

        return resultVO;
    }
}
