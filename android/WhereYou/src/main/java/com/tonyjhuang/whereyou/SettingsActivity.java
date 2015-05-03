package com.tonyjhuang.whereyou;

import android.os.Bundle;

import android.view.View;

import com.tonyjhuang.whereyou.helpers.AppRouter;
import com.tonyjhuang.whereyou.helpers.WhereYouActivity;

import butterknife.OnClick;

/**
 * Created by tony on 5/3/15.
 */
public class SettingsActivity extends WhereYouActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    @OnClick(R.id.back_container)
    public void onBackContainerClicked(View view) {
        onBackPressed();
    }

    @OnClick(R.id.about)
    public void onAboutClicked(View view) {
        AppRouter.openAboutActivity(this);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
}
