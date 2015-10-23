package nick.start;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by nick on 14/10/15.
 */
public class BackgroundService extends Service {

    private static final String tag = "MyTag";
    private PeriodicTaskReceiver mPeriodicTaskReceiver = new PeriodicTaskReceiver();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(tag, "background service onStartCommand");

        // IDK why but this method is called when removing task from recent task list
        if (intent == null) {
            Log.d(tag, "background service stopping itself");
            stopSelf(); // This also calls onDestroy() which handle the cleanup logic
        } else {
            // Pass the data bundle to the periodic task
            Bundle userData = intent.getBundleExtra("userData");
            if (userData == null) {
                Log.d(tag, "background service: no userData bundle found");
            } else if (userData.getStringArrayList("categories") == null) {
                Log.d(tag, "background service: no categories array found");
            }

            Log.d(tag, "background service restarting periodic task");
            mPeriodicTaskReceiver.restartHeartbeat(BackgroundService.this, userData);
        }

        return START_NOT_STICKY; // Consider START_NOT_STICKY (this fn will not be called with a null intent anymore)
    }

    @Override
    public void onDestroy() {
        Log.d(tag, "background service being destroyed");
        mPeriodicTaskReceiver.stopHeartbeat(BackgroundService.this);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(tag, "background service task removed");
        onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
