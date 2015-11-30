package fourpointoh.closebuy;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;

/**
 * Simple TextView that allows custom fonts to be declared at XML level
 */
public class CustomFontTextView extends TextView {

    private static HashMap<String, Typeface> singletonTypeFaces = null;

    public CustomFontTextView(Context context) {
        super(context);
        init(context, null);
    }

    public CustomFontTextView(Context context, AttributeSet attrs) {
        super(context);
        init(context, attrs);
    }

    public CustomFontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context);
        init(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    private void init(Context context, AttributeSet attrs) {
        if (singletonTypeFaces == null) {
            singletonTypeFaces = new HashMap<String, Typeface>();
        }

        if (attrs != null) {
            TypedArray a=getContext().obtainStyledAttributes(attrs, R.styleable.CustomFontTextView);

            // Set android attributes
            if (a.hasValue(R.styleable.CustomFontTextView_android_id)) {
                this.setId(a.getResourceId(R.styleable.CustomFontTextView_android_id, 0));
            }
            this.setText(a.getString(R.styleable.CustomFontTextView_android_text));
            this.setTextSize(a.getDimension(R.styleable.CustomFontTextView_android_textSize, 12));
            this.setTextColor(a.getColor(R.styleable.CustomFontTextView_android_textColor, context.getResources().getColor(R.color.colorListItemText)));
            this.setGravity(a.getInt(R.styleable.CustomFontTextView_android_gravity, Gravity.START));

            // set the typeface
            String path = "fonts/" + a.getString(R.styleable.CustomFontTextView_fontName) + ".ttf";
            if (singletonTypeFaces.get(path) == null) {
                Typeface tf = Typeface.createFromAsset(context.getAssets(), path);
                singletonTypeFaces.put(path, tf);
            }
            this.setTypeface(singletonTypeFaces.get(path));

            //Don't forget this
            a.recycle();
        }
    }
}
