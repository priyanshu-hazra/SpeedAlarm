package com.priyanshu.speedalarm;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.media.Image;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{
  //setting text to speech engine for alarming
    static TextToSpeech engine;
    //user input speed value for monitoring speed
    static float speedValue=0;
    //used so monitor if user is trying to access after once pause/stop is clicked
    boolean isPauseOrStoppedClicked=false;
    Context context=this;
    //
    boolean gpsStatusChanged=false;

    //for iamgeview
    static ImageView okSign;
    static ImageView notokSign;
    LocationService myService;
    static boolean status;
    LocationManager locationManager;
    static TextView dist,time,speed;
    Button start,pause,stop;
    static long startTime,endTime;
    ImageView image;
    static ProgressDialog locate;
    static int p=0;

    private ServiceConnection sc=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            LocationService.LocalBinder binder=(LocationService.LocalBinder) iBinder;
            myService=binder.getService();
            status=true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            status=false;
        }
    };


    void bindService() {
        if (status == true)
            return;
        Intent i = new Intent(getApplicationContext(), LocationService.class);
        bindService(i, sc, BIND_AUTO_CREATE);
        status = true;
        startTime = System.currentTimeMillis();
    }

    void unbindService() {
        if (status == false)
            return;
        Intent i = new Intent(getApplicationContext(), LocationService.class);
        unbindService(sc);
        status = false;
    }

    @Override
    protected void onResume() {
        //last changes
        //since TTS objects were destroyed in onstop and onPause so we need to reinitiate it on restarting the app
        if (engine==null){
            engine=new TextToSpeech(this,this);  //setting instance of text to speech
        }
        //if after coming from settings gps status is found true on resume then again check and obtain gps location
        if(gpsStatusChanged==true){
            checkGps();
        }
        super.onResume();

    }

    @Override
    protected void onStart() {
        //last changes
        //since TTS objects were destroyed in onstop and onPause so we need to reinitiate it on restarting the app
        if (engine==null){
            engine=new TextToSpeech(this,this);  //setting instance of text to speech
        }
        super.onStart();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (status == true)
            unbindService();
        //Destroy text to speech object
        if (engine!=null){
            engine.stop();
            engine.shutdown();
            Toast.makeText(this, "TTS object destroyed", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onBackPressed() {
        if (status == false)
            super.onBackPressed();
        else
            moveTaskToBack(true);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        engine=new TextToSpeech(this,this);  //setting instance of text to speech


        dist = (TextView) findViewById(R.id.distanceText);
        time = (TextView) findViewById(R.id.timeText);
        speed = (TextView) findViewById(R.id.speedText);

        start = (Button) findViewById(R.id.start);
        pause = (Button) findViewById(R.id.pause);
        stop = (Button) findViewById(R.id.stop);

        image = (ImageView) findViewById(R.id.image);
        //for imageview
        okSign=(ImageView)findViewById(R.id.okSpeed);
        notokSign=(ImageView)findViewById(R.id.excessSpeed);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Take speed input from User
                if (speedValue==0  || isPauseOrStoppedClicked==true) //bug fix! when gps was not enabled the alert box was appearing twice
                //for isPause or stopped clicked is to take speed again once pause or stop is clicked
                    takeUserSpeedLimitInput();
                if(gpsStatusChanged==true && isPauseOrStoppedClicked==false){
                    //input is taken and now just work with gps stuff here only and wont work if stop has been clicked
                    checkGps();
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                    return;
                }


                if (status == false)
                    //Here, the Location Service gets bound and the GPS Speedometer gets Active.
                    bindService();
                locate = new ProgressDialog(MainActivity.this);
                locate.setIndeterminate(true);
                locate.setCancelable(false);
                locate.setMessage("Getting Location...");
                locate.show();
                start.setVisibility(View.GONE);
                pause.setVisibility(View.VISIBLE);
                pause.setText("Pause");
                stop.setVisibility(View.VISIBLE);
//     takeUserSpeedLimitInput();
                }

                //The method below checks if Location is enabled on device or not. If not, then an alert dialog box appears with option
                //to enable gps.
//                checkGps();
                /*locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                    return;
                }


                if (status == false)
                    //Here, the Location Service gets bound and the GPS Speedometer gets Active.
                    bindService();
                locate = new ProgressDialog(MainActivity.this);
                locate.setIndeterminate(true);
                locate.setCancelable(false);
                locate.setMessage("Getting Location...");
                locate.show();
                start.setVisibility(View.GONE);
                pause.setVisibility(View.VISIBLE);
                pause.setText("Pause");
                stop.setVisibility(View.VISIBLE);*/


            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pause.getText().toString().equalsIgnoreCase("pause")) {
                    pause.setText("Resume");
                    p = 1;

                } else if (pause.getText().toString().equalsIgnoreCase("Resume")) {
                    checkGps();
                    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        //Toast.makeText(this, "GPS is Enabled in your devide", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    pause.setText("Pause");
                    p = 0;


                }
            }
        });


        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status == true)
                    unbindService();
                start.setVisibility(View.VISIBLE);
                pause.setText("Pause");
                pause.setVisibility(View.GONE);
                stop.setVisibility(View.GONE);
                p = 0;
                isPauseOrStoppedClicked=true;
            }
        });
    }

    private void takeUserSpeedLimitInput() {
        LayoutInflater layoutInflater= LayoutInflater.from(context);
        View promptsView=layoutInflater.inflate(R.layout.user_input_speed_screen,null); //prompts is the name of the another xml file

        final AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptsView); //setting the view
        final EditText userInput=(EditText)promptsView.findViewById(R.id.editTextDialogUserInput);

        //set dialogue message
        alertDialogBuilder.setMessage("Enter your speed limit :").setCancelable(false)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                               /* Log.d(userInput.getText().toString(), "getting user input ");*/
                                /*speedValue=Float.valueOf(userInput.getText().toString());
                                if(speedValue>100.0){
                                    Toast.makeText(context, "speed excess!", Toast.LENGTH_SHORT).show();
                                    speedValue=0;
                                    dialogInterface.cancel();

                                }else {
                                    dialogInterface.dismiss();
                                }*/

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        //create alert dialog
        final AlertDialog alertDialog=alertDialogBuilder.create();
        //show
        alertDialog.show();
        //overriding setpositivebutton
        alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speedValue=Float.valueOf(userInput.getText().toString());
                if(speedValue>120.0){
                    Toast.makeText(context, "speed excess!", Toast.LENGTH_SHORT).show();

                    speedValue=0;

                }else if (speedValue<=0.0){
                    Toast.makeText(MainActivity.this, "Enter some real value for the speed!!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(MainActivity.this, "Entered speed is "+speedValue+" km/h", Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                    checkGps();
                    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                        return;
                    }


                    if (status == false)
                        //Here, the Location Service gets bound and the GPS Speedometer gets Active.
                        bindService();
                    locate = new ProgressDialog(MainActivity.this);
                    locate.setIndeterminate(true);
                    locate.setCancelable(false);
                    locate.setMessage("Getting Location...");
                    locate.show();
                    start.setVisibility(View.GONE);
                    pause.setVisibility(View.VISIBLE);
                    pause.setText("Pause");
                    stop.setVisibility(View.VISIBLE);
                }
            }
        });

    }


    //This method leads you to the alert dialog box.
    void checkGps() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {


            showGPSDisabledAlertToUser();
        }
    }

    //This method configures the Alert Dialog box.
    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Enable GPS to use application")
                .setCancelable(false)
                .setPositiveButton("Enable GPS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //check if gps status is changed or not
                                gpsStatusChanged=true;
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();

    }

//used for text to speech
    @Override
    public void onInit(int i) {
        engine.setLanguage(Locale.UK);
        engine.setSpeechRate((float) 0.8);
        engine.setPitch((float) 1.5);
    }
    //required for text to speech
   static void speech(){
       //Queue add as otherwise on middle only it will overtake previous speech
        engine.speak(" Exceeding speed Limit.",TextToSpeech.QUEUE_ADD,null);
       engine.speak("Drive Slow.",TextToSpeech.QUEUE_ADD,null);
    }

    static void speechOk(){
        //when speed comes in limit any speech in queue is removed and this speech is spoken.
        engine.speak(" Speed OK",TextToSpeech.QUEUE_FLUSH,null);
    }

    //required for text to specch
    @Override
    protected void onPause() {
        //if app is paused then destroy the TTS object. Created again at OnResume-(not done yet .Do it)

        super.onPause();
    }

    @Override
    protected void onStop() {
        //if app is stopped then destroy the TTS
        /*if (engine!=null){
            engine.stop();
            engine.shutdown();
            Toast.makeText(this, "Destroyed tts object", Toast.LENGTH_SHORT).show();
        }*/
        super.onStop();
    }
    //till here

    @Override
    protected void onRestart() {
        //since TTS objects were destroyed in onstop and onPause so we need to reinitiate it on restarting the app
        if (engine==null){
            engine=new TextToSpeech(this,this);  //setting instance of text to speech
        }
        super.onRestart();
    }

}

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//    }
//}
