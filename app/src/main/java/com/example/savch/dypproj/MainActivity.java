package com.example.savch.dypproj;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.savch.dypproj.base.SQLAdapter;
import com.example.savch.dypproj.login.LoginActivity;
import com.example.savch.dypproj.session.Session;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    static final int REQUEST_TAKE_PHOTO = 1;
    private static final String[] LOCATION_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final int LOCATION_REQUEST = 1340;
    private static final int SERVER_PORT = 1994;
    private static final String SERVER_IP = "192.168.0.13";
    SQLAdapter dbHelper;
    private Context context;
    private Session session;
    private GPSTracker gps;
    private JSONObject json;
    private ImageView photoImage;
    private String mCurrentPhotoPath, base64 = "",
            resultGPS = "", state[] = null,
            selectedAgreement, selectedChain, selectedStore, selectedShelf,
            dateTime;
    private Spinner spinnerStore;

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
                Uri photoURI = FileProvider.getUriForFile(this,
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
            photoImage.setImageURI(Uri.parse(mCurrentPhotoPath));
            base64 = new Base64Utils(mCurrentPhotoPath).getBase64();
            try {
                json.put("image", base64);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @android.support.annotation.RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Policy for unblock android guard
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

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

        //Spinners
        Spinner spinnerAgreement = (Spinner) findViewById(R.id.spinner_agreement);
        Spinner spinnerChain = (Spinner) findViewById(R.id.spinner_chain);
        spinnerStore = (Spinner) findViewById(R.id.spinner_store);
        Spinner spinnerShelf = (Spinner) findViewById(R.id.spinner_shelf);
        // Spinner Drop down elements

        List<String> categories = new ArrayList<>();
        categories.add("Biedronka");
        categories.add("Zabka");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerChain.setAdapter(dataAdapter);
        spinnerChain.setOnItemSelectedListener(this);

        //Shelfs
        List<String> shelfs = new ArrayList<>();
        shelfs.add("1");
        shelfs.add("2");
        shelfs.add("3");
        shelfs.add("4");
        shelfs.add("5");
        shelfs.add("6");
        ArrayAdapter<String> dataAdapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, shelfs);
        dataAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerShelf.setAdapter(dataAdapter1);
        spinnerShelf.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedShelf = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //----------------
        dbHelper = new SQLAdapter(this);
        dbHelper.openToWrite();
        List<String> agreements = new ArrayList<>();
        Cursor cursor = dbHelper.queueAllAgreement();
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();

            do {
                agreements.add(cursor.getString(cursor.getColumnIndex("title")));
            } while (cursor.moveToNext()); // Moves to the next row
        }
        assert cursor != null;
        cursor.close();
        dbHelper.close();

        ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, agreements);
        dataAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAgreement.setAdapter(dataAdapter2);
        spinnerAgreement.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedAgreement = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        //Button objects
        Button _buttonLocation = (Button) findViewById(R.id.loc_button);
        Button _buttonPhoto = (Button) findViewById(R.id.photo_btn);
        requestPermissions(LOCATION_PERMS, LOCATION_REQUEST);

        //ImageView objects
        photoImage = (ImageView) findViewById(R.id.photoView);

        //put login, name and comment into JSON object
        try {
            json.put("updateUserDb", "false");
            json.put("login", textUserLogin.getText().toString());
            json.put("name", textUserName.getText().toString());
            json.put("localization", "");
            json.put("comment", "");
            json.put("image", base64);
        } catch (JSONException e) {
            System.out.println("JSON ERROR: " + e);
        }

        gps = new GPSTracker(MainActivity.this);
        if (gps.canGetLocation()) {

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            String strlat = String.valueOf(latitude);
            String strLong = String.valueOf(longitude);

            //create localization coordinates string
            resultGPS = strlat.substring(0, 5) + " " + strLong.substring(0, 5);
            try {
                json.put("localization", resultGPS);
            } catch (JSONException e) {
                System.out.println("JSON ERROR: " + e);
            }
            textUserLocal.setText(resultGPS);
        } else {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            try {
                json.put("localization", null);
            } catch (JSONException e) {
                System.out.println("JSON ERROR: " + e);
            }
            gps.showSettingsAlert();
        }


        //Location button listener
        _buttonLocation.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                gps = new GPSTracker(MainActivity.this);
                if (gps.canGetLocation()) {

                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();
                    String strlat = String.valueOf(latitude);
                    String strLong = String.valueOf(longitude);

                    //create localization coordinates string
                    resultGPS = strlat.substring(0, 5) + " " + strLong.substring(0, 5);
                    try {
                        json.put("localization", resultGPS);
                    } catch (JSONException e) {
                        System.out.println("JSON ERROR: " + e);
                    }
                    textUserLocal.setText(resultGPS);
                } else {
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    try {
                        json.put("localization", null);
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
                //Get date and time
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
                Date date = new Date();
                dateTime = dateFormat.format(date);
                try {
                    json.put("comment", editComment.getText().toString());
                    json.put("agreement", selectedAgreement);
                    json.put("chainStore", selectedChain);
                    json.put("store", selectedStore);
                    json.put("shelf", selectedShelf);
                    json.put("date", dateTime);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String jsonSTR = json.toString();
                Snackbar.make(view, "Sent", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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

    private void logOut() {
        session.setLoggedin(false);
        finish();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedChain = (String) parent.getItemAtPosition(position);
        if (position == 0) {
            state = new String[]{"Biedronka1", "Biedronka2", "Biedronka3"};
        }
        if (position == 1) {
            state = new String[]{"Zabka1", "Zabka2", "Zabka3"};
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, state);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStore.setAdapter(dataAdapter);
        spinnerStore.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStore = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /*class ClientThread implements Runnable {
        @Override
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, SERVERPORT);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }*/
    /*private class DownloadFilesTask extends AsyncTask<String, Integer> {
        @Override
        protected Object doInBackground(Object[] params) {
            try {
                String servIP = (String) params[0];
                int servPort = (Integer) params[1];
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, SERVERPORT);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return null;
        }
    }*/
    /*class MyTask extends AsyncTask<Void,Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, SERVERPORT);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }
    }*/

}
