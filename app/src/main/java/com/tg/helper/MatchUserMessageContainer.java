package com.tg.helper;

import android.util.Log;

import com.tg.VO.MatchUserVO;

public class MatchUserMessageContainer implements Comparable<MatchUserMessageContainer> {

    public MatchUserVO matchUser;

    public UserMessageDBModel userMessageModel;

    public Integer unreadMessagesCount;

    @Override
    public int compareTo(MatchUserMessageContainer o) {
        try {
            if (userMessageModel == null) {
                return -1;
            } else if (o.userMessageModel == null) {
                return 1;
            }

            return userMessageModel.createdAt.compareTo(o.userMessageModel.createdAt);
        }
        catch (Exception e) {
            Log.e("MUMC_E", e.getMessage() != null ? e.getMessage() : "MESSAGE NULL");
        }

        return -1;
    }

}
