package com.example.savch.dypproj.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.savch.dypproj.MainActivity;
import com.example.savch.dypproj.R;
import com.example.savch.dypproj.base.MySQLAdapter;
import com.example.savch.dypproj.session.Session;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    private static final int RC_SIGN_IN = 9001;
    final String LOG_TAG = "myLogs";
    MySQLAdapter dbHelper;

    private EditText _loginText;
    private EditText _passwordText;
    private Session session;
    private String userName;
    private String userSurName;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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

        dbHelper = new MySQLAdapter(this);
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
                    if (cursor.getString(cursor.getColumnIndex("password")).equals(password)) {
                        records_Exist = true;
                        userName = cursor.getString(cursor.getColumnIndex("name"));
                        userSurName = cursor.getString(cursor.getColumnIndex("surname"));
                        break;
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