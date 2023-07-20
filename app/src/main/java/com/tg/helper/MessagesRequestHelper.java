package com.tg.helper;

import com.tg.VO.MessageListVO;
import com.tg.VO.MessageVO;
import com.tg.dataManager.DbManager;
import com.tg.derdoapp.NavigationBaseActivity;
import com.tg.requestManager.HttpMethods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// UPDATE UI ELEMENTS!!
public class MessagesRequestHelper extends RequestHelper {

    public MessagesRequestHelper(NavigationBaseActivity currentActivity){
        super(currentActivity.getBaseContext());
    }

    public int fetchMessagesNSaveReturnCount() throws Exception {
        MessageListVO messagesListVo = getUnreadMessages();
        int result = saveMessagesToLocalDB(messagesListVo);

        if(result <= 0) {
            return result;
        }

        List<String> messageIds = new ArrayList<>();
        for(MessageVO mvo : messagesListVo.messageUsersList) {
            messageIds.add(mvo.id);
        }

        markMessagesAsRead(messageIds);

        //TODO : UPDATE UI ELEMENTS!!!
        return result;
    }

    protected int saveMessagesToLocalDB(MessageListVO messageListVO) {
        List<MessageVO> messages = messageListVO.messageUsersList;
        DbManager dbMan = new DbManager(this.appContext);
        List<Long> idsList = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            MessageVO messageVO = messages.get(i);
            long localDbId = dbMan.insertUserMesssage(messageVO.id, messageVO.fromUserId, messageVO.message, false);
            if(localDbId > 0) {
                idsList.add(localDbId);
            }
        }

        return idsList.size();
    }

    protected MessageListVO getUnreadMessages() throws Exception {
        MessageListVO resultVO = sendRequest("/message/getunreadmessages", null, HttpMethods.GET, MessageListVO.class);
        return resultVO;
    }

    protected MessageListVO markMessagesAsRead(List<String> messageIds) throws Exception {

        //String ids = String.join(",", messageIds);
        String ids = "";
        for(String s : messageIds) {
            ids += (s + ",");
        }

        ids = ids.substring(0, ids.length() - 1);

        HashMap<String, String> params = new HashMap();
        params.put("ids", ids);

        MessageListVO resultVO = sendRequest("/message/markasread", params, HttpMethods.POST, MessageListVO.class);

        return resultVO;
    }

}
