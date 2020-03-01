package com.qcut.biz.util;

import android.util.Log;

import java.text.MessageFormat;

public class LogUtils {

    private static final String MY_ACTIVITY = "MYACTIVITY";

    public static void info(String msgPattern, Object... params) {
        Log.i(MY_ACTIVITY, formatMessage(msgPattern, params));
    }

    public static void debug(String msgPattern, Object... params) {
//        Log.d(MY_ACTIVITY, formatMessage(msgPattern, params));
    }

    public static void error(String msgPattern, Object... params) {
        Log.e(MY_ACTIVITY, formatMessage(msgPattern, params));
    }

    public static void error(String msgPattern, Throwable t, Object... params) {
        Log.e(MY_ACTIVITY, formatMessage(msgPattern, params), t);
    }

    private static String formatMessage(String msgPattern, Object[] params) {
        return MessageFormat.format(msgPattern, params);
    }
}
