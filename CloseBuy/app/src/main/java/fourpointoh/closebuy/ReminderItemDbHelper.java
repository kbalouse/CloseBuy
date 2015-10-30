package fourpointoh.closebuy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by Kyle on 10/29/2015.
 */
public class ReminderItemDbHelper extends SQLiteOpenHelper implements DbHandle {
    private static ReminderItemDbHelper Instance;

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ReminderItem.db";

    // Table names
    public static final String TABLE_CATEGORY = "Category";
    public static final String TABLE_ITEM = "Item";
    public static final String TABLE_CONTAINS = "Contains";

    // Category table columns
    public static final String CATEGORY_ID = "category_id";
    public static final String CATEGORY_NAME = "name";

    // Category table columns
    public static final String ITEM_ID = "item_id";
    public static final String ITEM_NAME = "name";

    // Schema creation SQL
    private static final String SQL_CREATE_CATEGORY_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_CATEGORY + " (" +
                    CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    CATEGORY_NAME + " TEXT" +
            " )";

    private static final String SQL_CREATE_ITEM_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_ITEM + " (" +
                    ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    ITEM_NAME + " TEXT" +
                    " )";

    private static final String SQL_CREATE_CONTAINS_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_CATEGORY + " (" +
                    CATEGORY_ID + " INTEGER NOT NULL," +
                    ITEM_ID + " INTEGER NOT NULL," +
                    "FOREIGN KEY(" + CATEGORY_ID + ") REFERENCES " +
                        TABLE_CATEGORY + "(" + CATEGORY_ID + ") ON DELETE CASCADE," +
                    "FOREIGN KEY(" + ITEM_ID + ") REFERENCES " +
                        TABLE_ITEM + "(" + ITEM_ID + ") ON DELETE CASCADE" +
                    " )";

    // Table deletion SQL
    private static final String SQL_DELETE_CATEGORY =
            "DROP TABLE IF EXISTS " + TABLE_CATEGORY;

    private static final String SQL_DELETE_ITEM =
            "DROP TABLE IF EXISTS " + TABLE_ITEM;

    private static final String SQL_DELETE_CONTAINS =
            "DROP TABLE IF EXISTS " + TABLE_CONTAINS;


    public static synchronized ReminderItemDbHelper getInstance(Context context) {
        if (Instance == null) {
            Instance = new ReminderItemDbHelper(context.getApplicationContext());
        }
        return Instance;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_CATEGORY_TABLE);
        db.execSQL(SQL_CREATE_ITEM_TABLE);
        db.execSQL(SQL_CREATE_CONTAINS_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // If database schema is changed, delete all existing data
        db.execSQL(SQL_DELETE_CONTAINS);
        db.execSQL(SQL_DELETE_ITEM);
        db.execSQL(SQL_DELETE_CATEGORY);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    // Returns all reminder items in the DB.
    public ArrayList<ReminderItem> getAllItems() {
        ArrayList<ReminderItem> itemList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_ITEM;

        SQLiteDatabase db = Instance.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                ReminderItem item = new ReminderItem();
                item.id = cursor.getInt((cursor.getColumnIndex(ITEM_ID)));
                item.itemName = cursor.getString(cursor.getColumnIndex(ITEM_NAME));
                itemList.add(item);
            } while (cursor.moveToNext());
        }
        return itemList;
    }

    // Returns all reminder items in the DB that have a category label of the 'category' parameter.
    public ArrayList<ReminderItem> getItemsByCategory(Category category) {
        ArrayList<ReminderItem> itemList = new ArrayList<>();
        return itemList;
    }

    // Adds a reminder item to the DB.
    // Duplicate (item name, category list) reminders can be added to the DB.
    public void addItem(String itemName, ArrayList<Category> categories) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ITEM_NAME, itemName);
        long itemId = db.insert(TABLE_ITEM, null, values);

        for (Category cat : categories) {
            ContentValues catValues = new ContentValues();
            catValues.put(CATEGORY_ID, cat.getId());
            catValues.put(ITEM_ID, itemId);
            db.insert(TABLE_CONTAINS, null, catValues);
        }
    }

    // Deletes a reminder item from the DB.
    // If 'item' is not present in the DB, nothing will happen.
    public void deleteItem(ReminderItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ITEM, ITEM_ID + " = ?",
                new String[] { String.valueOf(item.id) });
    }

    public void deleteAllItems() {
        SQLiteDatabase db = Instance.getWritableDatabase();
        db.execSQL(SQL_DELETE_CONTAINS);
        db.execSQL(SQL_DELETE_ITEM);
        db.execSQL(SQL_DELETE_CATEGORY);
        onCreate(db);
    }

    private ReminderItemDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}
