package fourpointoh.closebuy;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by nick on 24/10/15.
 */
public class ReminderItemArrayAdapter extends ArrayAdapter<ReminderItem> {
    private ArrayList<ReminderItem> items;

    public ReminderItemArrayAdapter(Context context, int textViewIdResource, ArrayList<ReminderItem> inputItemList) {
        super(context, textViewIdResource, inputItemList);
        items = inputItemList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        Context appContext = parent.getContext();

        if (appContext == null) {
            // Log.d("CloseBuyLogTag", "appContext is null, getting view for index " + position);
        } else {
            // Log.d(appContext.getString(R.string.log_tag), "getting view for index " + position + " item " + items.get(position).itemName);
        }

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.list_item, null);
        }

        // Grab the corresponding item
        ReminderItem item = items.get(position);

        // Give content to the view according to the given item
        TextView textView = (TextView) v.findViewById(R.id.item_name);
        textView.setText(item.itemName);
        if (!item.enabled) textView.setTextColor(Color.GRAY);
        else textView.setTextColor(Color.BLACK);

        return v;
    }
}
