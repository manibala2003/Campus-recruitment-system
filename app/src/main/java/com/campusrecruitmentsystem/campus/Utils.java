package com.campusrecruitmentsystem.campus;

import android.content.Context;
import android.net.ConnectivityManager;

public class Utils {

    public static enum AccountType{
        STUDENT, COMPANY, ADMIN;
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}
