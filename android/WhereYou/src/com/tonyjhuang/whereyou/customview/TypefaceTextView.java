package com.tonyjhuang.whereyou.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import com.tonyjhuang.whereyou.R;
import com.tonyjhuang.whereyou.TypefaceManager;

/**
 * Created by Tony on 3/23/14.
 * Inspired by https://github.com/johnkil/Android-RobotoTextView
 * Textview that any font in the assets folder (right now used just with Lato).
 */
public class TypefaceTextView extends TextView {

    private String base;
    private Context context;

    public TypefaceTextView(Context context) {
        super(context);
        this.context = context;
        setTypeface(base);
    }

    public TypefaceTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.context = context;
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attributeSet,
                R.styleable.TypefaceTextView,
                0, 0);
        try {
            base = a.getString(R.styleable.TypefaceTextView_typeface) + ".ttf";
        } finally {
            a.recycle();
        }

        setTypeface(base);
    }

    public void setTypeface(String typeface) {
        setTypeface(TypefaceManager.get(context, typeface));
    }

}
