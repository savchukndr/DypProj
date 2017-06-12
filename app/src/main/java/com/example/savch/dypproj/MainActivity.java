package com.example.savch.dypproj;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.ProgressDialog;

import com.example.savch.dypproj.login.LoginActivity;
import com.example.savch.dypproj.session.Session;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private Context context;
    private Session session;
    private String resultGPS = "";
    private GPSTracker gps;
    private JSONObject json;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String[] LOCATION_PERMS={
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final int LOCATION_REQUEST = 1340;
    private ImageView photoImage;
    private String mCurrentPhotoPath;
    private Uri photoURI;
    private String base64 = "";

    /*private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }*/

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.savch.dypproj",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        }
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            //Bundle extras = data.getExtras();
            //Bitmap imageBitmap = (Bitmap) extras.get("data");
            //photoImage.setImageBitmap(imageBitmap);
            photoImage.setImageURI(Uri.parse(mCurrentPhotoPath));
            //TODO : set progress bar
            base64 = new Base64Utils(mCurrentPhotoPath).getBase64();
            try {
                json.put("image", base64);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

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
        Button _buttonPhoto = (Button) findViewById(R.id.photo_btn);
        requestPermissions(LOCATION_PERMS, LOCATION_REQUEST);

        //ImageView objects
        photoImage = (ImageView) findViewById(R.id.photoView);

        //put login, name and comment into JSON object
        try {
            json.put("login", textUserLogin.getText().toString());
            json.put("name", textUserName.getText().toString());
            json.put("localiztion", "");
            json.put("comment", "");
            //TODO: alert if base64 is null
            json.put("image", base64);
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


        _buttonPhoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                dispatchTakePictureIntent();
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
