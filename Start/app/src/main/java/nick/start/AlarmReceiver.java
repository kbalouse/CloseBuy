package nick.start;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by nick on 14/10/15.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private int counter = 0;
    private static final String tag = "MyTag";

    public void onReceive(Context arg0, Intent arg1) {
        Log.d(tag, "onReceive with counter=" + counter);
        Toast.makeText(arg0, "Running " + counter, Toast.LENGTH_SHORT).show();
    }
}
