package fourpointoh.closebuy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NearbyStoreUpdate extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // It was suggested that we check the action string to make sure it is correct intent?
//        if (!intent.getAction().equals(context.getString(R.string.update_action)))

        Log.d(context.getString(R.string.log_tag), "NearbyStoreUpdate onReceive()");
    }
}
