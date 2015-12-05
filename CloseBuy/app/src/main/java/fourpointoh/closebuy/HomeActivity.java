package fourpointoh.closebuy;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Paint;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity implements ReminderItemActionListener {

    private final int CONTEXT_MENU_ID_DELETE = 0;
    private final int CONTEXT_MENU_ID_DISABLE = 1;
    private final int CONTEXT_MENU_ID_ENABLE = 2;

    private final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean hasLocationPermission = false;

    private ArrayList<ReminderItem> reminderItems;
    private ReminderItemArrayAdapter adapter;
    private SwipeMenuListView listView;
    private DbHandle dbHandle;
    private SharedPreferences preferences;

    private Switch notificationSwitch;
    private SeekBar radiusSeekbar;
    private SeekBar snoozeSeekbar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Log.d(getString(R.string.log_tag), "onCreate()");

        // Initialize the preferences
        preferences = getPreferences(Context.MODE_PRIVATE);

        // Set the add button click handler
        View add = (View) findViewById(R.id.add_button);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, AddTextActivity.class);
                startActivity(intent);
            }
        });

        notificationSwitch = (Switch) findViewById(R.id.notification_switch);
        radiusSeekbar = (SeekBar) findViewById(R.id.radius_seekbar);
        snoozeSeekbar = (SeekBar) findViewById(R.id.snooze_seekbar);

        // Set listeners for settings menu
        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && !isNotificationSettingOn()) {
                    // Turn notifications on
                    Log.d(getString(R.string.log_tag), "Turning notifications on");
                    setNotificationSetting(true);
                    Intent startServiceIntent = new Intent(getApplicationContext(), NotificationService.class);
                    startService(startServiceIntent);
                } else if (!isChecked && isNotificationSettingOn()) {
                    // Turn notifications off
                    Log.d(getString(R.string.log_tag), "Turning notifications off");
                    setNotificationSetting(false);
                    stopService(new Intent(getApplicationContext(), NotificationService.class));
                }
            }
        });

        radiusSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView tv = (TextView) findViewById(R.id.radius_setting_text);
                tv.setText(progress + " meters");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int value = seekBar.getProgress();
                Log.d(getString(R.string.log_tag), "set radius setting " + value);
                setRadiusSetting(seekBar.getProgress());
            }
        });

        snoozeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView tv = (TextView) findViewById(R.id.snooze_setting_text);
                tv.setText(progress + " minutes");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int value = seekBar.getProgress();
                Log.d(getString(R.string.log_tag), "set snooze setting " + value);
                setSnoozeSetting(value);
            }
        });

        // Prevent left swipe on seek bar to close the drawer
        radiusSeekbar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                radiusSeekbar.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        snoozeSeekbar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                snoozeSeekbar.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        // Settings menu button callback
        View menuButton = (View) findViewById(R.id.menu_button);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View drawer = findViewById(R.id.sliding_drawer);
                DrawerLayout root = (DrawerLayout) findViewById(R.id.root_layout);
                root.openDrawer(drawer);
            }
        });

        // Initialize settings UI controls according to saved settings
        initializeSettingsUI();

        // Get a db handle
        dbHandle = ReminderItemDbHelper.getInstance(getApplicationContext());

        // Set the reminder items to non null
        reminderItems = new ArrayList<ReminderItem>();

        // Get the list view
        listView = (SwipeMenuListView) findViewById(R.id.item_list);

        // Define and set the adapter
        adapter = new ReminderItemArrayAdapter(this, R.layout.list_item, reminderItems);
        listView.setAdapter(adapter);

        SwipeMenuCreator creator = new MySwipeMenuCreator(this);
        listView.setMenuCreator(creator);
        listView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);

        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
            switch (index) {
                case 0:
                    ReminderItem item = reminderItems.get(position);
                    if (item.enabled) {
                        disableReminder(position);
                    } else {
                        enableReminder(position);
                    }
                    break;
                case 1:
                    deleteReminder(position);
                    break;
            }
            // false : close the menu; true : not close the menu
            return false;
            }
        });

        // Register the list view to create context menus when list items are long pressed
        registerForContextMenu(listView);

        // Need to ask permission in Android devices 6.0 (API 23) or higher
        askUserForLocationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(getString(R.string.log_tag), "onResume()");

        updateList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(getString(R.string.log_tag), "onDestroy()");
    }

    private void initializeNotificationService() {
        // Start the notification service if it the first time opening the app
        boolean isFirstLaunch = preferences.getBoolean(getString(R.string.first_app_launch), true);
        if (isFirstLaunch) {
            // Start the notification service
            Log.d(getString(R.string.log_tag), "First launch, starting notification service");
            setNotificationSetting(true);
            Intent startServiceIntent = new Intent(getApplicationContext(), NotificationService.class);
            startService(startServiceIntent);

            // Set the first launch preference = false
            preferences.edit().putBoolean(getString(R.string.first_app_launch), false).apply();
        }

        // Set the correct notification setting title based on preferences
        if (isNotificationSettingOn()) {
            notificationSwitch.setChecked(true);
            Log.d(getString(R.string.log_tag), "Notifications are on");
        } else {
            notificationSwitch.setChecked(false);
            Log.d(getString(R.string.log_tag), "Notifications are off");
        }
    }

    public void deleteReminder(int arrayAdapterPosition) {
        Log.d(getString(R.string.log_tag), "User is deleting reminder item: " + reminderItems.get(arrayAdapterPosition).itemName);
        ReminderItem item = reminderItems.get(arrayAdapterPosition);
        reminderItems.remove(arrayAdapterPosition);
        dbHandle.deleteItem(item);
        updateList();
    }

    public void disableReminder(int arrayAdapterPosition) {
        Log.d(getString(R.string.log_tag), "User is disabling reminder item: " + reminderItems.get(arrayAdapterPosition).itemName);

        // Change the UI on that item
        TextView v = (TextView) listView.getChildAt(arrayAdapterPosition - listView.getFirstVisiblePosition()).findViewById(R.id.item_name);
        v.setPaintFlags(v.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        v.setTextColor(getResources().getColor(R.color.colorDisabledListItemText));

        // Update the data representation
        ReminderItem item = reminderItems.get(arrayAdapterPosition);
        dbHandle.disableItem(item);
        updateList();
    }

    public void enableReminder(int arrayAdapterPosition) {
        Log.d(getString(R.string.log_tag), "User is enabling reminder item: " + reminderItems.get(arrayAdapterPosition).itemName);

        // Change the UI on that item
        TextView v = (TextView) listView.getChildAt(arrayAdapterPosition - listView.getFirstVisiblePosition()).findViewById(R.id.item_name);
        v.setPaintFlags(v.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        v.setTextColor(getResources().getColor(R.color.colorListItemText));

        // Update the data representation
        ReminderItem item = reminderItems.get(arrayAdapterPosition);
        dbHandle.enableItem(item);
        updateList();
    }

    private void updateList() {
        View noRemindersView = findViewById(R.id.no_reminders);

        // Retrieve reminder items from DB
        ArrayList<ReminderItem> refreshedItems = dbHandle.getAllItems();

        // DEBUG
        String itemNames = "";
        for (int i = 0; i < refreshedItems.size(); ++i) {
            itemNames += ", " + refreshedItems.get(i).itemName;
        }
        Log.d(getString(R.string.log_tag), "Update list: " + refreshedItems.size() + " items pulled from the db: " + itemNames);

        // Re-use reminderItems member, since it is attached to the adapter
        reminderItems.clear();
        for (int i = 0; i < refreshedItems.size(); ++i) {
            reminderItems.add(refreshedItems.get(i));
        }

        // Notify the adapter to refresh
        adapter.notifyDataSetChanged();

        // Show or hide the "no reminders" text
        if (reminderItems.size() == 0) {
            noRemindersView.setVisibility(View.VISIBLE);
        } else {
            noRemindersView.setVisibility(View.GONE);
        }
    }

    private boolean isNotificationSettingOn() {
        boolean defaultValue = getResources().getBoolean(R.bool.default_notification_setting);
        return preferences.getBoolean(getString(R.string.notification_setting), defaultValue);
    }

    private void setNotificationSetting(boolean value) {
        preferences.edit().putBoolean(getString(R.string.notification_setting), value).apply();
    }

    private int getRadiusSetting() {
        int defaultValue = getResources().getInteger(R.integer.default_radius_setting);
        return preferences.getInt(getString(R.string.radius_setting), defaultValue);
    }

    private void setRadiusSetting(int value) {
        preferences.edit().putInt(getString(R.string.radius_setting), value).apply();
    }

    private int getSnoozeSetting() {
        int defaultValue = getResources().getInteger(R.integer.default_snooze_setting);
        return preferences.getInt(getString(R.string.snooze_setting), defaultValue);
    }

    private void setSnoozeSetting(int value) {
        preferences.edit().putInt(getString(R.string.snooze_setting), value).apply();
    }

    private void askUserForLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Log.d(getString(R.string.log_tag), "Permissions: app needs to show request permission rationale");

            } else {

                // No explanation needed, we can request the permission.
                Log.d(getString(R.string.log_tag), "Permissions: app does not need permission rationale. requesting permissions...");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            Log.d(getString(R.string.log_tag), "Permissions: no need for request");
            hasLocationPermission = true;
            initializeNotificationService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d(getString(R.string.log_tag), "Permissions: Granted. Initializing notification service.");
                    hasLocationPermission = true;
                    initializeNotificationService();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    hasLocationPermission = false;

                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        Log.d(getString(R.string.log_tag), "Permissions: Denied.");
                    } else {
                        Log.d(getString(R.string.log_tag), "Permissions: Denied. Other");
                    }
                }
            }
        }
    }

    private void initializeSettingsUI() {
        int radius = getRadiusSetting();
        int snooze = getSnoozeSetting();

        radiusSeekbar.setProgress(radius);
        snoozeSeekbar.setProgress(snooze);
    }

    public static int dp2px(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return (int)px;
    }

    public static int px2dp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return (int)dp;
    }
}