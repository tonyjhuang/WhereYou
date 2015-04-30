package com.tonyjhuang.whereyou;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tonyjhuang.whereyou.api.ParseHelper;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by tony on 4/29/15.
 */
public class FriendsListView extends ListView {

    private FriendsListAdapter adapter = new FriendsListAdapter();
    private ParseHelper parseHelper = new ParseHelper();

    public FriendsListView(Context context) {
        this(context, null);
    }

    public FriendsListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FriendsListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View footer = View.inflate(context, R.layout.view_friends_add_footer, null);
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

    public class FriendsListAdapter extends BaseAdapter {

        private ArrayList<String> friends;

        public void update(ArrayList<String> friends) {
            this.friends = friends;
            notifyDataSetChanged();
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
}
