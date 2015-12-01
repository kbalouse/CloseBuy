package fourpointoh.closebuy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import java.util.ArrayList;
import android.widget.CheckBox;


/**
 * Created by josh on 29/11/2015.
 */
public class EditTextActivity extends AppCompatActivity {
    private DbHandle dbHandle;
    private ArrayList<ReminderItem> itemList;
    private ReminderItem item;
    //private Switch mySwitch;
    private EditText editText;
    private Button doneButton;
    private ArrayList<Category> checkedCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("log_tag", "The EditTextActivity onCreate() event");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_text);
        //mySwitch = (Switch) findViewById(R.id.mySwitch);
        editText = (EditText) findViewById(R.id.edit_message);
        doneButton = (Button) findViewById(R.id.btnDone);
        checkedCategories = new ArrayList<Category>();
        // Get a db handle
        dbHandle = ReminderItemDbHelper.getInstance(getApplicationContext());
        Intent intent = getIntent();
        // get data via the key = "position_id"
        int item_id = intent.getIntExtra("position_id", 0);
        Log.d("log_tag", "THE ITEM ID IS " + item_id);
        // pull reminder item from DB based on listview position id
        itemList = dbHandle.getAllItems(); // write function for getItemByID

        // update view with item pulled from DB
        updateView(itemList.get(item_id));

        // change it to function call later instead of definition
        // unlike AddTextActivity, don't need to add to DB
        doneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(getString(R.string.log_tag), "Done button clicked");

                // Enforce that the necessary fields are non empty
                // TODO
                String name = editText.getText().toString();

                // update item in DB, boolean "update" = true
                dbHandle.editItem(name, checkedCategories);

                // Return back to the home screen
                Intent intent = new Intent(EditTextActivity.this, HomeActivity.class);
                startActivity(intent);

            }
        });

    } // onCreate

    public void updateView(ReminderItem item) {
        updateEditText(item.itemName);
        updateSpecificStore();
        updateCategories(item.categories);
    }

    public void updateEditText(String itemName) {
        EditText edit_message = (EditText) findViewById(R.id.edit_message);
        edit_message.setText(itemName);
    }

    public void updateSpecificStore() {
        // fill in if we decide to implement in future
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


