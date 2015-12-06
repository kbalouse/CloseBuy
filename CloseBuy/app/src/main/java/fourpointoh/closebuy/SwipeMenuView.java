package fourpointoh.closebuy;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 
 * @author baoyz
 * @date 2014-8-23
 * 
 */
public class SwipeMenuView extends LinearLayout implements OnClickListener {

	private SwipeMenuListView mListView;
	private SwipeMenuLayout mLayout;
	private SwipeMenu mMenu;
	private OnSwipeItemClickListener onItemClickListener;
	private int position;

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public SwipeMenuView(SwipeMenu menu, SwipeMenuListView listView) {
		super(menu.getContext());
		mListView = listView;
		mMenu = menu;
		List<SwipeMenuItem> items = menu.getMenuItems();
		int id = 0;
		for (SwipeMenuItem item : items) {
			addItem(item, id++);
		}
	}

	private void addItem(SwipeMenuItem item, int id) {

        LayoutParams params = new LayoutParams(item.getWidth(),
                LayoutParams.MATCH_PARENT);

        if (item.hasCustomLayout()) {
            LinearLayout parent = createCustomMenuItemLayout(item);
            parent.setId(id);
            parent.setLayoutParams(params);
            parent.setOnClickListener(this);
            addView(parent);
        } else {
            LinearLayout parent = new LinearLayout(getContext());
            parent.setId(id);
            parent.setLayoutParams(params);
            parent.setOnClickListener(this);
            parent.setGravity(Gravity.CENTER);
            parent.setOrientation(LinearLayout.VERTICAL);
            parent.setBackgroundColor(Color.BLUE); // DEBUG
            parent.setBackgroundDrawable(item.getBackground());
            addView(parent);

            if (item.getIcon() != null) {
                parent.addView(createIcon(item));
            }
            if (!TextUtils.isEmpty(item.getTitle())) {
                View v = createTitle(item);
                parent.addView(v);
            }
        }
    }

    private LinearLayout createCustomMenuItemLayout(SwipeMenuItem item) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout menuLayout = (LinearLayout) inflater.inflate(item.getLayoutId(), null);
        return menuLayout;
    }

	private ImageView createIcon(SwipeMenuItem item) {
		ImageView iv = new ImageView(getContext());
		iv.setImageDrawable(item.getIcon());
		return iv;
	}

	private TextView createTitle(SwipeMenuItem item) {
		TextView tv = new TextView(getContext());
		tv.setText(item.getTitle());
		tv.setGravity(Gravity.CENTER);
		tv.setTextSize(item.getTitleSize());
		tv.setTextColor(item.getTitleColor());
		return tv;
	}

	@Override
	public void onClick(View v) {
		if (onItemClickListener != null && mLayout.isOpen()) {
			onItemClickListener.onItemClick(this, mMenu, v.getId());
		}
	}

	public OnSwipeItemClickListener getOnSwipeItemClickListener() {
		return onItemClickListener;
	}

	public void setOnSwipeItemClickListener(OnSwipeItemClickListener onItemClickListener) {
		this.onItemClickListener = onItemClickListener;
	}

	public void setLayout(SwipeMenuLayout mLayout) {
		this.mLayout = mLayout;
	}

	public static interface OnSwipeItemClickListener {
		void onItemClick(SwipeMenuView view, SwipeMenu menu, int index);
	}
}
