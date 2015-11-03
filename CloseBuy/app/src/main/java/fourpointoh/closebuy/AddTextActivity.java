package fourpointoh.closebuy;

import android.content.Intent;
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

public class AddTextActivity extends AppCompatActivity {
    String msg = "Android : ";
    private TextView switchStatus;
    private Switch mySwitch;
    private EditText editText;
    private Button doneButton;
    private DbHandle dbHandle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_text);
        Log.d(msg, "The AddTextActivity onCreate() event");
        switchStatus = (TextView) findViewById(R.id.switchStatus);
        mySwitch = (Switch) findViewById(R.id.mySwitch);
        editText = (EditText) findViewById(R.id.edit_message);
        doneButton = (Button) findViewById(R.id.btnDone);

        //set the switch to OFF initially
        mySwitch.setChecked(false);
        //attach a listener to check for changes in state
        mySwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                Log.d(getString(R.string.log_tag), "Switch button clicked");
                Toast.makeText(
                        getApplicationContext(),
                        getString(R.string.feature_not_ready),
                        Toast.LENGTH_LONG
                ).show();


                if(isChecked){
                    switchStatus.setText("Switch is currently ON");
                } // if
                else{
                    switchStatus.setText("Switch is currently OFF");
                } // else

            } // onCheckedChanged
        }); // setOnCheckedChangeListener

        //check the current state before we display the screen
        if(mySwitch.isChecked()){
            switchStatus.setText("Switch is currently ON");
        }
        else {
            switchStatus.setText("Switch is currently OFF");
        }

        doneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send newItem to Home Activity on click
                Log.d(getString(R.string.log_tag), "Done button clicked");
                Intent intent = new Intent(AddTextActivity.this, HomeActivity.class);
                intent.putExtra("editText",(String)editText.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    } // onCreate
} // addTextActivity

