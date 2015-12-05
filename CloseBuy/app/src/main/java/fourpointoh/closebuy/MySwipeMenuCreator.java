package fourpointoh.closebuy;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;

/**
 * Created by nick on 04/12/15.
 */
public class MySwipeMenuCreator implements SwipeMenuCreator {
    Context context;

    public MySwipeMenuCreator(Context c) {
        context = c;
    }

    @Override
    public void create(SwipeMenu menu) {
        // create "open" item
        SwipeMenuItem openItem = new SwipeMenuItem(context);
        // set item background
        openItem.setBackground(new ColorDrawable(context.getResources().getColor(R.color.colorUndoButton)));
        // set item width
        openItem.setWidth(dp2px(70));
        // set item title
        openItem.setTitle("Undo");
        // set item title fontsize
        openItem.setTitleSize(16);
        // set item title font color
        openItem.setTitleColor(Color.WHITE);
        // add to menu
        menu.addMenuItem(openItem);

        // create "delete" item
        SwipeMenuItem deleteItem = new SwipeMenuItem(
                context);
        // set item background
        deleteItem.setBackground(new ColorDrawable(context.getResources().getColor(R.color.colorDeleteButton)));
        // set item width
        deleteItem.setWidth(dp2px(70));
        // set a icon
        deleteItem.setIcon(R.mipmap.ic_trash);
        // add to menu
        menu.addMenuItem(deleteItem);
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }
}
