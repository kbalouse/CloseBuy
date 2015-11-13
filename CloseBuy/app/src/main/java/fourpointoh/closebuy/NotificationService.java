package fourpointoh.closebuy;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NotificationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    // Location Update Rates
    private final int MILLIS_PER_SEC = 1000;
    private final int INTERVAL_MILLIS = 20 * MILLIS_PER_SEC;
    private final int FASTEST_INTERVAL_MILLIS = 15 * MILLIS_PER_SEC;

    private Context appContext;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private boolean requestingLocationUpdates;
    private Location previousLocation;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // WARNING: intent parameter may be null

        // Initialize
        appContext = getApplicationContext();
        requestingLocationUpdates = false;
        previousLocation = null;

        locationRequest = new LocationRequest();
        locationRequest.setInterval(INTERVAL_MILLIS);
        locationRequest.setFastestInterval(FASTEST_INTERVAL_MILLIS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(getString(R.string.log_tag), "NotificationService onDestroy(): NearbyStoreUpdate stopped");

        // Stop location updates
        if (requestingLocationUpdates) {
            requestingLocationUpdates = false;
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, this);
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        onDestroy();
        super.onTaskRemoved(rootIntent);
        Log.d(getString(R.string.log_tag), "NotificationService onTaskRemoved(): root intent " + rootIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;

        // TODO: Return the communication channel to the service.
        // throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(getString(R.string.log_tag), "API client connected");

        // Start location updates if not doing so already
        if (!requestingLocationUpdates) {
            Log.d(getString(R.string.log_tag), "requesting location updates");
            requestingLocationUpdates = true;
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(getString(R.string.log_tag), "API client connection suspended, result = " + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(getString(R.string.log_tag), "API client connection failed, result = " + connectionResult.getErrorMessage());
    }

    @Override
    public void onLocationChanged(Location location) {

        // The emulator was invoking the callback twice for every location change.
        // Not sure if a real device will be different. Check anyways.
        if (previousLocation == null || !previousLocation.equals(location)) {
            previousLocation = location;
            locationUpdateTask(location.getLatitude(), location.getLongitude());
        }
    }

    private void locationUpdateTask(double latitude, double longitude) {
        Log.d(getString(R.string.log_tag), "NotificationService locationUpdateTask(" + latitude + ", " + longitude + ")");

        GooglePlacesRequest request = new GooglePlacesRequest();
        request.setLatitude(latitude);
        request.setLongitude(longitude);
        request.setRadius((double) 100);

        // Query DB to get all categories
        ReminderItemDbHelper db = ReminderItemDbHelper.getInstance(appContext);
        ArrayList<ReminderItem> items = db.getAllItems();

        if (items.isEmpty()) {
            return;
        }

        // Place each item into appropriate category sets.
        // mapping: category -> all items that have that category.
        Map<Category, Set<ReminderItem>> categorySetMap = new HashMap<Category, Set<ReminderItem>>();
        for (ReminderItem item : items) {
            for (Category c : item.categories) {
                // Make sure the map entry is there
                Set<ReminderItem> s = categorySetMap.get(c);
                if (s == null) {
                    categorySetMap.put(c, new HashSet<ReminderItem>());
                }
                categorySetMap.get(c).add(item);
            }
        }

        // Add the google category names to the request
        for (Category c : categorySetMap.keySet()) {
            request.addType(c.toGoogleCategoryString());
        }

        // Make the API query
        ArrayList<Place> places = new GooglePlacesService(appContext).getNearbyPlaces(request);

        // For each returned place, fire a notification for all items that fit its categories
        Log.d(getString(R.string.log_tag), "API Results");
        for (Place p : places) {
            Log.d(getString(R.string.log_tag), p.getName() + " types:" + p.getTypes());
            String message = "You are near " + p.getName() + ". Do you want to pick up ";

            for (Category c : p.getRecognizedCategories()) {
                if (categorySetMap.get(c) != null) {
                    for (ReminderItem item : categorySetMap.get(c)) {
                        message += item.itemName + ", ";
                    }
                }
            }

            message = message.substring(0, message.length() - 2) + "?";
            Log.d(getString(R.string.log_tag), "Notification message: \"" + message + "\"");
            fireNotification(appContext, "CloseBuy", message, R.drawable.add_text_image);
        }
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
