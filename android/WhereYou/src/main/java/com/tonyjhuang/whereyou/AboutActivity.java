package com.tonyjhuang.whereyou;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.tonyjhuang.whereyou.helpers.WhereYouActivity;

import java.util.Random;

import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by tony on 5/3/15.
 */
public class AboutActivity extends WhereYouActivity {
    @InjectView(R.id.container)
    LinearLayout container;
    @InjectView(R.id.emoji)
    ImageView emojiView;
    @InjectView(R.id.j)
    TextView jView;

    private int currentEmojiResource;
    private TypedArray emojiResources;
    private Random random = new Random();
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        resetState();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                YoYo.with(Techniques.Pulse)
                        .duration(450)
                        .playOn(jView);
            }
        }, 3000);
    }

    private int getRandomEmoji() {
        // for some reason, emojiResources.getIndexCount() always returns 0...
        int newEmojiResource = emojiResources.getResourceId(random.nextInt(18), 0);
        if(newEmojiResource == currentEmojiResource)
            return getRandomEmoji();
        else
            return newEmojiResource;
    }

    private void resetState() {
        int[] colors = ColorPicker.getRandomColors(container.getChildCount() + 1);
        for (int i = 0; i < container.getChildCount(); i++) {
            container.getChildAt(i).setBackgroundColor(colors[i]);
        }

        container.setBackgroundColor(colors[container.getChildCount()]);

        emojiResources = getResources().obtainTypedArray(R.array.emojis);
        currentEmojiResource = getRandomEmoji();
        emojiView.setImageResource(currentEmojiResource);
    }

    @OnClick(R.id.j)
    public void onJClicked(View view) {
        handler.removeCallbacksAndMessages(null);
        Log.d("About", "hey j");
        YoYo.with(Techniques.Pulse)
                .duration(450)
                .playOn(view);
        resetState();
    }

    @Override
    public void finish() {
        emojiResources.recycle();
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
}
