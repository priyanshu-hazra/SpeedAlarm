package com.priyanshu.speedalarm;

import android.app.Service;

import android.content.Intent;

import android.location.Location;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;

import java.util.concurrent.TimeUnit;

import static com.priyanshu.speedalarm.MainActivity.dist;
import static com.priyanshu.speedalarm.MainActivity.notokSign;
import static com.priyanshu.speedalarm.MainActivity.okSign;
import static com.priyanshu.speedalarm.MainActivity.speech;
import static com.priyanshu.speedalarm.MainActivity.speechOk;
import static com.priyanshu.speedalarm.MainActivity.speedValue;

/**
 * Created by priyanshu on 03-02-2017.
 */

public class LocationService extends Service implements  LocationListener,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {

    private int CHECK_SPEED_LIMIT=1; // 1-> within limit 0->outside limit
    private static final long INTERVAL=1000*2;
    private static final long FASTEST_INTERVAL=1000*1;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation,lStart,lEnd;
    static double distance=0;
    double speed;
    TextToSpeech tts; //used for speaking something
   // final Context context=this; //used for alert

    private final IBinder mBinder= new LocalBinder();


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //local binder above was not working so from google developrs docs added the below class to see if it works
    public class LocalBinder extends Binder {
//        LocationService getService() {
//            // Return this instance of LocalService so clients can call public methods
//            return LocationService.this;
//        }
            public LocationService getService() {
    return LocationService.this;
}
    }
    //till here copied from google docs



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        //here try






        return mBinder;
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onLocationChanged(Location location) {
        MainActivity.locate.dismiss();
        mCurrentLocation = location;
        if (lStart == null) {
            lStart = mCurrentLocation;
            lEnd = mCurrentLocation;
        } else
            lEnd = mCurrentLocation;

        //Calling the method below updates the  live values of distance and speed to the TextViews.
        updateUI();
        //calculating the speed with getSpeed method it returns speed in m/s so we are converting it into kmph
        speed = location.getSpeed() * 18 / 5;

        //generate possible alert





    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
            try{
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);

            }catch (SecurityException e)
            {

            }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        distance = 0;
    }

    //The live feed of Distance and Speed are being set in the method below .
    private void updateUI() {
           // speech();
        if (MainActivity.p == 0) {
            distance = distance + (lStart.distanceTo(lEnd) / 1000.00);
            MainActivity.endTime = System.currentTimeMillis();
            long diff = MainActivity.endTime - MainActivity.startTime;
            diff = TimeUnit.MILLISECONDS.toMinutes(diff);
            MainActivity.time.setText("Total Time: " + diff + " minutes");
            if (speed > 0.0) {

                MainActivity.speed.setText("Current speed: " + String.format("%.2f", speed) + " km/hr");
            }
                else
                MainActivity.speed.setText(".......");

            dist.setText(String.format("%.3f",distance) + " Km's.");

            lStart = lEnd;

            //generate alert at condition
            if (distance>0.0){
            //check for inner class
                //Toast.makeText(this, "speed changing", Toast.LENGTH_SHORT).show();



            }
            //this is used to see speed if >0 or not
            //Limit will be changed later on.
            if (speed>speedValue){
                CHECK_SPEED_LIMIT=0; //shows speed is outside limit
                notokSign.setVisibility(View.VISIBLE);
                okSign.setVisibility(View.GONE);
                speech(); // to speak tts
            }
            else{
                okSign.setVisibility(View.VISIBLE);
                notokSign.setVisibility(View.GONE);
                //if speed was outside limit then change the variable to within limits
                if (CHECK_SPEED_LIMIT==0){

                    CHECK_SPEED_LIMIT=1; //checked to within limits
                    speechOk(); // to flush out previous stored speech if any and speak 'speed ok'
                }
            }


/*      trial part -to make sure objects were displaying properly
            if (diff%2==0){
                notokSign.setVisibility(View.GONE);
                okSign.setVisibility(View.VISIBLE);
            }
            else {
                okSign.setVisibility(View.GONE);
                notokSign.setVisibility(View.VISIBLE);
            }*/



        }

    }

    @Override
    public boolean onUnbind(Intent intent) {
        stopLocationUpdates();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        lStart=null;
        lEnd=null;
        distance=0;

        return super.onUnbind(intent);
    }

}
