package com.tonyjhuang.whereyou;

import android.app.Activity;
import android.widget.Toast;

import butterknife.ButterKnife;

/**
 * Created by tony on 4/29/15.
 */
public class WhereYouActivity extends Activity {

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.inject(this);
    }

    private Toast currentToast;
    public void showToast(String message) {
        if(currentToast != null) currentToast.cancel();
        currentToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        currentToast.show();
    }
}
