package fourpointoh.closebuy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class NotificationService extends Service {

    private final int WAKE_UP_MILLIS = 60 * 1000;
    private Context appContext;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // WARNING: intent parameter may be null

        appContext = getApplicationContext();

        // start the heartbeat w/ broadcast receiver
        Intent alarmIntent = new Intent(appContext, NearbyStoreUpdate.class);

        boolean isAlarmUp = PendingIntent.getBroadcast(appContext, 0, alarmIntent, PendingIntent.FLAG_NO_CREATE) != null;

        if (!isAlarmUp) {
            AlarmManager alarmManager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
            alarmIntent.setAction(getString(R.string.update_action));
            PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, SystemClock.currentThreadTimeMillis(), WAKE_UP_MILLIS, pendingIntent);
            Log.d(getString(R.string.log_tag), "NotificationService onStartCommand(): NearbyStoreUpdate started");
        } else {
            Log.d(getString(R.string.log_tag), "NotificationService onStartCommand(): NearbyStoreUpdate already running");
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // stop the heartbeat w/ broadcast receiver
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(appContext, NearbyStoreUpdate.class);
        alarmIntent.setAction(getString(R.string.update_action));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 0, alarmIntent, 0);

        alarmManager.cancel(pendingIntent);
        Log.d(getString(R.string.log_tag), "NotificationService onDestroy(): NearbyStoreUpdate stopped");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(getString(R.string.log_tag), "NotificationService onTaskRemoved(): root intent " + rootIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;

        // TODO: Return the communication channel to the service.
        // throw new UnsupportedOperationException("Not yet implemented");
    }
}
