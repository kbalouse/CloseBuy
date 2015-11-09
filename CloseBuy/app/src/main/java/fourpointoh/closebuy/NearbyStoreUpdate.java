package fourpointoh.closebuy;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

public class NearbyStoreUpdate extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // It was suggested that we check the action string to make sure it is correct intent?
//        if (!intent.getAction().equals(context.getString(R.string.update_action)))

        Log.d(context.getString(R.string.log_tag), "NearbyStoreUpdate onReceive()");
        GooglePlacesRequest request = new GooglePlacesRequest();
        request.setLatitude(42.2797577);
        request.setLongitude(-83.7408239);
        request.setRadius((double) 100);
        request.addType("pharmacy");

        ArrayList<Place> places = new GooglePlacesService(context).getNearbyPlaces(request);

        if(places.size() > 0)
            fireNotification(context, "CloseBuy", "You are near " + places.get(0).getName() + ". Do you want to pick up Band-Aids?", R.drawable.add_text_image);
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
        builder.setStyle(new Notification.BigTextStyle().bigText(description));

        // Fetch the notification service and fire the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }
}
