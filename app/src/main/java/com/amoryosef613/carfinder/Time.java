package com.amoryosef613.carfinder;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.os.Bundle;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.security.AccessController.getContext;

public class Time extends AppCompatActivity
{
    String MY_PREFS_NAME="refs";
    NumberPicker hrPicker = null;
    NumberPicker minPicker = null;
    ImageView meterStart,mapButton;
    TextView timeLeft,currentTime_clock;
    int hour=0;
    boolean timmerr=false;
    int minute=0;
    long timer_time;
  public static  MediaPlayer mediaPlayer ;
    CounterClass waitTimer=null;
    public static boolean activityVisible=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.time_screen);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ActivityManager am = (ActivityManager) getApplication().getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        Log.d("topActivity", "CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());
        ComponentName componentInfo = taskInfo.get(0).topActivity;
        componentInfo.getPackageName();
        //Number Picker 1
        hrPicker = (NumberPicker) findViewById(R.id.pickNumber1);
        hrPicker.setMaxValue(23);
        hrPicker.setMinValue(0);
        hrPicker.setValue(0);
        hrPicker.setWrapSelectorWheel(true);

        hrPicker.performClick();

        //Number Picker 1
        minPicker = (NumberPicker) findViewById(R.id.pickNumber2);
        minPicker.setMaxValue(59);
        minPicker.setMinValue(0);
        minPicker.setValue(0);
        minPicker.setWrapSelectorWheel(true);

        minPicker.performClick();
        mapButton = (ImageView)findViewById(R.id.mapButton);
        meterStart = (ImageView) findViewById(R.id.meterStart);

        timeLeft = (TextView) findViewById(R.id.timeLeft);

        hrPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i2) {
                timer_time=0;
                hour=i2;
            }
        });

        minPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i2) {
                timer_time=0;
                minute=i2;
            }
        });

        //this where i'd like to use the value
        //of the number picker, in the first parameter)
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent();
                setResult(RESULT_OK,intent);
                finish();
            }
        });
        meterStart.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                long HH= hour_milisecond(hour);
                long MM= minute_milisecond(minute);
                timer_time=HH+MM;
                Log.i("timer_time",""+timer_time);
                if(waitTimer != null) {
                    //REMOVE PREVIOUS NOTIFICATION
                     NotificationManager notificationManager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                     notificationManager.cancel(101);

                    MainActivity.milliesUntill=0;
                    waitTimer.cancel();
                    waitTimer = null;
                    minPicker.setValue(0);
                    hrPicker.setValue(0);
                    hour=0;
                    minute=0;
                    timmerr=false;
                }
               else
                {
                    timmerr=true;
                    waitTimer = new CounterClass(timer_time, 1000);
                    waitTimer.start();
                    minPicker.setValue(0);
                    hrPicker.setValue(0);
                    hour=0;
                    minute=0;
                    Log.d("magic_timer",String.valueOf(timer_time));
                    Log.d("magic_MA",String.valueOf(MainActivity.milliesUntill));
                 if(timer_time>0) {
                        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                        editor.putLong("val",MainActivity.milliesUntill+System.currentTimeMillis());
                        editor.commit();

                        long l=timer_time;
                        Intent alertIntent = new Intent(getApplicationContext(), AlertReceiver.class);
                        AlarmManager alarmManager = (AlarmManager)
                                getSystemService(Context.ALARM_SERVICE);
                        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+l,
                                PendingIntent.getBroadcast(getApplicationContext(), 101, alertIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT));
                    }
                    else {

                        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                        editor.putLong("val",0);
                        editor.commit();


                    }



                }

            }
        });

        //checking if activity is launched for Time up purpose
      if(getIntent().getStringExtra("timeup")!=null&& getIntent().getStringExtra("timeup").equals("true")){
        //TODO Here you can do what you want on Timer's up!
          Toast.makeText(this,"Your time is over",Toast.LENGTH_SHORT).show();

    /*      new AlertDialog.Builder(this)
                  .setTitle("Stop Alarm")
                  .setMessage("Do you want to stop Alarm?")
                  .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                      public void onClick(DialogInterface dialog, int which) {
                          try {
                              mediaPlayer.stop();
                              mediaPlayer.release();
                          } catch (IllegalArgumentException e) {
                              e.printStackTrace();
                          } catch (SecurityException e) {
                              e.printStackTrace();
                          } catch (IllegalStateException e) {
                              e.printStackTrace();
                          }
                      }
                  })
                  .setNegativeButton("No", new DialogInterface.OnClickListener() {
                      public void onClick(DialogInterface dialog, int which) {
                          // do nothing
                      }
                  })
                  .setIcon(android.R.drawable.ic_dialog_alert)
                  .show();*/


          //set alarm ring here...
           Uri defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);


            try {
                Time.mediaPlayer=new MediaPlayer();
                Time.mediaPlayer.setDataSource(getBaseContext(), defaultRingtoneUri);
                Time.mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                Time.mediaPlayer.prepare();
                Time.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp)
                    {
                        mp.start();
                    }
                });
                Time.mediaPlayer.start();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
      }
    }
//******************************************************************************

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @SuppressLint("NewApi")

    public class CounterClass extends CountDownTimer
    {
        public CounterClass(long millisInFuture, long countDownInterval)
        {
            super(millisInFuture, countDownInterval);
        }

        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        @SuppressLint("NewApi")

        @Override
        public void onTick(long millisUntilFinished)
        {
            long millis = millisUntilFinished;
            String hms =  String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                    TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
            timeLeft.setText(hms);
            if(millisUntilFinished>1000 && millisUntilFinished<3000){
                NotificationManager notificationManager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(101);
                Intent alertIntent = new Intent(getApplicationContext(), AlertReceiver.class);
               // Toast.makeText(getBaseContext(),"notification canceled",Toast.LENGTH_SHORT).show();
                AlarmManager alarmManager = (AlarmManager)
                        getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(PendingIntent.getBroadcast(getApplicationContext(), 101, alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT));

            }
            MainActivity.milliesUntill=millisUntilFinished;
        }



        @Override
        public void onFinish()
        {
            MainActivity.milliesUntill=0;
            timmerr=false;
            timeLeft.setText("00:00:00");
            timer_time=0;
            //mediaPlayer=null;
            Toast.makeText(getBaseContext(),"Your Time is Finished!",Toast.LENGTH_LONG).show();

            ActivityManager am = (ActivityManager) getApplication().getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            Log.d("topActivity", "CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());
            String packageName=taskInfo.get(0).topActivity.getClassName();
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            componentInfo.getPackageName();

        if(packageName.equals("com.amoryosef613.carfinder.Time"))
        {
           /* Intent intent=new Intent(getApplicationContext(), DialogActivity.class);
            startActivity(intent);*/
        }
        else if(packageName.equals("com.amoryosef613.carfinder.MapFragment")){
          /*  Intent intent=new Intent(getApplicationContext(), DialogActivity.class);
            startActivity(intent);*/

        }

            Uri defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);


            try {
                Time.mediaPlayer=new MediaPlayer();
                Time.mediaPlayer.setDataSource(getBaseContext(), defaultRingtoneUri);
                Time.mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                Time.mediaPlayer.prepare();
                Time.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp)
                    {
                        mp.start();

                        //mp.release();
                    }
                });
                Time.mediaPlayer.start();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }






    /*        new AlertDialog.Builder(Time.this)
                    .setTitle("Stop Alarm")
                    .setMessage("Do you want to stop Alarm?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                mediaPlayer.stop();
                                mediaPlayer.release();
                            } catch (IllegalArgumentException e) {
                                e.printStackTrace();
                            } catch (SecurityException e) {
                                e.printStackTrace();
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();*/


        }
    }
    private long minute_milisecond(int minute)
    {
        Log.i("minute_milisecond",""+minute);
        long milliseconds = minute * 60000;
        return milliseconds;
    }

    private long hour_milisecond(int hour)
    {
        Log.i("hour_milisecond",""+hour);
        int min = (int) Math.round(hour * 60);
        long milliseconds = min * 60000;
        return milliseconds;
    }

    @Override
    protected void onPause() {
        Log.d("magic","pause");
        Log.d("magicM",String.valueOf(MainActivity.milliesUntill));
        Log.d("magicS",String.valueOf(MainActivity.milliesUntill+System.currentTimeMillis()));


        if(MainActivity.milliesUntill!=0) {
            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
            editor.putLong("val",MainActivity.milliesUntill+System.currentTimeMillis());
            editor.commit();

            long l=MainActivity.milliesUntill;
            Log.d("magic",String.valueOf(l));
            Intent alertIntent = new Intent(getApplicationContext(), AlertReceiver.class);
            AlarmManager alarmManager = (AlarmManager)
                    getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+l,
                    PendingIntent.getBroadcast(getApplicationContext(), 101, alertIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT));
        }
        else {
            /*try {
                Time.mediaPlayer.stop();
                Time.mediaPlayer.release();
                timmerr=false;

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }*/
            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
            editor.putLong("val",0);
            editor.commit();

        }
        super.onPause();

    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    try {
                        Time.mediaPlayer.stop();
                        Time.mediaPlayer.release();
                        mediaPlayer.release();
                        mediaPlayer.stop();
                        timmerr=false;

                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    try {
                        Time.mediaPlayer.stop();
                        Time.mediaPlayer.release();
                        mediaPlayer.release();
                        mediaPlayer.stop();
                        timmerr=false;

                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }


    @Override
    protected void onResume() {
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        long restoredVal = prefs.getLong("val", 0);
        Log.d("magic","resume");
        Log.d("magicV",String.valueOf(restoredVal-System.currentTimeMillis()));
        Log.d("magicV",String.valueOf(restoredVal));
        if (restoredVal-System.currentTimeMillis()>0)
        {
            waitTimer = new CounterClass(restoredVal-System.currentTimeMillis(), 1000);
            waitTimer.start();
            minPicker.setValue(0);
            hrPicker.setValue(0);
            hour=0;
            minute=0;
        }

        super.onResume();
    }
    public static boolean isActivityVisible() {
        return activityVisible;
    }
}