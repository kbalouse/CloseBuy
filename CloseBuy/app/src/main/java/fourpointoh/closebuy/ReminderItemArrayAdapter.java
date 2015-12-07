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
            v = inflater.inflate(R.layout.list_item, parent, false);
        } else {
            // Remove existing category bubbles in this recycled view
            ViewGroup categoryContainer = (ViewGroup) v.findViewById(R.id.category_container);
            categoryContainer.removeAllViews();
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

        // Add in category bubbles
        ViewGroup categoryContainer = (ViewGroup) v.findViewById(R.id.category_container);
        for (int i = 0; i < item.categories.size(); i++) {
            // Only allow 2 category bubbles to be shown, to avoid showing a cut off bubble.
            if (i == 2) {
                break;
            }

            Category c = item.categories.get(i);
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View bubble = inflater.inflate(R.layout.category_bubble, categoryContainer, false);
            TextView tv = (TextView) bubble.findViewById(R.id.category_text);
            tv.setText(c.toReadableString());

            // Set bubble background color
            switch (c) {
                case HARDWARE:
                    tv.setBackground(context.getResources().getDrawable(R.drawable.hardware_bubble_box));
                    break;
                case GROCERY:
                    tv.setBackground(context.getResources().getDrawable(R.drawable.grocery_bubble_box));
                    break;
                case PHARMACY:
                    tv.setBackground(context.getResources().getDrawable(R.drawable.pharmacy_bubble_box));
                    break;
                case ELECTRONICS:
                    tv.setBackground(context.getResources().getDrawable(R.drawable.electronics_bubble_box));
                    break;
                case CONVENIENCE:
                    tv.setBackground(context.getResources().getDrawable(R.drawable.convenience_bubble_box));
                    break;
                case PET:
                    tv.setBackground(context.getResources().getDrawable(R.drawable.petstore_bubble_box));
                    break;
            }

            categoryContainer.addView(bubble);
        }

        return v;
    }
}
