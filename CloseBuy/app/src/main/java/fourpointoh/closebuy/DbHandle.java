package fourpointoh.closebuy;

import java.util.ArrayList;

/**
 * Created by nick on 26/10/15.
 */
public interface DbHandle {

    // Returns all reminder items in the DB.
    ArrayList<ReminderItem> getAllItems();

    // Adds a reminder item to the DB.
    // Duplicate (item name, category list) reminders can be added to the DB.
    void addItem(String itemName, boolean inStore, ArrayList<Category> categories);

    // Edits a reminder item in the DB.
    void editItem(ReminderItem item);

    // Deletes a reminder item from the DB.
    // If 'item' is not present in the DB, nothing will happen.
    void deleteItem(ReminderItem item);

    // Delete all items in list
    void deleteAllItems();

    // Disable notifications for an item
    void disableItem(ReminderItem item);

    // Enable notifications for an item
    void enableItem(ReminderItem item);
}
