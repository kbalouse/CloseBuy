package fourpointoh.closebuy;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.view.View;
import java.util.ArrayList;
import android.widget.CheckBox;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.content.DialogInterface.OnClickListener;
import android.app.ActionBar;
import android.view.Window;
import android.view.MenuItem;
import android.graphics.drawable.ColorDrawable;

public class AddTextActivity extends AppCompatActivity {
    private Switch mySwitch;
    private EditText editText;
    private Button doneButton;
    private DbHandle dbHandle;
    private ArrayList<Category> checkedCategories;
    private boolean inStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_text);
        Log.d(getString(R.string.log_tag), "The AddTextActivity onCreate() event");
        mySwitch = (Switch) findViewById(R.id.mySwitch);
        editText = (EditText) findViewById(R.id.edit_message);
        doneButton = (Button) findViewById(R.id.btnDone);
        checkedCategories = new ArrayList<Category>();
        dbHandle = ReminderItemDbHelper.getInstance(getApplicationContext());

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) Log.d(getString(R.string.log_tag), "ActionBar is null");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        //set the switch to OFF by default
        //remind inStore OFF by default
        mySwitch.setChecked(false);
        inStore = false;

        //attach a listener to check for changes in state
        mySwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if(isChecked) {
                    inStore = true;
                } // if
                else{
                    inStore = false;
                } // else

            } // onCheckedChanged
        }); // setOnCheckedChangeListener




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

                // Add the item to the db
                dbHandle.addItem(name, inStore, checkedCategories);

                // Return back to the home screen
                finish();
            }
        });

    } // onCreate

    // back button override
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Log.d(getString(R.string.log_tag), "Back button clicked");
                String name = editText.getText().toString();
                if (name.matches("") && checkedCategories.isEmpty()) finish();
                else {
                    new AlertDialog.Builder(this)
                            .setTitle("Discard Reminder?")
                            .setNegativeButton(android.R.string.no, null)
                            .setPositiveButton(android.R.string.yes, new OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                                    finish();
                                }
                            }).create().show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();
        Log.d(getString(R.string.log_tag), "checkbox " + view.getId() + " clicked!");
        Log.d(getString(R.string.log_tag), "R.id.grocery =  " + R.id.grocery);
        // Check which checkbox was clicked
        switch(view.getId()) {
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
        }
    }
} // addTextActivity

