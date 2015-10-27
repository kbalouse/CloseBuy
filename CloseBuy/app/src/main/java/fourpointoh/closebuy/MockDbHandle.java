package fourpointoh.closebuy;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by nick on 24/10/15.
 *
 * Simple in-memory mock version of the DbHandle class.
 * Used for testing.
 * Each MockDbHandle instance will serve as its own database (No singleton).
 * The mock db starts with a few reminder items already in it.
 */
public class MockDbHandle implements DbHandle {
    private ArrayList<ReminderItem> items;
    private int idCounter;

    public MockDbHandle(Context context) {
        items = new ArrayList<ReminderItem>();
        idCounter = 0;

        // Add some items to simulate a populated db
        ReminderItem newItem = new ReminderItem();
        newItem.id = idCounter++;
        newItem.itemName = "Band-aids";
        ArrayList<Category> cats = new ArrayList<Category>();
        cats.add(Category.PHARMACY);
        cats.add(Category.GROCERY);
        newItem.categories =cats;
        items.add(newItem);

        ReminderItem newItem2 = new ReminderItem();
        newItem2.id = idCounter++;
        cats = new ArrayList<Category>();
        newItem2.itemName = "Bread";
        cats.add(Category.BAKERY);
        cats.add(Category.GROCERY);
        newItem2.categories = cats;
        items.add(newItem2);
    }

    public ArrayList<ReminderItem> getAllItems() {
        return items;
    }

    public void addItem(String itemName, ArrayList<Category> categories) {
        ReminderItem newItem = new ReminderItem();
        newItem.id = idCounter++;
        newItem.categories = categories;
        items.add(newItem);
    }

    public ArrayList<ReminderItem> getItemsByCategory(Category category) {
        ArrayList<ReminderItem> selectedItems = new ArrayList<>();

        for (int i = 0; i < items.size(); ++i) {
            if (items.get(i).categories.contains(category)) {
                selectedItems.add(items.get(i));
            }
        }

        return selectedItems;
    }

    public void deleteItem(ReminderItem item) {
        // Sort categories list to do equality comparison
        Collections.sort(item.categories);

        for (int i = 0; i < items.size(); ++i) {
            ReminderItem r = items.get(i);
            Collections.sort(r.categories);

            if (r.id == item.id && r.itemName.equals(item.itemName) && r.categories == item.categories) {
                items.remove(i);
            }
        }
    }

}
