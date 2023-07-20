package com.tg.dataManager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.tg.helper.UserMessageDBModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class DbManager extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "exampleAppDb";
    private static final String TABLE_Messages = "messages";
    private static final String KEY_ID = "id";
    private static final String MESSAGE_ID = "messageId";
    private static final String KEY_USER_ID = "relatedUserId";
    private static final String KEY_MESSAGE_CONTENT = "messageContent";
    private static final String KEY_IS_MY_MESSAGE = "isMyMessage";
    private static final String KEY_IS_READ_BY_ME = "isReadByMe";
    private static final String KEY_CREATED_AT = "createdAt";

    private static final String DATE_FORMAT = "dd-MMM-yyyy HH:mm:ss";

    public DbManager(Context context){
        super(context,DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_Messages + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MESSAGE_ID + " TEXT,"
                + KEY_USER_ID + " TEXT,"
                + KEY_IS_MY_MESSAGE + " INTEGER,"
                + KEY_MESSAGE_CONTENT + " TEXT,"
                + KEY_CREATED_AT + " TEXT,"
                + KEY_IS_READ_BY_ME + " INTEGER"+ ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        // Drop older table if exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_Messages);
        // Create tables again
        onCreate(db);
    }

    public long insertUserMesssage(String messageId, String relatedUserId, String messageContent, Boolean isMyMessage) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cValues = new ContentValues();
        cValues.put(MESSAGE_ID, messageId);
        cValues.put(KEY_USER_ID, relatedUserId);
        cValues.put(KEY_MESSAGE_CONTENT, messageContent);
        cValues.put(KEY_IS_MY_MESSAGE, isMyMessage ? 1 : 0);
        cValues.put(KEY_IS_READ_BY_ME, isMyMessage ? 1 : 0);//NOTE : I Read my message

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        String formattedDate = df.format(c);

        cValues.put(KEY_CREATED_AT, formattedDate);

        long newRowId = db.insert(TABLE_Messages,null, cValues);
        return newRowId;
    }

    public void updateIsReadStatus(long id, Boolean isRead) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cVals = new ContentValues();
        cVals.put(KEY_IS_READ_BY_ME, isRead ? 1 : 0);
        db.update(TABLE_Messages, cVals, KEY_ID+" = ?",new String[]{String.valueOf(id)});
    }

    public void updateIsReadStatus(String userId, Boolean isRead) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cVals = new ContentValues();
        cVals.put(KEY_IS_READ_BY_ME, isRead ? 1 : 0);
        db.update(TABLE_Messages, cVals, KEY_USER_ID +" = ?",new String[]{String.valueOf(userId)});
    }

    public void deleteChatHistoryWithUser(String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_Messages, KEY_USER_ID +" = ?", new String[]{String.valueOf(userId)});
    }

    //TODO : LIMIT USER MESAGES
    public ArrayList<UserMessageDBModel> getMessagesRelatedTo(String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<UserMessageDBModel> resultList = new ArrayList<>();
        Cursor cursor = db.query(TABLE_Messages, new String[] { MESSAGE_ID, KEY_ID, KEY_USER_ID, KEY_IS_MY_MESSAGE, KEY_MESSAGE_CONTENT, KEY_CREATED_AT, KEY_IS_READ_BY_ME }, KEY_USER_ID+ "=?",new String[]{String.valueOf(userId)},null, null, KEY_ID +  " DESC", "20");
        while (cursor.moveToNext()) {

            UserMessageDBModel umdbm = new UserMessageDBModel();

            umdbm.id = cursor.getLong(cursor.getColumnIndex(KEY_ID));
            umdbm.messageId = cursor.getString(cursor.getColumnIndex(MESSAGE_ID));
            umdbm.relatedUserId = cursor.getString(cursor.getColumnIndex(KEY_USER_ID));
            umdbm.isMyMessage = cursor.getInt(cursor.getColumnIndex(KEY_IS_MY_MESSAGE)) == 0 ? false : true;
            umdbm.messageContent = cursor.getString(cursor.getColumnIndex(KEY_MESSAGE_CONTENT));
            try {
                umdbm.createdAt = new SimpleDateFormat(DATE_FORMAT).parse(cursor.getString(cursor.getColumnIndex(KEY_CREATED_AT)));
            }
            catch (Exception ex) {
                Log.e("Date.Parse.EX", ex.getMessage() != null ? ex.getMessage() : "NULL_MESSAGE");
            }

            umdbm.isReadByMe = cursor.getInt(cursor.getColumnIndex(KEY_IS_READ_BY_ME)) == 0 ? false : true;

            resultList.add(umdbm);
        }
        return  resultList;
    }

    public UserMessageDBModel getById(Long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        UserMessageDBModel result = null;
        Cursor cursor = db.query(TABLE_Messages, new String[] { MESSAGE_ID, KEY_ID, KEY_USER_ID, KEY_IS_MY_MESSAGE, KEY_MESSAGE_CONTENT, KEY_CREATED_AT, KEY_IS_READ_BY_ME }, KEY_ID+ "=?", new String[]{String.valueOf(id)},null, null, KEY_ID +  " DESC", "1");
        if (cursor.moveToNext()) {

            result = new UserMessageDBModel();

            result.id = cursor.getLong(cursor.getColumnIndex(KEY_ID));
            result.messageId = cursor.getString(cursor.getColumnIndex(MESSAGE_ID));
            result.relatedUserId = cursor.getString(cursor.getColumnIndex(KEY_USER_ID));
            result.isMyMessage = cursor.getInt(cursor.getColumnIndex(KEY_IS_MY_MESSAGE)) == 0 ? false : true;
            result.messageContent = cursor.getString(cursor.getColumnIndex(KEY_MESSAGE_CONTENT));
            try {
                result.createdAt = new SimpleDateFormat(DATE_FORMAT).parse(cursor.getString(cursor.getColumnIndex(KEY_CREATED_AT)));
            }
            catch (Exception ex) {
                Log.e("Date.Parse.EX", ex.getMessage() != null ? ex.getMessage() : "NULL_MESSAGE");
            }

            result.isReadByMe = cursor.getInt(cursor.getColumnIndex(KEY_IS_READ_BY_ME)) == 0 ? false : true;
        }

        return  result;
    }

    public HashMap<String, Integer> getUnreadMessages() {
        SQLiteDatabase db = this.getWritableDatabase();
        HashMap<String, Integer> userIdsNUnreadMessagesCount = new HashMap<>();
        String rawQ = "select count(" + KEY_USER_ID + ") as 'CNT', " + KEY_USER_ID + "  from " + TABLE_Messages + " where " + KEY_IS_READ_BY_ME + "=0 GROUP BY " + KEY_USER_ID;
        Log.i("RAW_Q", rawQ);
        Cursor cursor = db.rawQuery(rawQ, null);
        while (cursor.moveToNext()) {

            Integer unreadCount = cursor.getInt(cursor.getColumnIndex("CNT"));
            String relatedUserId = cursor.getString(cursor.getColumnIndex(KEY_USER_ID));

            userIdsNUnreadMessagesCount.put(relatedUserId, unreadCount);
        }

        return  userIdsNUnreadMessagesCount;
    }

    public ArrayList<UserMessageDBModel> getLatestMessagesOpt() {
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<UserMessageDBModel> resultList = new ArrayList<>();
       
        Cursor cursor = db.rawQuery("select * from " + TABLE_Messages + " where " + KEY_ID + " in (select max(" + KEY_ID + ") from " + TABLE_Messages + " group by " + KEY_USER_ID + ")", null);
        while (cursor.moveToNext()) {

            String relatedUserId = cursor.getString(cursor.getColumnIndex(KEY_USER_ID));

            Boolean userHasMessage = false;
            for (UserMessageDBModel indexedUM : resultList) {
                if(indexedUM.relatedUserId.equals(relatedUserId)) {
                    userHasMessage = true;
                    break;
                }
            }

            if(!userHasMessage) {
                UserMessageDBModel umdbm = new UserMessageDBModel();

                umdbm.id = cursor.getLong(cursor.getColumnIndex(KEY_ID));
                umdbm.messageId = cursor.getString(cursor.getColumnIndex(MESSAGE_ID));
                umdbm.relatedUserId = relatedUserId;
                umdbm.messageContent = cursor.getString(cursor.getColumnIndex(KEY_MESSAGE_CONTENT));
                umdbm.isReadByMe = cursor.getInt(cursor.getColumnIndex(KEY_IS_READ_BY_ME)) == 0 ? false : true;
                umdbm.isMyMessage = cursor.getInt(cursor.getColumnIndex(KEY_IS_MY_MESSAGE)) == 0 ? false : true;

                try {
                    umdbm.createdAt = new SimpleDateFormat(DATE_FORMAT).parse(cursor.getString(cursor.getColumnIndex(KEY_CREATED_AT)));
                }
                catch (Exception e) {
                    Log.e("ERROR_DB_MAN", e.getMessage() != null ? e.getMessage() : "ERROR_MESSAGE_NULL");
                }

                resultList.add(umdbm);
            }
        }

        for(UserMessageDBModel umdbm : resultList) {
            Log.w("USER_MESSAGE", "results.id : " + umdbm.id + " | relatedUserId : " + umdbm.relatedUserId);
        }

        return  resultList;
    }

    //TODO : OPTIMIZE THIS!!!
    public ArrayList<UserMessageDBModel> getLatestMessages() {
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<UserMessageDBModel> resultList = new ArrayList<>();
        Cursor cursor = db.query(TABLE_Messages, new String[] { KEY_ID, MESSAGE_ID, KEY_USER_ID, KEY_MESSAGE_CONTENT, KEY_IS_READ_BY_ME, KEY_IS_MY_MESSAGE, KEY_CREATED_AT }, null,null, null, null, KEY_CREATED_AT + " DESC", null);
        while (cursor.moveToNext()) {

            String relatedUserId = cursor.getString(cursor.getColumnIndex(KEY_USER_ID));

            Boolean userHasMessage = false;
            for (UserMessageDBModel indexedUM : resultList) {
                if(indexedUM.relatedUserId.equals(relatedUserId)) {
                    userHasMessage = true;
                    break;
                }
            }

            if(!userHasMessage) {
                UserMessageDBModel umdbm = new UserMessageDBModel();

                umdbm.id = cursor.getLong(cursor.getColumnIndex(KEY_ID));
                umdbm.messageId = cursor.getString(cursor.getColumnIndex(MESSAGE_ID));
                umdbm.relatedUserId = relatedUserId;
                umdbm.messageContent = cursor.getString(cursor.getColumnIndex(KEY_MESSAGE_CONTENT));
                umdbm.isReadByMe = cursor.getInt(cursor.getColumnIndex(KEY_IS_READ_BY_ME)) == 0 ? false : true;
                umdbm.isMyMessage = cursor.getInt(cursor.getColumnIndex(KEY_IS_MY_MESSAGE)) == 0 ? false : true;

                try {
                    umdbm.createdAt = new SimpleDateFormat(DATE_FORMAT).parse(cursor.getString(cursor.getColumnIndex(KEY_CREATED_AT)));
                }
                catch (Exception e) {
                    Log.e("ERROR_DB_MAN", e.getMessage() != null ? e.getMessage() : "ERROR_MESSAGE_NULL");
                }

                resultList.add(umdbm);
            }
        }

        for(UserMessageDBModel umdbm : resultList) {
            Log.w("USER_MESSAGE", "results.id : " + umdbm.id + " | relatedUserId : " + umdbm.relatedUserId);
        }

        return  resultList;
    }
}
