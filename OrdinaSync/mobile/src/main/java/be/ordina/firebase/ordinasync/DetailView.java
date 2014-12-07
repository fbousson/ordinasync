package be.ordina.firebase.ordinasync;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;
import com.firebase.client.ValueEventListener;

import be.ordina.firebase.ordinasync.application.OrdinaSync;
import be.ordina.firebase.ordinasync.domain.Message;

/**
 * Created by fbousson on 18/11/14.
 */
public class DetailView extends ActionBarActivity  {

    private static String TAG = DetailView.class.getSimpleName();

    private Firebase _firebase;

    private Firebase _itemFirebase;

    private Message _message;

    private String _originalText;
    private String _localValue;
    private String _serverValue;


    public static final String DETAILVIEW_MESSAGE = "detailview_message";
    public static final String DETAILVIEW_FIREBASE = "detailview_firebase";

    public static final int DELETE_SUCCESS = 1;

    private static final int PICK_VALUE_REQUEST = 2;


    private ValueEventListener _valueEventListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailview);

        _message = getMessage(getIntent());
        _firebase = getFirebase(getIntent());
        _itemFirebase = _firebase.child(_message.getKey());



        _originalText = _message.getText();
        _localValue = _originalText;


        final EditText editText = (EditText) findViewById(R.id.activity_detailview_edit_text);
        final Button mergeButton = (Button) findViewById(R.id.activity_detailview_merge_button);
        mergeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startThreeWayMerge(_serverValue, editText.getText().toString(), _originalText);
            }
        });





        Button updateButton = (Button)findViewById(R.id.activity_detailview_update_button);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateItem(editText.getText().toString());
            }
        });


        Button deleteButton = (Button) findViewById(R.id.activity_detailview_delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteItem();

            }
        });


        _valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                _serverValue = (String) dataSnapshot.getValue();
                if(_localValue.equals(_serverValue)){
                    editText.setText(_serverValue);
                }else{
                    //Notify user that there is a merge conflict
                    Toast.makeText(DetailView.this, getString(R.string.activity_detailview_merge_conflict), Toast.LENGTH_SHORT).show();
                    mergeButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.w(TAG, "Firebase error onCancelled" + firebaseError);
            }
        };
    }


    @Override
    protected void onResume() {
        super.onResume();
        //might want to populate the field with the firebase valued instead of the other value.
        _itemFirebase.addValueEventListener(_valueEventListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        _itemFirebase.removeEventListener(_valueEventListener);
    }

    private void updateItem(final String localText) {

        _localValue = localText;

        _itemFirebase.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {

                _serverValue = (String) currentData.getValue();

                if(_serverValue == null) {
                    currentData.setValue(localText);
                } else {
                    Log.d(TAG, "Server value " + _serverValue + " local value before change" + _originalText + " changing to " + localText);

                    if(!_serverValue.equals(_originalText)){
                        //show popup with both values
                        startThreeWayMerge(_serverValue, localText, _originalText);
                        return Transaction.abort();
                    }else{
                        currentData.setValue(localText);
                    }
                }
                return Transaction.success(currentData); //we can also abort by calling Transaction.abort()
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean committed, DataSnapshot dataSnapshot) {
                Log.d(TAG, "Committed " + committed);
                if(!committed){
                    Toast.makeText(DetailView.this, "Not commited", Toast.LENGTH_SHORT).show();
                }
                if(firebaseError != null){
                    toastFirebaseError(firebaseError);
                }else{
                    Log.d(TAG, "Value after transaction: " + dataSnapshot.getValue());
                }

                if(committed && firebaseError == null){
                    _originalText = (String) dataSnapshot.getValue();
                    Log.d(TAG, "Sync success. Updating original value: " + _originalText );

                }

            }
        });
    }

    private void startThreeWayMerge(String serverValue, String localText, String originalText) {
        Log.d(TAG, "Starting 3 way merge");
        Intent intent = new Intent(this, ThreeWayMerge.class);
        intent.putExtra(ThreeWayMerge.SERVER_VALUE, serverValue);
        intent.putExtra(ThreeWayMerge.LOCAL_VALUE, localText);
        intent.putExtra(ThreeWayMerge.ORIGINAL_VALUE, originalText);
        startActivityForResult(intent, PICK_VALUE_REQUEST);
    }

    private void deleteItem() {
        _itemFirebase.removeValue(new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {

                if(firebaseError != null){
                    toastFirebaseError(firebaseError);
                }else{
                    Toast.makeText(DetailView.this, getString(R.string.detailview_removed_item_success), Toast.LENGTH_LONG).show();
                    finishActivity(DELETE_SUCCESS);
                }

            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == PICK_VALUE_REQUEST) {
            if (resultCode == RESULT_OK) {
               //A value was passed. Read the data, update field
               Log.d(TAG, "Pick value request OK" + data);
            }
        }
    }

    private void toastFirebaseError(FirebaseError firebaseError) {
        Toast.makeText(this, firebaseError.toString(), Toast.LENGTH_LONG).show();
    }

    private Firebase getFirebase(Intent intent) {
        Bundle bundle = intent.getExtras();
        Firebase firebase = null;
        if(bundle != null){
            String firebaseName = (String) bundle.getString(DETAILVIEW_FIREBASE);
            Log.d(TAG, "Found firebaseName: " + firebaseName);
            if(firebaseName != null){
                firebase =  OrdinaSync.getInstance().getFireBaseStore().getChileFireBaseRef(firebaseName);
            }
        }
        Log.d(TAG, "Firebase passed on intent: " + firebase);
        return firebase;
    }

    private Message getMessage(Intent intent) {

        Bundle bundle = intent.getExtras();
        Message message = null;
        if(bundle != null){
            message = (Message) bundle.get(DETAILVIEW_MESSAGE);
        }

        Log.d(TAG, "Message passed on intent: " + message);

        return message;
    }


}
