package fourpointoh.closebuy;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Paint;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;


import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private final int CONTEXT_MENU_ID_DELETE = 0;
    private final int CONTEXT_MENU_ID_DISABLE = 1;
    private final int CONTEXT_MENU_ID_ENABLE = 2;

    private final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean hasLocationPermission = false;

    private ArrayList<ReminderItem> reminderItemsNearby;
    private ReminderItemArrayAdapter adapterNearby;
    private SwipeMenuListView listViewNearby;

    private ArrayList<ReminderItem> reminderItemsInStore;
    private ReminderItemArrayAdapter adapterInStore;
    private SwipeMenuListView listViewInStore;

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

        // Set the reminder items to non null
        reminderItemsNearby = new ArrayList<ReminderItem>();
        reminderItemsInStore = new ArrayList<ReminderItem>();

        // Get the list view
        listViewNearby = (SwipeMenuListView) findViewById(R.id.nearby_item_list);
        listViewInStore = (SwipeMenuListView) findViewById(R.id.in_store_item_list);

        // Define and set the adapter
        adapterNearby = new ReminderItemArrayAdapter(this, R.layout.list_item, reminderItemsNearby);
        adapterInStore = new ReminderItemArrayAdapter(this, R.layout.list_item, reminderItemsInStore);
        listViewNearby.setAdapter(adapterNearby);
        listViewInStore.setAdapter(adapterInStore);

        SwipeMenuCreator creator = new MySwipeMenuCreator(this);
        listViewNearby.setMenuCreator(creator);
        listViewInStore.setMenuCreator(creator);
        listViewNearby.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
        listViewInStore.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);

        // Set the click listeners for the list
        // single click/tap on item will go to item's edit page
        listViewNearby.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(getString(R.string.log_tag), "Item " + position + " on list going to edit page.");
                Intent intent = new Intent(HomeActivity.this, EditTextActivity.class);
                int itemId = reminderItemsNearby.get(position).id;
                Log.d(getString(R.string.log_tag), "item id: " + itemId);
                intent.putExtra("position_id", itemId);
                startActivity(intent);
            }
        });
        listViewInStore.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(getString(R.string.log_tag), "Item " + position + " on list going to edit page.");
                Intent intent = new Intent(HomeActivity.this, EditTextActivity.class);
                int itemId = reminderItemsInStore.get(position).id;
                Log.d(getString(R.string.log_tag), "item id: " + itemId);
                intent.putExtra("position_id", itemId);
                startActivity(intent);
            }
        });

        listViewNearby.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        ReminderItem item = reminderItemsNearby.get(position);
                        if (item.enabled) {
                            disableReminder(position, reminderItemsNearby, listViewNearby);
                        } else {
                            enableReminder(position, reminderItemsNearby, listViewNearby);
                        }
                        break;
                    case 1:
                        deleteReminder(position, reminderItemsNearby);
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
        listViewInStore.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        ReminderItem item = reminderItemsInStore.get(position);
                        if (item.enabled) {
                            disableReminder(position, reminderItemsInStore, listViewInStore);
                        } else {
                            enableReminder(position, reminderItemsInStore, listViewInStore);
                        }
                        break;
                    case 1:
                        deleteReminder(position, reminderItemsInStore);
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });

        // Get a db handle
        dbHandle = ReminderItemDbHelper.getInstance(getApplicationContext());

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

    public void deleteReminder(int arrayAdapterPosition, ArrayList<ReminderItem> array) {
        Log.d(getString(R.string.log_tag), "User is deleting reminder item: " + array.get(arrayAdapterPosition).itemName);
        ReminderItem item = array.get(arrayAdapterPosition);
        array.remove(arrayAdapterPosition);
        dbHandle.deleteItem(item);
        updateList();
    }

    public void disableReminder(int arrayAdapterPosition, ArrayList<ReminderItem> array, SwipeMenuListView listView) {
        Log.d(getString(R.string.log_tag), "User is disabling reminder item: " + array.get(arrayAdapterPosition).itemName);

        // Change the UI on that item
        TextView v = (TextView) listView.getChildAt(arrayAdapterPosition - listView.getFirstVisiblePosition()).findViewById(R.id.item_name);
        v.setPaintFlags(v.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        v.setTextColor(getResources().getColor(R.color.colorDisabledListItemText));

        // Update the data representation
        ReminderItem item = array.get(arrayAdapterPosition);
        dbHandle.disableItem(item);
        updateList();
    }

    public void enableReminder(int arrayAdapterPosition, ArrayList<ReminderItem> array, SwipeMenuListView listView) {
        Log.d(getString(R.string.log_tag), "User is enabling reminder item: " + array.get(arrayAdapterPosition).itemName);

        // Change the UI on that item
        TextView v = (TextView) listView.getChildAt(arrayAdapterPosition - listView.getFirstVisiblePosition()).findViewById(R.id.item_name);
        v.setPaintFlags(v.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        v.setTextColor(getResources().getColor(R.color.colorListItemText));

        // Update the data representation
        ReminderItem item = array.get(arrayAdapterPosition);
        dbHandle.enableItem(item);
        updateList();
    }

    private void updateList() {

        // Retrieve reminder items from DB
        ArrayList<ReminderItem> refreshedItems = dbHandle.getAllItems();

        // DEBUG
        String itemNames = "";
        for (int i = 0; i < refreshedItems.size(); ++i) {
            itemNames += ", " + refreshedItems.get(i).itemName;
        }
        Log.d(getString(R.string.log_tag), "Update list: " + refreshedItems.size() + " items pulled from the db: " + itemNames);

        // Re-use reminderItems member, since it is attached to the adapter
        reminderItemsNearby.clear();
        reminderItemsInStore.clear();

        // Add each item to the correct list
        for (ReminderItem item : refreshedItems) {
            if (item.inStore) {
                reminderItemsInStore.add(item);
            } else {
                reminderItemsNearby.add(item);
            }
        }

        // Notify the adapters to refresh
        adapterNearby.notifyDataSetChanged();
        adapterInStore.notifyDataSetChanged();

        ListUtils.setDynamicHeight(listViewNearby);
        ListUtils.setDynamicHeight(listViewInStore);

        View noRemindersView = findViewById(R.id.no_reminders);
        View nearbyHeader = findViewById(R.id.nearby_list_header);
        View inStoreHeader = findViewById(R.id.in_store_list_header);

        // Show or hide the "no reminders" view and list headers
        if (reminderItemsNearby.isEmpty()) {
            nearbyHeader.setVisibility(View.GONE);
        } else {
            nearbyHeader.setVisibility(View.VISIBLE);
        }
        if (reminderItemsInStore.isEmpty()) {
            inStoreHeader.setVisibility(View.GONE);
        } else {
            inStoreHeader.setVisibility(View.VISIBLE);
        }
        if (reminderItemsNearby.isEmpty() && reminderItemsInStore.isEmpty()) {
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

    public static class ListUtils {
        public static void setDynamicHeight(ListView mListView) {
            ListAdapter mListAdapter = mListView.getAdapter();
            if (mListAdapter == null) {
                // when adapter is null
                return;
            }
            int height = 0;
            int desiredWidth = View.MeasureSpec.makeMeasureSpec(mListView.getWidth(), View.MeasureSpec.UNSPECIFIED);
            for (int i = 0; i < mListAdapter.getCount(); i++) {
                View listItem = mListAdapter.getView(i, null, mListView);
                listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                height += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = mListView.getLayoutParams();
            params.height = height + (mListView.getDividerHeight() * (mListAdapter.getCount() - 1));
            mListView.setLayoutParams(params);
            mListView.requestLayout();
        }
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