package com.tonyjhuang.whereyou;

import android.content.Context;
import android.os.Handler;
import android.os.Vibrator;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.BaseViewAnimator;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.nineoldandroids.animation.ObjectAnimator;
import com.tonyjhuang.whereyou.api.ParseHelper;
import com.tonyjhuang.whereyou.helpers.BackAwareEditText;
import com.tonyjhuang.whereyou.helpers.ColorPicker;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by tony on 4/29/15.
 */
public class FriendsListView extends ListView {

    private WeakReference<MainActivity> mainActivityWeakReference;
    private FriendsListAdapter adapter = new FriendsListAdapter();
    private ParseHelper parseHelper = new ParseHelper();
    private AddFriendFooterView footer;
    private Vibrator vibrator;

    public FriendsListView(Context context) {
        this(context, null);
    }

    public FriendsListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FriendsListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        try {
            mainActivityWeakReference = new WeakReference<>((MainActivity) context);
        } catch (ClassCastException e) {
            throw new ClassCastException("Containing activity MUST be MainActivity!");
        }

        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        footer = new AddFriendFooterView(context);
        addFooterView(footer);
        setAdapter(adapter);
    }

    public void setFriends(JSONArray jsonFriends) {
        ArrayList<String> friends = new ArrayList<>();
        if (jsonFriends != null) {
            try {
                for (int i = 0; i < jsonFriends.length(); i++) {
                    friends.add(jsonFriends.getString(i));
                }
            } catch (JSONException e) {
                Log.e("FriendsListView", e.getMessage());
            }
        }
        adapter.update(friends);
    }

    public boolean onBackPressed() {
        if (footer.isShowingEditor()) {
            footer.showEditor(false);
            return true;
        }
        return false;
    }

    public void shakeAddFooter() {
        footer.shake();
    }

    public class FriendsListAdapter extends BaseAdapter {

        private ArrayList<String> friends;

        public void update(ArrayList<String> friends) {
            this.friends = friends;
            notifyDataSetChanged();
            footer.showEditor(false);
        }

        @Override
        public int getCount() {
            return friends != null ? friends.size() : 0;
        }

        @Override
        public String getItem(int i) {
            return friends != null ? friends.get(i) : null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            final RowViewHolder holder;
            if (view == null) {
                view = View.inflate(viewGroup.getContext(), R.layout.view_friends_row, null);
                holder = new RowViewHolder(view);
                view.setTag(holder);
            } else {
                holder = (RowViewHolder) view.getTag();
            }

            final String friend = getItem(i);
            int bg = ColorPicker.getColor(friend);

            holder.name.setText(friend);
            holder.container.setBackgroundColor(bg);
            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    parseHelper.poke(friend);
                    footer.showEditor(false);
                    vibrator.vibrate(25);

                    if (holder.counter >= 3) {
                        holder.name.setText(":(");

                        // reset after 3 seconds
                        holder.refreshHandler.removeCallbacksAndMessages(null);
                        holder.resetHandler.removeCallbacksAndMessages(null);
                        holder.resetHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                holder.name.setText(friend);
                                holder.counter = 0;
                            }
                        }, 3000);
                    } else {
                        holder.counter += 1;

                        // removes the counter
                        holder.refreshHandler.removeCallbacksAndMessages(null);
                        holder.refreshHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                holder.counter = 0;
                            }
                        }, 3000);
                    }

                    // Shake their name. Do it after it changes tho.
                    YoYo.with(Techniques.Swing)
                            .duration(150)
                            .playOn(holder.name);

                    YoYo.with(new FadeInOutAnimator())
                            .duration(70)
                            .playOn(holder.sent);
                }
            });

            return view;
        }

        public class RowViewHolder {
            @InjectView(R.id.container)
            RelativeLayout container;
            @InjectView(R.id.name)
            TextView name;
            @InjectView(R.id.sent)
            TextView sent;

            // Number of times the user has spammed this guy.
            int counter = 0;

            Handler resetHandler = new Handler();
            Handler refreshHandler = new Handler();

            public RowViewHolder(View view) {
                ButterKnife.inject(this, view);
            }
        }
    }

    public class AddFriendFooterView extends FrameLayout {
        @InjectView(R.id.friend_input)
        BackAwareEditText friendInput;
        @InjectView(R.id.add_container)
        LinearLayout addContainer;
        @InjectView(R.id.plus)
        TextView plus;

        private boolean isShowingEditor = false;

        public AddFriendFooterView(Context context) {
            this(context, null);
        }

        public AddFriendFooterView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public AddFriendFooterView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            inflate(context, R.layout.view_friends_add_footer, this);
            ButterKnife.inject(this, this);
            addContainer.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    showEditor(true);
                }
            });

            friendInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE ||
                            (event != null && event.getAction() == KeyEvent.ACTION_DOWN &&
                                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                        String friend = friendInput.getText().toString();

                        if (mainActivityWeakReference.get() != null) {
                            mainActivityWeakReference.get().addFriend(friend, new ParseHelper.SimpleCallback<Boolean>() {
                                @Override
                                public void onFinish(Boolean successful) {
                                    if (!successful) {
                                        YoYo.with(Techniques.Shake)
                                                .duration(150)
                                                .playOn(friendInput);
                                    }
                                }
                            });
                        }

                        return true;
                    }
                    return false;
                }
            });

            friendInput.setOnEditTextImeBackListener(new BackAwareEditText.OnImeBackListener() {
                @Override
                public void onImeBack() {
                    showEditor(false);
                }
            });

            friendInput.addTextChangedListener(new SignupActivity.SimpleTextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {
                    String text = editable.toString();
                    String lowerCase = text.toLowerCase();
                    if (!text.equals(lowerCase)) {
                        friendInput.setText(lowerCase);
                    }
                    friendInput.setSelection(text.length());
                }
            });
        }

        public void shake() {
            YoYo.with(Techniques.Swing)
                    .duration(300)
                    .playOn(plus);
        }

        public void showEditor(boolean show) {
            isShowingEditor = show;
            if (show) {
                addContainer.setVisibility(INVISIBLE);
                friendInput.setVisibility(VISIBLE);
                friendInput.requestFocus();
            } else {
                addContainer.setVisibility(VISIBLE);
                friendInput.setVisibility(INVISIBLE);
                friendInput.setText("");
            }
            showKeyboard(show);
        }

        private void showKeyboard(boolean show) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (show) {
                imm.showSoftInput(friendInput, InputMethodManager.SHOW_IMPLICIT);
            } else {
                imm.hideSoftInputFromWindow(friendInput.getWindowToken(), 0);
            }
        }

        public boolean isShowingEditor() {
            return isShowingEditor;
        }
    }

    public static class FadeInOutAnimator extends BaseViewAnimator {
        @Override
        protected void prepare(View target) {
            getAnimatorAgent().playSequentially(
                    ObjectAnimator.ofFloat(target, "alpha", 0, 1),
                    ObjectAnimator.ofFloat(target, "alpha", 1, 0)
            );
        }
    }
}
