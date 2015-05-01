package com.tonyjhuang.whereyou.helpers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.KeyEvent;

/**
 * Created by tony on 5/1/15.
 */
public class BackAwareEditText extends TypefaceEditText {

    private OnImeBackListener onOnImeBackListener;

    public BackAwareEditText(Context context) {
        super(context);
    }

    public BackAwareEditText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, @NonNull KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (onOnImeBackListener != null) onOnImeBackListener.onImeBack();
        }
        return super.dispatchKeyEvent(event);
    }

    public void setOnEditTextImeBackListener(OnImeBackListener listener) {
        onOnImeBackListener = listener;
    }

    public interface OnImeBackListener {
        void onImeBack();
    }
}