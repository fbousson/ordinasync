package be.ordina.firebase.ordinasync;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import be.ordina.firebase.ordinasync.application.OrdinaSync;

/**
 * Created by fbousson on 16/11/14.
 */
public class StoreChoiceActivity extends ActionBarActivity {


    private static String TAG = StoreChoiceActivity.class.getSimpleName();

    private static final String PREFS_NAME = "UserPreferencesFile";
    private static final String STORE_NAME = "storeName";
    private  SharedPreferences.Editor _editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_choice);


        SharedPreferences userSharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        _editor = userSharedPreferences.edit();
        String storeName = userSharedPreferences.getString(STORE_NAME, getString(R.string.default_store_child_ref));

        final EditText storeChoiceEditText = (EditText) findViewById(R.id.activity_store_choice_store_ref_name);
        storeChoiceEditText.setText(storeName);

        final Button confirmButton = (Button) findViewById(R.id.activity_store_choice_confirm_button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String storeRefName = storeChoiceEditText.getText().toString();
                _editor.putString(STORE_NAME, storeRefName);
                _editor.apply();
                Log.d(TAG, "StoreRef Name " + storeRefName);
                startStoreOverView(storeRefName);
            }
        });

    }

    private void startStoreOverView(String storeRefName) {
        Intent intent = new Intent(this, StoreOverview.class);
        intent.putExtra(StoreOverview.STORE_OVERVIEW_STORE_REF_NAME, storeRefName);
        startActivity(intent);

    }


}
