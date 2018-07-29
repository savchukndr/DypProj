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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by savch on 28.03.2017.
 * All rights are reserved.
 * If you will have any cuastion, please
 * contact via email (savchukndr@gmail.com)
 */

public class SQLAdapter {
    private static final int VERSION = 1;
    private static final String DBNAME = "DB_2"; //DB_r_11
    private static final String TABLE = "employee";
    private static final String TABLE_AGREEMENT = "agreement";
    private static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS employee ("
                    + "employee_id TEXT PRIMARY KEY NOT NULL,"
                    + "name TEXT,"
                    + "surname TEXT,"
                    + "login TEXT,"
                    + "password TEXT);";
    private static final String CREATE_TABLE_AGREEMENT =
            "CREATE TABLE IF NOT EXISTS agreement ("
                    + "id_agreement TEXT PRIMARY KEY NOT NULL,"
                    + "title TEXT);";
    private SQLiteDatabase sqLiteDatabase;
    private SQLiteHelper sqLiteHelper;
    private Context mContext;

    public SQLAdapter(Context context) {
        mContext = context;
    }

    public void close() {
        sqLiteHelper.close();
    }

    public SQLAdapter openToWrite() throws SQLException {
        try {
            sqLiteHelper = new SQLiteHelper(mContext, DBNAME, null, VERSION);
            sqLiteDatabase = sqLiteHelper.getWritableDatabase();
        } catch (Exception ignored) {
        }
        return this;
    }

    public Cursor queueAll() {
        return sqLiteDatabase.rawQuery("SELECT * FROM employee;", null);
    }

    public Cursor queueAllAgreement() {
        return sqLiteDatabase.rawQuery("SELECT * FROM agreement;", null);
    }

    public void createTable() {
        sqLiteDatabase.rawQuery(CREATE_TABLE, null);
    }

    public void createTableAgreement() {
        sqLiteDatabase.rawQuery(CREATE_TABLE_AGREEMENT, null);
    }

    public void dropTable() {
        sqLiteDatabase.delete("employee", null, null);
    }

    public void dropTableAgreement() {
        sqLiteDatabase.delete("agreement", null, null);
    }

    public void insertUser(String jsonMessage) {
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, HashMap<String, String>>>() {
        }.getType();
        HashMap<String, HashMap<String, String>> jsonMap = gson.fromJson(jsonMessage, type);
        List<String> keyList = new ArrayList<>(jsonMap.keySet());
        Collections.sort(keyList);
        for (String key : keyList) {
            ContentValues cv = new ContentValues();
            Map<String, String> mapOfValues = jsonMap.get(key);
            cv.put("employee_id", key);
            cv.put("name", mapOfValues.get("name"));
            cv.put("surname", mapOfValues.get("surname"));
            cv.put("login", mapOfValues.get("login"));
            cv.put("password", mapOfValues.get("password"));
            sqLiteDatabase.insert(TABLE, null, cv);
        }
    }

    public void insertAgreement(String jsonMessage) {
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, String>>() {
        }.getType();
        HashMap<String, String> jsonMap = gson.fromJson(jsonMessage, type);
        List<String> keyList = new ArrayList<>(jsonMap.keySet());
        Collections.sort(keyList);
        for (String key : keyList) {
            ContentValues cv = new ContentValues();
            cv.put("id_agreement", key);
            cv.put("title", jsonMap.get(key));
            sqLiteDatabase.insert(TABLE_AGREEMENT, null, cv);
        }
    }

    private class SQLiteHelper extends SQLiteOpenHelper {
        SQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
            db.execSQL(CREATE_TABLE_AGREEMENT);
        }

        public void onUpgrade(SQLiteDatabase db, int oldversion, int newversion) {
        }
    }
}
