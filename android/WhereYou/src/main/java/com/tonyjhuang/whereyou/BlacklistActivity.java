package com.tonyjhuang.whereyou;

import android.os.Bundle;
import android.widget.ListView;

import com.parse.ParseInstallation;
import com.tonyjhuang.whereyou.api.ParseHelper;
import com.tonyjhuang.whereyou.helpers.WhereYouActivity;

import org.json.JSONArray;

import butterknife.InjectView;

/**
 * Created by tony on 5/3/15.
 */
public class BlacklistActivity extends WhereYouActivity {

    @InjectView(R.id.blacklist)
    BlacklistListView blacklistListView;

    private ParseHelper parseHelper = new ParseHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blacklist);

        ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();
        JSONArray blacklist = currentInstallation.getJSONArray("blacklist");
        blacklistListView.setBlacklist(blacklist);
    }

    public void addToBlacklist(String name) {
        blacklistListView.setBlacklist(parseHelper.addToBlacklist(name));
    }

    public void removeFromBlacklist(String name) {
        blacklistListView.setBlacklist(parseHelper.removeFromBlacklist(name));
    }

    @Override
    public void onBackPressed() {
        if(!blacklistListView.onBackPressed())
            super.onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
}
