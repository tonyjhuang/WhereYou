package com.tonyjhuang.whereyou;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WearableListView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private ArrayList<String> friends = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ColorPicker.init(this);

        friends.add("gasper");
        friends.add("kevin");
        friends.add("tyrone");
        friends.add("tony");
        friends.add("ronny");
        friends.add("david");
        friends.add("allison");

        WearableListView listView = (WearableListView) findViewById(R.id.listview);
        listView.setAdapter(new Adapter());
    }

    private class Adapter extends WearableListView.Adapter {

        @Override
        public FriendRowViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = View.inflate(viewGroup.getContext(), R.layout.view_friends_row, null);
            return new FriendRowViewHolder(view);
        }

        @Override
        public void onBindViewHolder(WearableListView.ViewHolder viewHolder, int i) {
            FriendRowViewHolder holder = (FriendRowViewHolder) viewHolder;
            String friend = friends.get(i);
            holder.nameView.setText(friend);
            holder.nameView.setBackgroundColor(ColorPicker.getColor(friend));
        }

        @Override
        public int getItemCount() {
            return friends.size();
        }

    }

    public static class FriendRowViewHolder extends WearableListView.ViewHolder {
        TextView nameView;

        public FriendRowViewHolder(View itemView) {
            super(itemView);
            nameView = (TextView) itemView.findViewById(R.id.name);
        }
    }
}
