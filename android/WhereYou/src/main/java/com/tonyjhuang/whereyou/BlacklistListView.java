package com.tonyjhuang.whereyou;

import android.content.Context;
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
import com.daimajia.androidanimations.library.YoYo;
import com.nineoldandroids.animation.ObjectAnimator;
import com.tonyjhuang.whereyou.helpers.BackAwareEditText;
import com.tonyjhuang.whereyou.helpers.ColorPicker;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.InjectViews;

/**
 * Created by tony on 5/3/15.
 */
public class BlacklistListView extends ListView {

    private WeakReference<BlacklistActivity> blacklistActivityWeakReference;
    private BlacklistListAdapter adapter = new BlacklistListAdapter();
    private AddToBlacklistHeaderView header;

    public BlacklistListView(Context context) {
        this(context, null);
    }

    public BlacklistListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BlacklistListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        try {
            blacklistActivityWeakReference = new WeakReference<>((BlacklistActivity) context);
        } catch (ClassCastException e) {
            throw new ClassCastException("Containing activity MUST be BlacklistActivity!");
        }

        header = new AddToBlacklistHeaderView(context);
        addHeaderView(header);
        setAdapter(adapter);
    }

    public void setBlacklist(JSONArray jsonBlacklist) {
        ArrayList<String> blacklist = new ArrayList<>();
        if (jsonBlacklist != null) {
            try {
                for (int i = 0; i < jsonBlacklist.length(); i++) {
                    blacklist.add(jsonBlacklist.getString(i));
                }
            } catch (JSONException e) {
                Log.e("BlacklistListView", e.getMessage());
            }
        }
        Collections.reverse(blacklist);
        adapter.update(blacklist);
    }

    public boolean onBackPressed() {
        if (header.isShowingEditor()) {
            header.showEditor(false);
            return true;
        } else {
            return false;
        }
    }

    public class BlacklistListAdapter extends BaseAdapter {

        private ArrayList<String> friends;

        public void update(ArrayList<String> friends) {
            this.friends = friends;
            notifyDataSetChanged();
            header.showEditor(false);
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
            final BlacklistRowView holder;
            if (view == null) {
                view = View.inflate(viewGroup.getContext(), R.layout.view_friends_row, null);
                holder = new BlacklistRowView(view);
                view.setTag(holder);
            } else {
                holder = (BlacklistRowView) view.getTag();
            }

            String blacklisted = getItem(i);
            holder.bind(blacklisted, i);

            return view;
        }

        public class BlacklistRowView {
            @InjectView(R.id.container)
            RelativeLayout container;
            @InjectView(R.id.name)
            TextView name;
            @InjectViews({R.id.delete})
            List<View> editViews;

            private List<YoYo.YoYoString> strings = new ArrayList<>();

            final ButterKnife.Setter<View, Boolean> VISIBLE = new ButterKnife.Setter<View, Boolean>() {
                @Override
                public void set(View view, Boolean visible, int index) {
                    view.setVisibility(visible ? View.VISIBLE : GONE);
                }
            };

            final ButterKnife.Setter<View, Integer> FLOAT = new ButterKnife.Setter<View, Integer>() {
                @Override
                public void set(View view, Integer offset, int index) {
                    strings.add(index, YoYo.with(new InfBounceAnimator())
                            .duration(1000)
                            .playOn(view));
                }
            };

            public BlacklistRowView(View view) {
                ButterKnife.inject(this, view);
            }

            public void bind(final String friend, int index) {
                int bgColor = getResources().getColor(R.color.darker_grey);
                bgColor += (0x00050505 * index);

                name.setText(friend);
                container.setBackgroundColor(bgColor);

                // For editmode animations
                for (YoYo.YoYoString string : strings) {
                    string.stop(true);
                }
                strings = new ArrayList<>();

                // Setup edit buttons
                ButterKnife.apply(editViews, VISIBLE, true);
                ButterKnife.apply(editViews, FLOAT, index);
                // Delete button
                editViews.get(0).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        blacklistActivityWeakReference.get().removeFromBlacklist(friend);
                    }
                });
            }
        }
    }

    public class AddToBlacklistHeaderView extends FrameLayout {
        @InjectView(R.id.friend_input)
        BackAwareEditText blacklistInput;
        @InjectView(R.id.add_container)
        LinearLayout addContainer;

        private boolean isShowingEditor = false;

        public AddToBlacklistHeaderView(Context context) {
            this(context, null);
        }

        public AddToBlacklistHeaderView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public AddToBlacklistHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            inflate(context, R.layout.view_friends_add_footer, this);
            ButterKnife.inject(this, this);
            addContainer.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    showEditor(true);
                }
            });

            blacklistInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE ||
                            (event != null && event.getAction() == KeyEvent.ACTION_DOWN &&
                                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                        String friend = blacklistInput.getText().toString();

                        if (blacklistActivityWeakReference.get() != null) {
                            blacklistActivityWeakReference.get().addToBlacklist(friend);
                        }

                        return true;
                    }
                    return false;
                }
            });

            blacklistInput.setOnEditTextImeBackListener(new BackAwareEditText.OnImeBackListener() {
                @Override
                public void onImeBack() {
                    showEditor(false);
                }
            });

            blacklistInput.addTextChangedListener(new SignupActivity.SimpleTextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {
                    String text = editable.toString();
                    String lowerCase = text.toLowerCase();
                    if (!text.equals(lowerCase)) {
                        blacklistInput.setText(lowerCase);
                    }
                    blacklistInput.setSelection(text.length());
                }
            });
        }

        public void showEditor(boolean show) {
            isShowingEditor = show;
            if (show) {
                addContainer.setVisibility(INVISIBLE);
                blacklistInput.setVisibility(VISIBLE);
                blacklistInput.requestFocus();
            } else {
                addContainer.setVisibility(VISIBLE);
                blacklistInput.setVisibility(INVISIBLE);
                blacklistInput.setText("");
            }
            showKeyboard(show);
        }

        private void showKeyboard(boolean show) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (show) {
                imm.showSoftInput(blacklistInput, InputMethodManager.SHOW_IMPLICIT);
            } else {
                imm.hideSoftInputFromWindow(blacklistInput.getWindowToken(), 0);
            }
        }

        public boolean isShowingEditor() {
            return isShowingEditor;
        }
    }

    public static class InfBounceAnimator extends BaseViewAnimator {
        @Override
        public void prepare(View target) {
            ObjectAnimator y = ObjectAnimator.ofFloat(target, "translationY", 10, -10);
            y.setRepeatCount(ObjectAnimator.INFINITE);
            y.setRepeatMode(ObjectAnimator.REVERSE);
            getAnimatorAgent().playTogether(y);
        }
    }
}
