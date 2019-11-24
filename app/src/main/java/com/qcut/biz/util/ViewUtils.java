package com.qcut.biz.util;

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
}
