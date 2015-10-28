package fourpointoh.closebuy;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NearbyStoreUpdate extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // It was suggested that we check the action string to make sure it is correct intent?
//        if (!intent.getAction().equals(context.getString(R.string.update_action)))

        Log.d(context.getString(R.string.log_tag), "NearbyStoreUpdate onReceive()");
        fireNotification(context, "Example title", "Example description", R.drawable.add_text_image);

        // TODO
        // implement update logic
    }

    private void fireNotification(Context context, String title, String description, int iconResource) {
        // Create the pending intent to start the home page activity
        Intent homePageIntent = new Intent(context, HomeActivity.class);
        PendingIntent pending = PendingIntent.getActivity(context, 0, homePageIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Create the notification builder and set the appropriate fields
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle(title);
        builder.setContentText(description);
        builder.setSmallIcon(iconResource);
        builder.setContentIntent(pending);

        // Fetch the notification service and fire the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }
}
