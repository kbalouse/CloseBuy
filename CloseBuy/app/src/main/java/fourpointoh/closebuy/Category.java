package fourpointoh.closebuy;

/**
 * Created by nick on 24/10/15.
 */
public enum Category {
    GROCERY(1), PHARMACY(2), BAKERY(3);
    public final int id;
    Category(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
