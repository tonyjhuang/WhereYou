package com.tonyjhuang.whereyou;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;

public class MainActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private ArrayList<String> friends = new ArrayList<>();
    private GoogleApiClient googleApiClient;
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ColorPicker.init(this);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        friends.add("gasper");
        friends.add("kevin");
        friends.add("tyrone");
        friends.add("tony");
        friends.add("ronny");
        friends.add("david");
        friends.add("allison");

        WearableListView listView = (WearableListView) findViewById(R.id.listview);
        adapter = new Adapter();
        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        googleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        PendingResult<DataItemBuffer> results = Wearable.DataApi.getDataItems(googleApiClient);
        results.setResultCallback(new ResultCallback<DataItemBuffer>() {
            @Override
            public void onResult(DataItemBuffer dataItems) {
                if (dataItems.getCount() != 0) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItems.get(0));

                    // This should read the correct value.
                    friends = dataMapItem.getDataMap().getStringArrayList("friends");
                    Log.d("Main", friends.toString());
                    Toast.makeText(MainActivity.this, "test" + dataMapItem.getDataMap().getInt("TEST"), Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                }

                dataItems.release();
            }
        });
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d("Main", "onConnectionSuspended: " + cause);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("Main", "onConnectionFailed: " + connectionResult);
        Toast.makeText(this, "Couldn't connect to phone :(", Toast.LENGTH_SHORT).show();
        finish();
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
