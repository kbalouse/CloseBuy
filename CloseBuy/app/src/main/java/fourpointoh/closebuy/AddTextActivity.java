package fourpointoh.closebuy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
public class AddTextActivity extends AppCompatActivity {
    String msg = "Android : ";
    private TextView switchStatus;
    private Switch mySwitch;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_text);
        Log.d(msg, "The AddTextActivity onCreate() event");
        switchStatus = (TextView) findViewById(R.id.switchStatus);
        mySwitch = (Switch) findViewById(R.id.mySwitch);
        editText = (EditText) findViewById(R.id.edit_message);
        /*
        this.post(new Runnable() {
            public void run() {
                editText.requestFocus();
            }
        });
        */
        //set the switch to ON
        mySwitch.setChecked(false);
        //attach a listener to check for changes in state
        mySwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

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
        } // if
        else {
            switchStatus.setText("Switch is currently OFF");
        }//else
        //editText.requestFocus();
        //InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

    } // onCreate

} // addTextActivity

