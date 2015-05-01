package com.tonyjhuang.whereyou;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tonyjhuang.whereyou.api.ParseHelper;

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
            RowViewHolder holder;
            if (view == null) {
                view = View.inflate(viewGroup.getContext(), R.layout.view_friends_row, null);
                holder = new RowViewHolder(view);
                view.setTag(holder);
            } else {
                holder = (RowViewHolder) view.getTag();
            }

            final String friend = getItem(i);
            holder.name.setText(friend);
            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    parseHelper.poke(friend);
                    footer.showEditor(false);
                }
            });

            return view;
        }

        public class RowViewHolder {
            @InjectView(R.id.container)
            LinearLayout container;
            @InjectView(R.id.name)
            TextView name;

            public RowViewHolder(View view) {
                ButterKnife.inject(this, view);
            }
        }
    }

    public class AddFriendFooterView extends FrameLayout {
        @InjectView(R.id.friend_input)
        EditText friendInput;
        @InjectView(R.id.add_container)
        LinearLayout addContainer;

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
                            mainActivityWeakReference.get().addFriend(friend);
                        }

                        return true;
                    }
                    return false;
                }
            });
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
}
