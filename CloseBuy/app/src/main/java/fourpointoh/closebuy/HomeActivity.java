package fourpointoh.closebuy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private final int CONTEXT_MENU_ID_DELETE = 0;
    private final int CONTEXT_MENU_ID_DISABLE = 1;
    private final int CONTEXT_MENU_ID_ENABLE = 2;

    private ArrayList<ReminderItem> reminderItems;
    private ReminderItemArrayAdapter adapter;
    private DbHandle dbHandle;
    private SharedPreferences preferences;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Log.d(getString(R.string.log_tag), "onCreate()");

        // Set the button pictures and click handlers
        FloatingActionButton add = (FloatingActionButton) findViewById(R.id.add);
        FloatingActionButton addText = (FloatingActionButton) findViewById(R.id.add_text);
        FloatingActionButton addPhoto = (FloatingActionButton) findViewById(R.id.add_photo);

        add.setImageResource(R.drawable.add_image);
        addText.setImageResource(R.drawable.add_text_image);
        addPhoto.setImageResource(R.drawable.add_photo_image);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(getString(R.string.log_tag), "Add button clicked");
                FloatingActionButton addText = (FloatingActionButton) findViewById(R.id.add_text);
                FloatingActionButton addPhoto = (FloatingActionButton) findViewById(R.id.add_photo);

                if (addText.getVisibility() == View.GONE) {
                    addText.setVisibility(View.VISIBLE);
                    addPhoto.setVisibility(View.VISIBLE);
                } else {
                    addText.setVisibility(View.GONE);
                    addPhoto.setVisibility(View.GONE);
                }

            }
        });

        addText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(getString(R.string.log_tag), "Add text button clicked");
                Intent intent = new Intent(HomeActivity.this, AddTextActivity.class);
                startActivity(intent);
            }
        });

        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(getString(R.string.log_tag), "Add photo button clicked");
                Toast.makeText(
                        getApplicationContext(),
                        getString(R.string.feature_not_ready),
                        Toast.LENGTH_LONG
                ).show();
            }
        });

        // Get a db handle
        dbHandle = ReminderItemDbHelper.getInstance(getApplicationContext());

        dbHandle.deleteAllItems();
        ArrayList<Category> cats = new ArrayList<>();
        cats.add(Category.GROCERY);
        cats.add(Category.BAKERY);
        dbHandle.addItem("Cheese", cats);
        dbHandle.addItem("Soup", cats);
        dbHandle.addItem("Bread", cats);
        dbHandle.addItem("Milk", cats);

        // Set the reminder items to non null
        reminderItems = new ArrayList<ReminderItem>();

        // Get the list view
        ListView listView = (ListView) findViewById(R.id.item_list);

        // Define and set the adapter
        adapter = new ReminderItemArrayAdapter(this, R.layout.list_item, reminderItems);
        listView.setAdapter(adapter);

        // Set the click listeners for the list
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(getString(R.string.log_tag), "Position " + position);
            }
        });

        // Register the list view to create context menus when list items are long pressed
        registerForContextMenu(listView);

        // Initialize the preferences
        preferences = getPreferences(Context.MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(getString(R.string.log_tag), "onResume()");

        updateList();

        FloatingActionButton addText = (FloatingActionButton) findViewById(R.id.add_text);
        FloatingActionButton addPhoto = (FloatingActionButton) findViewById(R.id.add_photo);
        addText.setVisibility(View.GONE);
        addPhoto.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(getString(R.string.log_tag), "onDestroy()");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(getString(R.string.log_tag), "onCreateOptionsMenu()");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);
        super.onCreateOptionsMenu(menu);

        MenuItem item = menu.findItem(R.id.action_notifications);

        // Start the notification service if it the first time opening the app
        boolean isFirstLaunch = preferences.getBoolean(getString(R.string.first_app_launch), true);
        if (isFirstLaunch) {
            // Start the notification service
            Log.d(getString(R.string.log_tag), "First launch, starting notification service");
            Intent startServiceIntent = new Intent(getApplicationContext(), NotificationService.class);
            startService(startServiceIntent);

            // Set the first launch preference = false
            preferences.edit().putBoolean(getString(R.string.first_app_launch), false).apply();
        }

        // Set the correct notification setting title based on preferences
        if (isNotificationSettingOn()) {
            item.setTitle(getString(R.string.turn_notifications_off));
            Log.d(getString(R.string.log_tag), "Notifications are on");
        } else {
            item.setTitle(getString(R.string.turn_notifications_on));
            Log.d(getString(R.string.log_tag), "Notifications are off");
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_notifications:
                // Check the current notification setting, and switch it
                boolean isNotificationsOn = isNotificationSettingOn();

                if (isNotificationsOn) {
                    // Turn notifications off
                    Log.d(getString(R.string.log_tag), "Turning notifications off");
                    stopService(new Intent(getApplicationContext(), NotificationService.class));

                    menuItem.setTitle(getString(R.string.turn_notifications_on));
                } else {
                    // Turn notifications on
                    Log.d(getString(R.string.log_tag), "Turning notifications on");
                    Intent startServiceIntent = new Intent(getApplicationContext(), NotificationService.class);
                    startService(startServiceIntent);

                    menuItem.setTitle(getString(R.string.turn_notifications_off));
                }

                setNotificationSetting(!isNotificationsOn);
                return true;
            default:
                Log.d(getString(R.string.log_tag), "unknown menu item clicked");
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        Log.d(getString(R.string.log_tag), "onCreateContextMenu()");
        AdapterView.AdapterContextMenuInfo adapterInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;

        ReminderItem selected = reminderItems.get(adapterInfo.position);
        menu.setHeaderTitle(selected.itemName);
        if (selected.enabled) menu.add(0, CONTEXT_MENU_ID_DISABLE, 0, "Disable Reminder"); // groupid, itemid, menu position, title
        else menu.add(0, CONTEXT_MENU_ID_ENABLE, 0, "Enable Reminder");
        menu.add(0, CONTEXT_MENU_ID_DELETE, 1, "Delete Reminder");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.d(getString(R.string.log_tag), "onContextItemSelected(): id=" + item.getItemId());
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case CONTEXT_MENU_ID_DISABLE:
                disableReminder(info.position);
                return true;
            case CONTEXT_MENU_ID_ENABLE:
                enableReminder(info.position);
                return true;
            case CONTEXT_MENU_ID_DELETE:
                deleteReminder(info.position);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void deleteReminder(int arrayAdapterPosition) {
        Log.d(getString(R.string.log_tag), "User is deleting reminder item: " + reminderItems.get(arrayAdapterPosition).itemName);
        ReminderItem item = reminderItems.get(arrayAdapterPosition);
        reminderItems.remove(arrayAdapterPosition);
        dbHandle.deleteItem(item);
        updateList();
    }

    private void disableReminder(int arrayAdapterPosition) {
        Log.d(getString(R.string.log_tag), "User is disabling reminder item: " + reminderItems.get(arrayAdapterPosition).itemName);
        ReminderItem item = reminderItems.get(arrayAdapterPosition);
        dbHandle.disableItem(item);
        updateList();
    }

    private void enableReminder(int arrayAdapterPosition) {
        Log.d(getString(R.string.log_tag), "User is disabling reminder item: " + reminderItems.get(arrayAdapterPosition).itemName);
        ReminderItem item = reminderItems.get(arrayAdapterPosition);
        dbHandle.enableItem(item);
        updateList();
    }

    private void updateList() {
        TextView t = (TextView) findViewById(R.id.no_reminders);

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
            t.setVisibility(View.VISIBLE);
        } else {
            t.setVisibility(View.GONE);
        }
    }

    private boolean isNotificationSettingOn() {
        boolean defaultValue = getResources().getBoolean(R.bool.default_notification_setting);
        return preferences.getBoolean(getString(R.string.notification_setting), defaultValue);
    }

    private void setNotificationSetting(boolean value) {
        preferences.edit().putBoolean(getString(R.string.notification_setting), value).apply();
    }
}
