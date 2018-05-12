package com.example.savch.dypproj.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.savch.dypproj.MainActivity;
import com.example.savch.dypproj.R;
import com.example.savch.dypproj.base.SQLAdapter;
import com.example.savch.dypproj.session.Session;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    private static final int RC_SIGN_IN = 9001;
    final String LOG_TAG = "LOG_AAAA";
    SQLAdapter dbHelper;
    Context context;
    private EditText _loginText;
    private EditText _passwordText;
    private Session session;
    private String userName;
    private String userSurName;
    private JSONObject json;
    private Handler mHandler;
    private static final int SERVER_PORT = 1994;
    private static final String SERVER_IP = "192.168.43.16";
    private final int CLIENT_SERVER_PORT = 1996;
    private String jsonMessageFromServer = "";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mHandler = new Handler();

        class SocketThread implements Runnable {
            @Override
            public void run() {
                try {
                    greetingWithServer();
                    receiveMessage();
                    Log.d(LOG_TAG, "Greating done.");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            private void greetingWithServer() throws JSONException {
                json = new JSONObject();
                json.put("updateUserDb", "true");
                //TODO: receive updateed SQL data
                json.put("greeting", "Hello server!");
                String jsonSTR = json.toString();
                Socket socket;
                byte[] jsonByteArr = jsonSTR.getBytes();
                try {
                    socket = new Socket(SERVER_IP, SERVER_PORT);
                    DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
                    outStream.writeInt(jsonByteArr.length);
                    outStream.write(jsonByteArr);
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            private void receiveMessage(){
                try {
                    ServerSocket socket = new ServerSocket(CLIENT_SERVER_PORT);
                    Socket clientSocket = socket.accept();
                    DataInputStream inStream = new DataInputStream(clientSocket.getInputStream());
                    int length = inStream.readInt();
                    if(length>0) {
                        byte[] message = new byte[length];
                        inStream.readFully(message, 0, message.length); // read the message
                        jsonMessageFromServer = new String(message);

                        //open and insert into employee table
                        dbHelper.openToWrite();
                        dbHelper.dropTable();
                        dbHelper.createTable();
                        dbHelper.insertUser(jsonMessageFromServer);


                        ////////
//                        Gson gson = new Gson();
//                        Type type = new TypeToken<HashMap<String, HashMap<String, String>>>(){}.getType();
//                        HashMap<String, HashMap<String, String>> myMap = gson.fromJson(jsonMessageFromServer, type);
//                        Log.d(LOG_TAG, "nnn " + String.valueOf(myMap));
                    }
//                    Log.d(LOG_TAG, jsonMessageFromServer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
//            private void jsonHandler() throws JSONException {
//                json = new JSONObject(jsonMessageFromServer);
//            }
        }

        session = new Session(this);
        if (session.loggedin()) {
            Intent mainIntent = new Intent(this, MainActivity.class);
            startActivity(mainIntent);
            finish();
        }

        Button _loginButton = (Button) findViewById(R.id.btn_login);
        _loginText = (EditText) findViewById(R.id.input_login);
        _passwordText = (EditText) findViewById(R.id.input_password);
        //*****************************************************************************
        //Login button listener
        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });


        dbHelper = new SQLAdapter(this);
        SocketThread socket = new SocketThread();
        Thread thread = new Thread(socket);
        thread.start();
//        try {
//            thread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


        ProgressDialog mConnectionProgressDialog = new ProgressDialog(this);
        mConnectionProgressDialog.setMessage("Signing in...");



    }


    //********************************************************************************
    //login with Login Button
    public void login() {
        // Log.d(TAG, "Login");

        /*if (!validate()) {
            onLoginFailed();
            return;
        }*/

        //_loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        final String login = _loginText.getText().toString();
        final String password = _passwordText.getText().toString();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed

                        if (userAutentification(login, password)) {
                            onLoginSuccess(login, userName, userSurName);
                        } else {
                            onLoginFailed();
                            // onLoginFailed();
                        }
                        progressDialog.dismiss();
                    }
                }, 3000);
    }
    //******************************************************************************


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {

        }
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess(String login, String name, String surname) {

        session.setLoggedin(true);
        session.setLogin(login);
        session.setName(name);
        session.setSurName(surname);
        Intent intentIntro = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intentIntro);
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

    }

    //*************************************************************************************
    /*public boolean validate() {
        boolean valid = true;

        String password = _passwordText.getText().toString();

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }*/
    //*************************************************************************************

    public Boolean userAutentification(String login, String password) {
        dbHelper.openToWrite();

        // Setting up the cursor which points to the desired table
        Cursor cursor = dbHelper.queueAll();

        Boolean records_Exist = false;

        // Checking if the table has values other than the header using the cursor
        if (cursor != null && cursor.getCount() > 0) {
            // Moving the cursor to the first row in the table
            cursor.moveToFirst();

            do {
                if (cursor.getString(cursor.getColumnIndex("login")).equals(login)) {
                    try {
                        if (AESCrypt.decrypt(cursor.getString(cursor.getColumnIndex("password"))).equals(password)) {
                            records_Exist = true;
                            userName = cursor.getString(cursor.getColumnIndex("name"));
                            userSurName = cursor.getString(cursor.getColumnIndex("surname"));
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //}

            } while (cursor.moveToNext()); // Moves to the next row
        }
        assert cursor != null;
        cursor.close();
        dbHelper.close();
        return records_Exist;
    }
}