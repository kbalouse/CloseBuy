package fourpointoh.closebuy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import java.util.ArrayList;
import android.widget.CheckBox;
import android.widget.Toast;


/**
 * Created by josh on 29/11/2015.
 */
public class EditTextActivity extends AppCompatActivity {
    private Switch mySwitch;
    private DbHandle dbHandle;
    private ArrayList<ReminderItem> itemList;
    private EditText editText;
    private Button doneButton;
    private ArrayList<Category> checkedCategories;
    private int item_id;
    private boolean inStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("log_tag", "The EditTextActivity onCreate() event");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_text);
        mySwitch = (Switch) findViewById(R.id.mySwitch);
        editText = (EditText) findViewById(R.id.edit_message);
        doneButton = (Button) findViewById(R.id.btnDone);
        checkedCategories = new ArrayList<Category>();
        // Get a db handle
        dbHandle = ReminderItemDbHelper.getInstance(getApplicationContext());
        Intent intent = getIntent();
        // get data via the key = "position_id"
        item_id = intent.getIntExtra("position_id", 0);
        Log.d("log_tag", "THE ITEM ID IS " + item_id);
        // pull reminder item from DB based on listview position id
        itemList = dbHandle.getAllItems(); // write function for getItemByID

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) Log.d(getString(R.string.log_tag), "ActionBar is null");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        // TODO: Just pass the whole ReminderItem to the activity so we don't have to query DB and search
        ReminderItem item = null;
        for (ReminderItem i : itemList) {
            if (i.id == item_id) {
                item = i;
                break;
            }
        }
        if (item == null) {
            Log.d(getString(R.string.log_tag), "ERROR: item not found in db");
        }

        // Initialize the checked boxes list with the categories that the item has
        checkedCategories = item.categories;

        // update view with item pulled from DB
        updateView(item);

        //attach a listener to check for changes in state
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    inStore = true;
                } // if
                else {
                    inStore = false;
                } // else

            } // onCheckedChanged
        }); // setOnCheckedChangeListener

        // change it to function call later instead of definition
        // unlike AddTextActivity, don't need to add to DB
        doneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(getString(R.string.log_tag), "Done button clicked");

                // Enforce that the necessary fields are non empty
                String name = editText.getText().toString();
                if (name.equals("")) {
                    Toast.makeText(getApplicationContext(), "Item must have a non-empty name.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (checkedCategories.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Item must be associated with at least 1 category.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // update item in DB, boolean "update" = true
                ReminderItem edited = new ReminderItem();
                edited.categories = checkedCategories;
                edited.inStore = inStore;
                edited.itemName = name;
                edited.enabled = true;
                edited.id = item_id;

                dbHandle.editItem(edited);

                // Return back to the home screen
                finish();
            }
        });

    } // onCreate

    // back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Log.d(getString(R.string.log_tag), "Back button clicked");
                new AlertDialog.Builder(this)
                        .setTitle("Discard Changes?")
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                finish();
                            }
                        }).create().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateView(ReminderItem item) {
        updateEditText(item.itemName);
        updateInStore(item.inStore);
        updateCategories(item.categories);
    }

    public void updateEditText(String itemName) {
        EditText edit_message = (EditText) findViewById(R.id.edit_message);
        edit_message.setText(itemName);
    }

    public void updateInStore(boolean inStore_new) {
        inStore = inStore_new;
        mySwitch.setChecked(inStore);
    }

    public void updateCategories(ArrayList<Category> categories) {
        CheckBox checkBox;
        for (Category c: categories) {
           int category = c.getId(); // ENUM
                // set checkbox view to checked according to category
                // add additional statements once we finalize categories
                // make sure category ENUM corresponds to right R.id.(refer to "Category")
                if (category == 1) {
                    checkBox = (CheckBox)findViewById(R.id.grocery);
                    Log.d("log_tag", "grocery view id = " + R.id.grocery);
                    checkBox.setChecked(!checkBox.isChecked());
                }
                else if (category == 2) {
                    checkBox = (CheckBox)findViewById(R.id.pharmacy);
                    Log.d("log_tag", "pharmacy view id = " + R.id.pharmacy);
                    checkBox.setChecked(!checkBox.isChecked());
                }
                else if (category == 4) {
                    checkBox = (CheckBox)findViewById(R.id.convenience);
                    Log.d("log_tag", "convenience view id = " + R.id.convenience);
                    checkBox.setChecked(!checkBox.isChecked());
                }
                else if (category == 5) {
                    checkBox = (CheckBox)findViewById(R.id.petCare);
                    Log.d("log_tag", "petCare view id = " + R.id.petCare);
                    checkBox.setChecked(!checkBox.isChecked());
                }
        } // for

    } //updateCategories

    // change it to function call later instead of definition
    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();
        Log.d(getString(R.string.log_tag), "checkbox " + view.getId() + " clicked!");
        Log.d(getString(R.string.log_tag), "R.id.grocery =  " + R.id.grocery);

        // Check which checkbox was clicked
        switch (view.getId()) {
            case R.id.grocery:
                if (checked)
                    checkedCategories.add(Category.GROCERY);
                else
                    checkedCategories.remove(Category.GROCERY);
                break;
            case R.id.convenience:
                if (checked)
                    checkedCategories.add(Category.CONVENIENCE);
                else
                    checkedCategories.remove(Category.CONVENIENCE);
                break;
            case R.id.pharmacy:
                if (checked)
                    checkedCategories.add(Category.PHARMACY);
                else
                    checkedCategories.remove(Category.PHARMACY);
                break;
            case R.id.hardware:
                if (checked)
                    checkedCategories.add(Category.HARDWARE);
                else
                    checkedCategories.remove(Category.HARDWARE);
                break;
            case R.id.petCare:
                if (checked)
                    checkedCategories.add(Category.PET);
                else
                    checkedCategories.remove(Category.PET);
                break;
            case R.id.postOffice:
//                if (checked)
//                    checkedCategories.add(Category.);
//                else
//                    checkedCategories.remove(Category.);
                break;
        } //switch

    } //onCheckboxClicked
}


