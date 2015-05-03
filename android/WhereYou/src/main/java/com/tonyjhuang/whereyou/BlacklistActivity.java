package com.tonyjhuang.whereyou;

import android.os.Bundle;

import com.tonyjhuang.whereyou.helpers.WhereYouActivity;

/**
 * Created by tony on 5/3/15.
 */
public class BlacklistActivity extends WhereYouActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blacklist);
    }
    
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
}
