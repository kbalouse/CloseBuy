package fourpointoh.closebuy;

import java.util.ArrayList;

/**
 * Created by nick on 24/10/15.
 */
public class ReminderItem implements Comparable<ReminderItem> {
    public int id;
    public boolean enabled;
    public boolean inStore;
    public String itemName;
    public ArrayList<Category> categories;

    public int compareTo(ReminderItem item) {
        return Integer.compare(this.id, item.id);
    }
}
