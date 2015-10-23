package nick.start;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {

    private static final String tag = "MyTag";

    // Stopwatch members
    private long startTime = 0;
    private TextView timerTextView;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable = new Runnable() {
        @Override public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) millis / 1000;
            int minutes = seconds / 60;
            seconds = seconds % 60;

            timerTextView.setText(String.format("%d:%02d", minutes, seconds));
            timerHandler.postDelayed(timerRunnable, 500);
        }
    };

    // Notification members
    private NotificationCompat.Builder mBuilder;
    private int notificationId = 0;

    @Override
    public void onRestart() {
        super.onRestart();
        Log.d(tag, "MainActivity restarted");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(tag, "MainActivity paused");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(tag, "MainActivity resumed");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(tag, "MainActivity started");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(tag, "MainActivity stopped");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(tag, "MainActivity destroyed");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(tag, "MainActivity created");

        timerTextView = (TextView) findViewById(R.id.stopwatchText);

        // Set up the notification stuff
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("My Title");
        mBuilder.setContentText("My Content");
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                this,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setContentIntent(resultPendingIntent);

        // Stopwatch start/stop button
        Button b1 = (Button) findViewById(R.id.bStopwatch);
        b1.setText("start");
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                if (b.getText() == "start") {
                    startTime = System.currentTimeMillis();
                    timerHandler.postDelayed(timerRunnable, 500);
                    b.setText("stop");
                } else {
                    timerHandler.removeCallbacks(timerRunnable);
                    b.setText("start");
                }
            }
        });

        // Background timer start button
        Button b2 = (Button) findViewById(R.id.bTimerStart);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create and bundle user data
                ArrayList<String> categories = new ArrayList<String>();
                categories.add("grocery");
                categories.add("pharmacy");

                Bundle userData = new Bundle();
                userData.putStringArrayList("categories", categories);

                // Create a background service intent with user data
                Intent startServiceIntent = new Intent(getApplicationContext(), BackgroundService.class);
                startServiceIntent.putExtra("userData", userData);

                Log.d(tag, "starting background service");
                startService(startServiceIntent);
            }
        });

        // Background timer stop button
        Button b4 = (Button) findViewById(R.id.bTimerStop);
        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(tag, "stopping background service");
                stopService(new Intent(getApplicationContext(), BackgroundService.class));
            }
        });

        // Notification button
        Button b3 = (Button) findViewById(R.id.bNotification);
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Fires a notification
                NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                mNotifyMgr.notify(notificationId, mBuilder.build());
                ++notificationId;
            }
        });
    }
}
