package com.live2d.demo.schedule;


import android.content.Context;
import android.content.SharedPreferences;

public class Utils {

    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_FIRST_LAUNCH = "isFirstLaunch";

    // 첫 실행 여부 확인
    public static boolean isFirstLaunch(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true);
    }

    // 첫 실행이 끝났음을 저장
    public static void setFirstLaunchDone(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();
    }
}
