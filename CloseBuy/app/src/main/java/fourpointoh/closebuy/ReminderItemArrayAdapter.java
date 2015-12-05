package fourpointoh.closebuy;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
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
    private Context context;

    public ReminderItemArrayAdapter(Context contextIn, int textViewIdResource, ArrayList<ReminderItem> inputItemList) {
        super(contextIn, textViewIdResource, inputItemList);
        items = inputItemList;
        context = contextIn;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.list_item, null);
        }

        // Grab the corresponding item
        ReminderItem item = items.get(position);

        // Give content to the view according to the given item
        TextView textView = (TextView) v.findViewById(R.id.item_name);
        textView.setText(item.itemName);
        if (item.enabled) {
            textView.setTextColor(context.getResources().getColor(R.color.colorListItemText));
            if ((textView.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0) {
                textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        } else {
            textView.setTextColor(context.getResources().getColor(R.color.colorDisabledListItemText));
            if ((textView.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) == 0) {
                textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
        }

        return v;
    }
}
