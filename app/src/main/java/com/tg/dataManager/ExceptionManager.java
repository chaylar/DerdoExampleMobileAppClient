package com.tg.dataManager;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionManager {

    public static void LogExceptionStackTrace(String tag, Exception e) {
        try {
            if(tag.length() >= 23) {
                tag = tag.substring(0, 22);
            }

            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.e(tag, exceptionAsString);
        }
        catch (Exception ee) {
            Log.e("LogExceptionStackTrace", ee.getMessage());
        }
    }

}
