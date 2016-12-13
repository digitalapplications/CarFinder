package com.amoryosef613.carfinder;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;

import java.util.List;

public class MainActivity extends Activity {
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    GPSTracker mGPS;
    NetworkDetector networkDetector;
    private String TAG = "MainActivity";
   public static long milliesUntill=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);
        ActivityManager am = (ActivityManager) getApplication().getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        Log.d("topActivity", "CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());
        ComponentName componentInfo = taskInfo.get(0).topActivity;
        componentInfo.getPackageName();
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
//                 No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
        mGPS = new GPSTracker(MainActivity.this);

        networkDetector = new NetworkDetector(MainActivity.this);
        if (!networkDetector.isConnectingToInternet()) {
            networkDetector.showAlertDialog(MainActivity.this, "Internet Error!!", "Please check internet connection", false);
        } else {
            if (!mGPS.GPS_Status()) {
                mGPS.showSettingsAlert();
            }
        }

        ImageButton parking = (ImageButton) findViewById(R.id.parking);
        parking.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intentName = new Intent(getApplicationContext(), MapFragment.class);
                startActivity(intentName);
                finish();
            }

            ;
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SharedPreferences.Editor spEditor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                    spEditor.putBoolean("permission_granted", true);
                    spEditor.apply();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                            MY_PERMISSIONS_REQUEST_LOCATION);
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
   /* public static void timeup(final Context context) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("Time up");
        dialog.setMessage("Do you want to stop alarm?");
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            *//*    MediaPlayer mediaPlayer = new MediaPlayer();

                try {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }*//*
                ((Activity)context).finish();
            }
        });
        dialog.show();
    }*/

}

