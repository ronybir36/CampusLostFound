package com.example.lostfound.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

/** Shared helpers for messages and launching other apps. */
public final class Ui {

    private Ui() { }

    public static void snack(Activity activity, String message) {
        View root = activity.findViewById(android.R.id.content);
        Snackbar.make(root, message, Snackbar.LENGTH_LONG).show();
    }

    /** Starts an implicit intent and shows a message if no app can handle it. */
    public static void startSafely(Activity activity, Intent intent) {
        try {
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            snack(activity, "No app found to handle this action");
        }
    }

    public static String errorText(Exception e) {
        if (e == null || e.getMessage() == null) {
            return "Unknown error";
        }
        return e.getMessage();
    }
}
