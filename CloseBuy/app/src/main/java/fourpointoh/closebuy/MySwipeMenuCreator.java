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
        SwipeMenuItem undoItem = new SwipeMenuItem(context);
        undoItem.setWidth(dp2px(70));
        undoItem.setLayoutId(R.layout.undo_menu_item);
        menu.addMenuItem(undoItem);

        SwipeMenuItem deleteItem = new SwipeMenuItem(context);
        deleteItem.setWidth(dp2px(70));
        deleteItem.setLayoutId(R.layout.delete_menu_item);
        menu.addMenuItem(deleteItem);
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }
}
