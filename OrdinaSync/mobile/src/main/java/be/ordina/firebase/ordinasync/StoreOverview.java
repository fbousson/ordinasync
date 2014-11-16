package be.ordina.firebase.ordinasync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.client.Firebase;

import be.ordina.firebase.ordinasync.application.OrdinaSync;

/**
 * Created by fbousson on 16/11/14.
 */
public class StoreOverview extends ActionBarActivity {


    private static String TAG = StoreOverview.class.getSimpleName();

    public static final String STORE_OVERVIEW_STORE_REF_NAME = "OrdinaSync_storeOverviewRefName";

    private View _noFirebase;
    private View _firebaseActive;

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

        boolean fireBaseActive = firebaseStoreName != null;
        fireBaseActiveView(fireBaseActive);

        if(fireBaseActive){
            getSupportActionBar().setTitle(firebaseStoreName);
            firebase = OrdinaSync.getInstance().getFireBaseStore().getChileFireBaseRef(firebaseStoreName);
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
