package nick.start;

import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by nick on 14/10/15.
 */

public class PeriodicTaskReceiver extends BroadcastReceiver {

    private static final String tag = "MyTag";
    private static final String INTENT_ACTION = "nick.start.PERIODIC_TASK_HEART_BEAT";
    private int counter = 0;
    private boolean fireNotification = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        Application myApp = (Application) context.getApplicationContext();

        if (intent.getAction().equals(INTENT_ACTION)) {
            Bundle userData = intent.getBundleExtra("userData");
            if (userData == null) {
                Log.d(tag, "Periodic task: onReceive received intent with no userData bundle");
            }

            doPeriodicTask(context, myApp, userData);
        } else {
            Log.d(tag, "Periodic task received a rogue broadcast: \"" + intent.getAction() + "\"");
        }
    }

    private void doPeriodicTask(Context backgroundServiceContext, Application myApp, Bundle userData) {
        ArrayList<String> categories = userData.getStringArrayList("categories");

        if (categories == null) {
            Log.d(tag, "Periodic task: no categories array found");
        } else {
            Log.d(tag, "Periodic task with data:");
            for (int i = 0; i < categories.size(); i++) {
                Log.d(tag, categories.get(i));
            }

            if (fireNotification) {
                Log.d(tag, "Periodic task about to fire notification");

                NotificationCompat.Builder mBuilder;

                // Set up the notification stuff
                mBuilder = new NotificationCompat.Builder(backgroundServiceContext);
                mBuilder.setContentTitle("My Title");
                mBuilder.setContentText("My Content");
                mBuilder.setSmallIcon(R.mipmap.ic_launcher);
                Intent resultIntent = new Intent(backgroundServiceContext, MainActivity.class);
                PendingIntent resultPendingIntent = PendingIntent.getActivity(
                        backgroundServiceContext,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
                mBuilder.setContentIntent(resultPendingIntent);

                int notificationId = 1;
                NotificationManager mNotifyMgr = (NotificationManager) backgroundServiceContext.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotifyMgr.notify(notificationId, mBuilder.build());

                Log.d(tag, "Periodic task fired notification");
            }
        }

        ++counter;
    }

    public void restartHeartbeat(Context backgroundServiceContext, Bundle userData) {
        Intent alarmIntent = new Intent(backgroundServiceContext, PeriodicTaskReceiver.class);

        // Make sure to pass the data bundle to the intent
        alarmIntent.putExtra("userData", userData);

        boolean isAlarmUp = PendingIntent.getBroadcast(backgroundServiceContext, 0, alarmIntent, PendingIntent.FLAG_NO_CREATE) != null;

        if (!isAlarmUp) {
            AlarmManager alarmManager = (AlarmManager) backgroundServiceContext.getSystemService(Context.ALARM_SERVICE);
            alarmIntent.setAction(INTENT_ACTION);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(backgroundServiceContext, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, SystemClock.currentThreadTimeMillis(), 1000, pendingIntent);
            Log.d(tag, "Periodic task restarted");
        } else {
            Log.d(tag, "Periodic task already running");
        }
    }

    public void stopHeartbeat(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, PeriodicTaskReceiver.class);
        alarmIntent.setAction(INTENT_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);

        alarmManager.cancel(pendingIntent);
        Log.d(tag, "Periodic task stopped");
    }
}
