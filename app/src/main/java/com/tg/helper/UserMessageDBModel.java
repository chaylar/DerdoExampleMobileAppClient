package com.tg.helper;

import java.util.Date;

public class UserMessageDBModel {

    public long id;

    public String messageId;

    public String relatedUserId;

    public String messageContent;

    public Boolean isMyMessage;

    public Boolean isReadByMe;

    public Date createdAt;

}
