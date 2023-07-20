package com.tg.dataManager;

import android.util.Log;

import com.tg.VO.MatchUserVO;

import java.util.ArrayList;
import java.util.List;

public class MatchUsersDataManager {

    private List<MatchUserVO> matchUsersList;

    private static MatchUsersDataManager single_instance = null;

    private MatchUsersDataManager()
    {
        matchUsersList = new ArrayList<>();
    }

    public static MatchUsersDataManager Instance()
    {
        if (single_instance == null)
        {
            single_instance = new MatchUsersDataManager();
        }

        return single_instance;
    }

    public void resetList() {
        matchUsersList = new ArrayList<>();
    }

    public Boolean hasAny() {
        return (matchUsersList != null && matchUsersList.size() > 0);
    }

    public MatchUserVO getNext() {
        if(matchUsersList == null || matchUsersList.size() <= 0) {
            return null;
        }

        Log.w("MatchUsersDM.getNext", matchUsersList.get(0).userName);
        return matchUsersList.get(0);
    }

    public Boolean addToList(List<MatchUserVO> matchUsers) {

        if(matchUsers == null || matchUsers.size() <= 0) {
            return false;
        }

        Log.w("MatchUsersDM.addToList", String.valueOf(matchUsers.size()));
        Log.w("MatchUsersDM.addToList", matchUsers.get(0).userName);

        matchUsersList.addAll(matchUsers);
        return true;
    }

    public void removeFromList(MatchUserVO matchUserVO) {

        if(matchUserVO == null) {
            return;
        }

        Log.w("MatchUDM.removeFromList", matchUserVO.id);
        Log.w("MatchUDM.removeFromList", matchUserVO.userName);

        int removeIndex = -1;
        for (int i = 0; i < matchUsersList.size(); i++) {
            MatchUserVO mvo = matchUsersList.get(i);

            Log.w("MatchUDM.mvoId", mvo.id);

            if(mvo.id.equals(matchUserVO.id)) {
                removeIndex = i;
                break;
            }
        }

        Log.w("MatchUDM.removeIndex", String.valueOf(removeIndex));

        if(removeIndex != -1) {
            matchUsersList.remove(removeIndex);
        }
    }
}
