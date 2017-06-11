package com.example.savch.dypproj;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.savch.dypproj.login.LoginActivity;
import com.example.savch.dypproj.session.Session;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private Context context;
    private Session session;
    //private String messageSend;
    private String resultGPS = "";
    private GPSTracker gps;
    private JSONObject json;

    private static final String[] LOCATION_PERMS={
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final int LOCATION_REQUEST = 1340;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //JSON object initialisation
        json = new JSONObject();

        //Get activity context
        context = this;

        //Session create
        session = new Session(this);

        //TextView objects
        TextView textUserLogin = (TextView) findViewById(R.id.textLoginCh);
        textUserLogin.setText(session.getLogin());
        TextView textUserName = (TextView) findViewById(R.id.textNameCh);
        String completeName = session.getName() + " " + session.getSurName();
        textUserName.setText(completeName);
        final TextView textUserLocal = (TextView) findViewById(R.id.textLocalCh);

        //EditText objects
        final EditText editComment = (EditText) findViewById(R.id.textCommentEdit);

        //Button objects
        Button _buttonLocation = (Button) findViewById(R.id.loc_button);
        requestPermissions(LOCATION_PERMS, LOCATION_REQUEST);

        //put login, name and comment into JSON object
        try {
            json.put("login", textUserLogin.getText().toString());
            json.put("name", textUserName.getText().toString());
            json.put("localiztion", "");
            json.put("comment", "");
        } catch (JSONException e) {
            System.out.println("JSON ERROR: " + e);
        }


        //Location button listener
        _buttonLocation.setOnClickListener(new View.OnClickListener() {

               @Override
               public void onClick(View arg0) {
                   gps = new GPSTracker(MainActivity.this);
                   if(gps.canGetLocation()){

                       double latitude = gps.getLatitude();
                       double longitude = gps.getLongitude();
                       String strlat = String.valueOf(latitude);
                       String strLong = String.valueOf(longitude);

                       //create localization coordinates string
                       resultGPS = strlat.substring(0, 5) + " " + strLong.substring(0, 5);
                       try {
                           json.put("localiztion", resultGPS);
                       } catch (JSONException e) {
                           System.out.println("JSON ERROR: " + e);
                       }
                       textUserLocal.setText(resultGPS);
                   }else{
                       // can't get location
                       // GPS or Network is not enabled
                       // Ask user to enable GPS/network in settings
                       try {
                           json.put("localiztion", null);
                       } catch (JSONException e) {
                           System.out.println("JSON ERROR: " + e);
                       }
                       gps.showSettingsAlert();
                   }
               }
           });



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    json.put("comment", editComment.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String jsonSTR = json.toString();
                Snackbar.make(view, jsonSTR, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            //Logout alert
            final AlertDialog.Builder exitAlert = new AlertDialog.Builder(context);
            exitAlert.setTitle(R.string.exit_alert_title);
            exitAlert.setMessage(R.string.exit_alert_messege);
            exitAlert.setPositiveButton(R.string.yes_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    logOut();
                }
            });
            exitAlert.setNegativeButton(R.string.no_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            AlertDialog alertDialog = exitAlert.create();
            alertDialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logOut(){
        session.setLoggedin(false);
        finish();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }
}
