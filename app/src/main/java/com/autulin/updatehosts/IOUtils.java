package com.autulin.updatehosts;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;



public class IOUtils {
    public static final String PREF_FIRST_TIME = "pref_first_time";
    public static final String PREF_STORE_DATA = "pref_store_data";
    public static final String PREF_IS_SHOW = "pref_is_show";
    public static final String FILE_PATH = Environment.getExternalStorageDirectory() + "/UpdateHosts/";

    public static boolean isShowTip(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_IS_SHOW, false);
    }

    public static void markNotShowTip(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_IS_SHOW, true).commit();
    }

    public static boolean isFirstTime(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_FIRST_TIME, false);
    }

    public static void markNotFirstTime(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_FIRST_TIME, true).commit();
    }

    public static void setDataToSP(final Context context, String data) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_STORE_DATA, data).commit();
    }

    public static String getDataFromSP(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_STORE_DATA, null);
    }

    public static String readStringFromRaw(Context context, int id){
        String line = null;
        StringBuffer stringBuffer = new StringBuffer();
        try {
            BufferedReader bufferReader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(id)));
            while ((line = bufferReader.readLine()) != null) {
                stringBuffer.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuffer.toString();
    }



}
