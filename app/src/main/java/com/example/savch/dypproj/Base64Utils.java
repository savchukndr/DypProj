package com.example.savch.dypproj;

import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by savch on 12.06.2017.
 * All rights are reserved.
 * If you will have any cuastion, please
 * contact via email (savchukndr@gmail.com)
 */

public class Base64Utils {

    private static final String TAG = "Base64Utils";
    private String picturePath;
    private String base64;

    public Base64Utils(String picturePath) {
        this.picturePath = picturePath;
    }

    public String getPicturePath() {
        return picturePath;
    }
    public void setPicturePath(String picturePath) {
        this.picturePath = picturePath;
    }

    public String getBase64() {
        FileInputStream fis11=null;
        try {
            fis11 = new FileInputStream(picturePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream bos11 = new ByteArrayOutputStream();
        byte[] buf = new byte[8096];
        try {
            for (int readNum; (readNum = fis11.read(buf)) != -1;) {
                bos11.write(buf, 0, readNum);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        byte[] bytes = bos11.toByteArray();
        base64 = Base64.encodeToString(bytes, Base64.DEFAULT);
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }
}
