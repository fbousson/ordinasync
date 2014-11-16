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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import be.ordina.firebase.ordinasync.application.OrdinaSync;
import be.ordina.firebase.ordinasync.domain.Message;

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

    private Firebase _firebase;
    private List<Message> _list = new ArrayList<Message>();

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
                _firebase.removeValue(new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        String message = firebaseError == null ? getString(R.string.data_removed_success) : getString(R.string.data_removed_failure)+ " " + firebaseError;
                        Toast.makeText(StoreOverview.this, message, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        final EditText editText = (EditText) findViewById(R.id.activity_store_overview_firebase_item_edittext);


        Button addButton = (Button) findViewById(R.id.activity_store_overview_add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString();
                Log.d(TAG, "Adding firebase item " + text);
                Firebase newPostRef = _firebase.push();
                newPostRef.setValue(text, new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        editText.setText("");
                        String message = firebaseError == null ? getString(R.string.data_saved_success) : getString(R.string.data_saved_failure) + " " + firebaseError.getMessage();
                        Toast.makeText(StoreOverview.this, message, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });


        initRecyclerView();

        boolean fireBaseActive = firebaseStoreName != null;
        fireBaseActiveView(fireBaseActive);

        if(fireBaseActive){
            getSupportActionBar().setTitle(firebaseStoreName);
            _firebase = OrdinaSync.getInstance().getFireBaseStore().getChileFireBaseRef(firebaseStoreName);
            //TODO fbousson should be replaced by addChildEventListener for more finegrained item manipulation..
            _firebase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Map<String, String> values = (Map) dataSnapshot.getValue();
                    Log.d(TAG, "Data changed " + values);
                    _list.clear();
                    if(values != null){
                        for(Map.Entry<String, String> entry:  values.entrySet()){
                            _list.add(new Message(entry.getKey(), entry.getValue()));
                        }
                    }

                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Log.e(TAG, firebaseError.toString());
                }
            });
        }
    }

    private void initRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.activity_store_overview_items_recyclerview);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(_list);
        mRecyclerView.setAdapter(mAdapter);
    }


    static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private List<Message> mDataset;

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
        public MyAdapter(List<Message> myDataset) {
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
            String text = mDataset.get(position).getText();
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
