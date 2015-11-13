package fourpointoh.closebuy;

/**
 * Created by nick on 24/10/15.
 */
public enum Category {
    GROCERY(1), PHARMACY(2), BAKERY(3), CONVENIENCE(4), PET(5), ELECTRONICS(6), HARDWARE(7); // new categories must be added incrementally by 1
    public final int id;
    Category(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    // Returns the string that is associated with the particular category, according to google's definitions.
    // https://developers.google.com/places/supported_types
    public String toGoogleCategoryString() {
        switch (this) {
            case GROCERY:
                return "grocery_or_supermarket";
            case PHARMACY:
                return "pharmacy";
            case BAKERY:
                return "bakery";
            case CONVENIENCE:
                return "convenience_store";
            case PET:
                return "pet_store";
            case ELECTRONICS:
                return "electronics_store";
            case HARDWARE:
                return "hardware_store";
            default:
                return "";
        }
    }

    // Returns the category enum associated with the specific category string.
    // Inverse of toGoogleCategoryString()
    // https://developers.google.com/places/supported_types
    public static Category toCategory(String s) {
        if (s.equals("grocery_or_supermarket")) {
            return GROCERY;
        } else if (s.equals("pharmacy")) {
            return PHARMACY;
        } else if (s.equals("bakery")) {
            return BAKERY;
        } else if (s.equals("convenience_store")) {
            return CONVENIENCE;
        } else if (s.equals("pet_store")) {
            return PET;
        } else if (s.equals("electronics_store")) {
            return ELECTRONICS;
        } else if (s.equals("hardware_store")) {
            return HARDWARE;
        } else {
            return null;
        }
    }
}