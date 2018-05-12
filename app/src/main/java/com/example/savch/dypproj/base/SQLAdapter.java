package com.example.savch.dypproj.base;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by savch on 28.03.2017.
 * All rights are reserved.
 * If you will have any cuastion, please
 * contact via email (savchukndr@gmail.com)
 */

public class SQLAdapter {
    private static final String DBNAME  = "DB_2"; //DB_r_11
    private static final String TABLE   = "employee";
    public static final int    VERSION = 1;
    private Map<String, String> mapOfValues;

    private SQLiteDatabase sqLiteDatabase;
    private SQLiteHelper sqLiteHelper;
    private Context mContext;

    private static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS employee ("
                    + "employee_id TEXT PRIMARY KEY NOT NULL,"
                    + "name TEXT,"
                    + "surname TEXT,"
                    + "login TEXT,"
                    + "password TEXT);";

    public SQLAdapter(Context context){
        mContext = context;
    }

    public void close() {
        sqLiteHelper.close();
    }

    public SQLAdapter openToRead() throws SQLException {
        try {
            sqLiteHelper = new SQLiteHelper(mContext, DBNAME, null, VERSION);
            sqLiteDatabase = sqLiteHelper.getReadableDatabase();
        } catch (Exception ignored){}
        return this;
    }

    public SQLAdapter openToWrite() throws SQLException {
        try {
            sqLiteHelper = new SQLiteHelper(mContext, DBNAME, null, VERSION);
            sqLiteDatabase = sqLiteHelper.getWritableDatabase();
        } catch (Exception ignored){}
        return this;
    }

    public Cursor queueAll() {
        return sqLiteDatabase.rawQuery("SELECT * FROM employee;", null);
    }

//    public Cursor queueUserId(String email){
//        return sqLiteDatabase.rawQuery("SELECT id_user FROM user WHERE email='" + email + "';", null);
//    }

    public void createTable(){
        sqLiteDatabase.rawQuery(CREATE_TABLE, null);
    }

    public void dropTable(){
        sqLiteDatabase.rawQuery("DROP TABLE IF EXISTS employee;", null);
    }

    public void insertUser(String jsonMessage) {
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, HashMap<String, String>>>(){}.getType();
        HashMap<String, HashMap<String, String>> jsonMap = gson.fromJson(jsonMessage, type);
        List<String> keyList = new ArrayList<>(jsonMap.keySet());
        Collections.sort(keyList);
        for (String key: keyList) {
            ContentValues cv = new ContentValues();
            mapOfValues = jsonMap.get(key);
            cv.put("employee_id", key);
            cv.put("name", mapOfValues.get("name"));
            cv.put("surname", mapOfValues.get("surname"));
            cv.put("login", mapOfValues.get("login"));
            cv.put("password", mapOfValues.get("password"));
            sqLiteDatabase.insert(TABLE, null, cv);
        }
    }

    private class SQLiteHelper extends SQLiteOpenHelper {
        SQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS employee;");
            db.execSQL(CREATE_TABLE);
//            ContentValues cv;
//            cv = new ContentValues();
//            cv.put("name", "Andrii");
//            cv.put("surname", "Savchuk");
//            cv.put("login", "savchukndr");
//            cv.put("password", "savasava");
//            db.insert(TABLE, null, cv);
//            cv = new ContentValues();
//            cv.put("name", "Olhai");
//            cv.put("surname", "Peleshenko");
//            cv.put("login", "pele");
//            cv.put("password", "pelepele");
//            db.insert(TABLE, null, cv);
        }

        public void onUpgrade(SQLiteDatabase db, int oldversion, int newversion) {
        }
    }

    //Date format
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy/MM/dd HH : mm : ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}
