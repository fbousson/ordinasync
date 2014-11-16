package be.ordina.firebase.ordinasync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;

import java.util.ArrayList;
import java.util.List;

import be.ordina.firebase.ordinasync.application.OrdinaSync;

/**
 * Created by fbousson on 16/11/14.
 */
public class StoreOverview extends ActionBarActivity {


    private static String TAG = StoreOverview.class.getSimpleName();

    public static final String STORE_OVERVIEW_STORE_REF_NAME = "OrdinaSync_storeOverviewRefName";

    private View _noFirebase;
    private View _firebaseActive;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Firebase firebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_overview);
        String firebaseStoreName =getFireBaseStoreName(getIntent().getExtras());

        _noFirebase = findViewById(R.id.activity_store_overview_no_firebase);
        _firebaseActive = findViewById(R.id.activity_store_overview_firebase_active);

        Button toggleAirPlaneModeButton = (Button) findViewById(R.id.activity_store_overview_toggle_airplane_mode);
        toggleAirPlaneModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAirplaneModeSettings();

            }
        });

        Button removeAllButton = (Button) findViewById(R.id.activity_store_overview_remote_all_button);
        removeAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "removing all items");
            }
        });


        initRecyclerView();

        boolean fireBaseActive = firebaseStoreName != null;
        fireBaseActiveView(fireBaseActive);

        if(fireBaseActive){
            getSupportActionBar().setTitle(firebaseStoreName);
            firebase = OrdinaSync.getInstance().getFireBaseStore().getChileFireBaseRef(firebaseStoreName);
        }
    }

    private void initRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.activity_store_overview_items_recyclerview);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        List<String> list = new ArrayList<String>();
        for(int i = 0; i <= 100; i++){
            list.add("" + i);
        }

        mAdapter = new MyAdapter(list);
        mRecyclerView.setAdapter(mAdapter);
    }


    static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private List mDataset;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public static class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView mTextView;
            public ViewHolder(View v) {
                super(v);
                mTextView = (TextView) v.findViewById(R.id.recyclerview_firebaseitem_text);
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public MyAdapter(List myDataset) {
            mDataset = myDataset;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_firebaseitem, parent, false);
            // set the view's size, margins, paddings and layout parameters
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            String text = mDataset.get(position).toString();
            holder.mTextView.setText(text);

        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }

    private void goToAirplaneModeSettings() {
        startActivityForResult(new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS), 0);
    }

    private void fireBaseActiveView(boolean active) {
        _noFirebase.setVisibility(active? View.GONE : View.VISIBLE);
        _firebaseActive.setVisibility(active ? View.VISIBLE : View.GONE);
    }

    private void registerAirPlaneModeBroadcastReceiver(){
        IntentFilter intentFilter = new IntentFilter("android.intent.action.AIRPLANE_MODE");

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Service state changed");
            }
        };

        this.registerReceiver(receiver, intentFilter);
    }

    private String getFireBaseStoreName(Bundle bundle){
        if(bundle == null ){
            return null;
        }
         return bundle.getString(STORE_OVERVIEW_STORE_REF_NAME);
    }


}
