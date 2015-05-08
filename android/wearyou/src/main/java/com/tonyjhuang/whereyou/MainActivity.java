package com.tonyjhuang.whereyou;

import android.app.Activity;
import android.os.Bundle;
import android.os.Vibrator;
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
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "Main";

    private ArrayList<String> friends = new ArrayList<>();
    private GoogleApiClient googleApiClient;
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ColorPicker.init(this);

        googleApiClient = GoogleApiClientBuilder.build(this, this, this);

        WearableListView listView = (WearableListView) findViewById(R.id.listview);
        adapter = new Adapter();
        listView.setAdapter(adapter);
        listView.setClickListener(new WearableListView.ClickListener() {
            @Override
            public void onClick(WearableListView.ViewHolder viewHolder) {
                FriendRowViewHolder holder = (FriendRowViewHolder) viewHolder;
                String friend = holder.nameView.getText().toString();
                sendAskMessage(friend);
            }

            @Override
            public void onTopEmptyRegionClick() {
                // Don't need this
            }
        });
    }

    private void sendAskMessage(final String friend) {
        Log.d(TAG, "sendAskMessage: " + friend);
        if (googleApiClient.isConnected()) {
            Wearable.NodeApi.getConnectedNodes(googleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                @Override
                public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                    List<Node> nodes = getConnectedNodesResult.getNodes();
                    if (nodes.size() == 1) {
                        sendAskMessage(friend, nodes.get(0).getId());
                    } else {
                        showToast(getString(R.string.error_multiple_connections));
                    }
                }
            });
        } else {
            showToast(getString(R.string.error_connect));
        }
    }

    private void sendAskMessage(String friend, String nodeId) {
        Log.d(TAG, "sendAskMessage: " + friend + ", " + nodeId);
        try {
            Wearable.MessageApi.sendMessage(googleApiClient, nodeId, Constants.WEAR_MSG_PATH_ASK, friend.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "couldn't endcode string as UTF-8");
            Wearable.MessageApi.sendMessage(googleApiClient, nodeId, Constants.WEAR_MSG_PATH_ASK, friend.getBytes());
        }

        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(50);
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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
        // this definitely isn't the best way http://stackoverflow.com/a/24601307/1476372
        PendingResult<DataItemBuffer> results = Wearable.DataApi.getDataItems(googleApiClient);
        results.setResultCallback(new ResultCallback<DataItemBuffer>() {
            @Override
            public void onResult(DataItemBuffer dataItems) {
                if (dataItems.getCount() != 0) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItems.get(0));

                    // This should read the correct value.
                    friends = dataMapItem.getDataMap().getStringArrayList(Constants.WEAR_DATA_KEY_FRIENDS);
                    adapter.notifyDataSetChanged();
                }

                dataItems.release();
            }
        });
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended: " + cause);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed: " + connectionResult);
        showToast(getString(R.string.error_connect));
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
