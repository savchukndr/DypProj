package com.example.savch.dypproj.session;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 5/5/2016.
 * All rights are reserved.
 * If you will have any cuastion, please
 * contact via email (savchukndr@gmail.com)
 */
public class Session {
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    public Session(Context ctx) {
        prefs = ctx.getSharedPreferences("DypProj", Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void setLogin(String login) {
        editor.putString("email", login);
        editor.commit();
    }

    public String getName() {
        return prefs.getString("name", null);
    }

    public void setName(String name) {
        editor.putString("name", name);
        editor.commit();
    }

    public String getSurName() {
        return prefs.getString("surName", null);
    }

    public void setSurName(String surName) {
        editor.putString("surName", surName);
        editor.commit();
    }

    public String getLoginl() {
        return prefs.getString("login", null);
    }

    public void setLoggedin(boolean logggedin) {
        editor.putBoolean("loggedInmode", logggedin);
        editor.commit();
    }

    public boolean loggedin() {
        return prefs.getBoolean("loggedInmode", false);
    }

    public void setFingerPrint(boolean fingerprint) {
        editor.putBoolean("fingerPrintMode", fingerprint);
        editor.commit();
    }
}
