package com.example.lostfound.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Small wrapper around SharedPreferences for settings that must survive
 * app restarts (remember-me flag and last used email).
 */
public class PrefsManager {

    private static final String PREF_NAME = "lost_found_prefs";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_LAST_EMAIL = "last_email";

    private final SharedPreferences prefs;

    public PrefsManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isRememberMe() {
        return prefs.getBoolean(KEY_REMEMBER_ME, true);
    }

    public void setRememberMe(boolean value) {
        prefs.edit().putBoolean(KEY_REMEMBER_ME, value).apply();
    }

    public String getLastEmail() {
        return prefs.getString(KEY_LAST_EMAIL, "");
    }

    public void setLastEmail(String email) {
        prefs.edit().putString(KEY_LAST_EMAIL, email).apply();
    }
}
