package fourpointoh.closebuy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.content.Intent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
    private View doneButton;
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
        doneButton = (View) findViewById(R.id.btnDone);
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
        actionBar.setElevation(0);

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

    // Close keyboard when user touches anywhere but on the keyboard
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        View v = getCurrentFocus();
        boolean ret = super.dispatchTouchEvent(event);

        if (v instanceof EditText) {
            View w = getCurrentFocus();
            int scrcoords[] = new int[2];
            w.getLocationOnScreen(scrcoords);
            float x = event.getRawX() + w.getLeft() - scrcoords[0];
            float y = event.getRawY() + w.getTop() - scrcoords[1];

            if (event.getAction() == MotionEvent.ACTION_UP && (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w.getBottom()) ) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
            }
        }
        return ret;
    }

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

            switch (c) {
                case GROCERY:
                    checkBox = (CheckBox)findViewById(R.id.grocery);
                    checkBox.setChecked(!checkBox.isChecked());
                    break;
                case CONVENIENCE:
                    checkBox = (CheckBox)findViewById(R.id.convenience);
                    checkBox.setChecked(!checkBox.isChecked());
                    break;
                case PHARMACY:
                    checkBox = (CheckBox)findViewById(R.id.pharmacy);
                    checkBox.setChecked(!checkBox.isChecked());
                    break;
                case HARDWARE:
                    checkBox = (CheckBox)findViewById(R.id.hardware);
                    checkBox.setChecked(!checkBox.isChecked());
                    break;
                case PET:
                    checkBox = (CheckBox)findViewById(R.id.petCare);
                    checkBox.setChecked(!checkBox.isChecked());
                    break;
                case ELECTRONICS:
                    checkBox = (CheckBox)findViewById(R.id.electronics);
                    checkBox.setChecked(!checkBox.isChecked());
                    break;
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
            case R.id.electronics:
                if (checked)
                    checkedCategories.add(Category.ELECTRONICS);
                else
                    checkedCategories.remove(Category.ELECTRONICS);
                break;
        } //switch

    } //onCheckboxClicked
}


