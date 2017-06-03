package com.example.savch.dypproj.base;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by savch on 28.03.2017.
 * All rights are reserved.
 * If you will have any cuastion, please
 * contact via email (savchukndr@gmail.com)
 */

public class MySQLAdapter {
    private static final String DBNAME  = "DB_2"; //DB_r_11
    private static final String TABLE   = "user";
    public static final int    VERSION = 1;

    private SQLiteDatabase sqLiteDatabase;
    private SQLiteHelper sqLiteHelper;
    private Context mContext;

    private static final String CREATE_TABLE =
            "CREATE TABLE user ("
                    + "id_user INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                    + "name TEXT,"
                    + "surname TEXT,"
                    + "login TEXT,"
                    + "password TEXT);";

    public MySQLAdapter(Context context){
        mContext = context;
    }

    public void close() {
        sqLiteHelper.close();
    }

    public long insertUser(String nameVal, String surnameVal, String loginVal, String passwordVal) {
        ContentValues cv = new ContentValues();
        cv.put("name", nameVal);
        cv.put("surname", surnameVal);
        cv.put("login", loginVal);
        cv.put("password", passwordVal);
        return sqLiteDatabase.insert(TABLE, null, cv);
    }

    public MySQLAdapter openToRead() throws SQLException {
        try {
            sqLiteHelper = new SQLiteHelper(mContext, DBNAME, null, VERSION);
            sqLiteDatabase = sqLiteHelper.getReadableDatabase();
        } catch (Exception ignored){}
        return this;
    }

    public MySQLAdapter openToWrite() throws SQLException {
        try {
            sqLiteHelper = new SQLiteHelper(mContext, DBNAME, null, VERSION);
            sqLiteDatabase = sqLiteHelper.getWritableDatabase();
        } catch (Exception ignored){}
        return this;
    }

    public Cursor queueAll() {
        return sqLiteDatabase.rawQuery("SELECT * FROM user;", null);
    }

    public Cursor queueUserId(String email){
        return sqLiteDatabase.rawQuery("SELECT id_user FROM user WHERE email='" + email + "';", null);
    }

    private class SQLiteHelper extends SQLiteOpenHelper {
        SQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);

            ContentValues cv;
            cv = new ContentValues();
            cv.put("name", "Andrii");
            cv.put("surname", "Savchuk");
            cv.put("login", "savchukndr");
            cv.put("password", "savasava");
            db.insert(TABLE, null, cv);
            cv = new ContentValues();
            cv.put("name", "Olhai");
            cv.put("surname", "Peleshenko");
            cv.put("login", "pele");
            cv.put("password", "pelepele");
            db.insert(TABLE, null, cv);
        }

        public void onUpgrade(SQLiteDatabase db, int oldversion, int newversion) {}
    }

    //Date format
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy/MM/dd HH : mm : ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}
