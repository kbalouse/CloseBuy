package fourpointoh.closebuy;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
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
    private CountDownTimer timer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // WARNING: intent parameter may be null

        String action = "";
        if (intent != null)
            action = intent.getAction();

        if (action != null && action.equals("snooze")) {
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(0);
            onSnooze();
            return START_STICKY;
        }

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

        if (timer != null) {
            timer.cancel();
        }

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

    private void onSnooze() {
        if (requestingLocationUpdates) {
            requestingLocationUpdates = false;
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, this);
        }

        SharedPreferences prefs = getSharedPreferences(getString(R.string.preference_file), MODE_PRIVATE);
        int time = 60;
        if (prefs != null) {
            time = prefs.getInt(getString(R.string.snooze_setting), -1);
        }

        timer = new CountDownTimer(time * MILLIS_PER_SEC, MILLIS_PER_SEC) {
            public void onTick(long millisUntilFinished) { return; }

            public void onFinish() {
                Intent startServiceIntent = new Intent(getApplicationContext(), NotificationService.class);
                startService(startServiceIntent);
            }
        }.start();
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

        SharedPreferences prefs = getSharedPreferences(getString(R.string.preference_file), MODE_PRIVATE);
        int radius = 100;
        if (prefs != null)
            radius = prefs.getInt(getString(R.string.radius_setting), -1);


        latitude = 42.2797577;
        longitude = -83.7408239;

        GooglePlacesRequest request = new GooglePlacesRequest();
        request.setLatitude(latitude);
        request.setLongitude(longitude);
        request.setRadius((double) radius);

        Place currentLoc = new Place();
        currentLoc.setLatitude(latitude);
        currentLoc.setLongitude(longitude);

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
            if (!item.enabled) continue;

            for (Category c : item.categories) {
                // Make sure the map entry is there
                Set<ReminderItem> s = categorySetMap.get(c);
                if (s == null) {
                    categorySetMap.put(c, new HashSet<ReminderItem>());
                }
                categorySetMap.get(c).add(item);
            }
        }

        if (categorySetMap.isEmpty()) return;

        // Add the google category names to the request
        for (Category c : categorySetMap.keySet()) {
            request.addType(c.toGoogleCategoryString());
        }

        // Make the API query
        ArrayList<Place> places = new GooglePlacesService(appContext).getNearbyPlaces(request);

        if (places.isEmpty()) return;

        double dist = Double.MAX_VALUE;
        Place closest = new Place();
        for (Place p : places) {
            double currDist = getDistanceFromLatLon(p.getLatitude(), p.getLongitude(), latitude, longitude);
            if (currDist < dist) {
                closest = p;
            }
        }

        HashSet<ReminderItem> toDisplay = new HashSet<>();

        for (Category c : closest.getRecognizedCategories()) {
            Set<ReminderItem> itemList = categorySetMap.get(c);
            if (itemList == null) continue;
            for (ReminderItem item : itemList) {
                if (item.inStore == true) {
                    if (getDistanceFromLatLon(closest.getLatitude(), closest.getLongitude(), latitude, longitude) < 10) {
                        toDisplay.add(item);
                    }
                } else {
                    toDisplay.add(item);
                }
            }
        }


        String message = "You are near " + closest.getName() + ". Do you want to pick up ";
        ArrayList<Integer> itemIds = new ArrayList<>();
        for (ReminderItem item : toDisplay) {
            message += item.itemName + ", ";
            itemIds.add(item.id);
        }

        message = message.substring(0, message.length() - 2) + "?";
        Log.d(getString(R.string.log_tag), "Notification message: \"" + message + "\"");
        fireNotification(appContext, "CloseBuy", itemIds, message, currentLoc, closest, R.mipmap.shop);
    }

    private double getDistanceFromLatLon(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Radius of the earth in km
        double dLat = deg2rad(lat2 - lat1);  // deg2rad below
        double dLon = deg2rad(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c; // Distance in km
        return d * 1000;
    }

    private double deg2rad(double deg) {
        return deg * (Math.PI/180);
    }

    private void fireNotification(Context context, String title, ArrayList<Integer> itemIds, String description, Place start, Place destination, int iconResource) {
        Intent homePageIntent = new Intent(context, HomeActivity.class);
        homePageIntent.setAction("0");
        homePageIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingHomePage = PendingIntent.getActivity(context, 0, homePageIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Bundle idBundle = new Bundle();
        idBundle.putIntegerArrayList("ids", itemIds);

        // Create the pending intent to disable items on homepage
        Intent homePageDisableIntent = new Intent(context, HomeActivity.class);
        homePageDisableIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        homePageDisableIntent.putExtra("methodName", "disableItem");
        homePageDisableIntent.putExtra("idBundle", idBundle);
        homePageDisableIntent.setAction("1");
        PendingIntent pendingHomePageDisable = PendingIntent.getActivity(context, 0, homePageDisableIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        // Create the pending intent to snooze app
        Intent snoozeIntent = new Intent(context, NotificationService.class);
        snoozeIntent.setAction("snooze");
        PendingIntent pendingSnooze = PendingIntent.getService(context, 0, snoozeIntent, 0);


        String mapsUrl = "http://maps.google.com/maps?saddr=" + start.getLatitude() + "," + start.getLongitude() +
                "&daddr=" + destination.getLatitude() + "," + destination.getLongitude();
        Intent mapIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(mapsUrl));
        //mapIntent.setAction("3");
        PendingIntent pendingMaps = PendingIntent.getActivity(context, 0, mapIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Create the notification builder and set the appropriate fields
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle(title);
        builder.setContentText(description);
        builder.setSmallIcon(iconResource);
        builder.setContentIntent(pendingHomePage);
        builder.setStyle(new Notification.BigTextStyle().bigText(description));
        builder.addAction(R.drawable.icon_check, "Done",  pendingHomePageDisable);
        builder.addAction(R.drawable.icon_snooze, "Snooze",  pendingSnooze);
        builder.addAction(R.drawable.icon_map, "Map", pendingMaps);
        builder.setVibrate(new long[]{1000, 1000});
        builder.setAutoCancel(true);

        // Fetch the notification service and fire the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }
}
