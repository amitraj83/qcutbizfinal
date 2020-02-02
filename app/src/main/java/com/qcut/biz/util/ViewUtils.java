package com.qcut.biz.util;

import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class ViewUtils {
    public static boolean notEmpty(TextView textView){
        if(textView != null && textView.getText() != null && textView.getText().toString() != null
                && textView.getText().toString().trim().length() != 0) {
            return true;
        }
        return false;
    }

    public static boolean notEmpty(Spinner spinner){
        if(spinner != null && spinner.getSelectedItem() != null
                && spinner.getSelectedItem().toString().length() != 0) {
            return true;
        }
        return false;
    }

    public static boolean notEmpty(EditText editText) {
        if(editText != null && editText.getText() != null
                && editText.getText().toString() != null
                && editText.getText().toString().trim().length() != 0) {
            return true;
        }
        return false;
    }

    public static int getDisplayHeight(WindowManager windowManager) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    public static int getDisplayWidth(WindowManager windowManager) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }


}
