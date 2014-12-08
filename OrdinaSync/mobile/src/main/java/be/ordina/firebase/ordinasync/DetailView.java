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

import be.ordina.firebase.ordinasync.domain.Message;
import be.ordina.firebase.ordinasync.util.FirebaseUtil;

/**
 * Created by fbousson on 18/11/14.
 */
public class DetailView extends ActionBarActivity  {

    private static String TAG = DetailView.class.getSimpleName();

    private Firebase _firebase;

    private Firebase _itemFirebase;

    private Message _message;

    private EditText _editText;

    private String _originalText;
    private String _localValue;
    private String _serverValue;


    public static final String DETAILVIEW_MESSAGE = "detailview_message";


    public static final int DELETE_SUCCESS = 1;

    private static final int PICK_VALUE_REQUEST = 2;

    private String _mergeServerValue;


    private ValueEventListener _valueEventListener;

    public String getServerValue() {
        return _serverValue;
    }

    public void setServerValue(String serverValue) {
        _serverValue = serverValue;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailview);
        _mergeServerValue = null;

        _message = getMessage(getIntent());
        _firebase = FirebaseUtil.getFirebase(getIntent(), FirebaseUtil.EXTRA_FIREBASE_REF);
        _itemFirebase = _firebase.child(_message.getKey());



        _originalText = _message.getText();
        _localValue = _originalText;


        _editText = (EditText) findViewById(R.id.activity_detailview_edit_text);
        final Button mergeButton = (Button) findViewById(R.id.activity_detailview_merge_button);
        mergeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startThreeWayMerge(getServerValue(), _editText.getText().toString(), _originalText);
            }
        });





        Button updateButton = (Button)findViewById(R.id.activity_detailview_update_button);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateItem(_editText.getText().toString());
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
                setServerValue((String) dataSnapshot.getValue());
                if(_localValue.equals(getServerValue())){
                    _editText.setText(getServerValue());
                }else if(_mergeServerValue != null){
                 Log.d(TAG, "Currently merging results. Local server data: " + _mergeServerValue);
                }
                else{
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

                //TODO fbousson check if server value has remained stable with old version and if resolve flag has been set.

                setServerValue((String) currentData.getValue());

                Log.d(TAG, "Old server value " + _mergeServerValue + " latest server value " + getServerValue());

                if(getServerValue() == null) {

                    currentData.setValue(localText);
                } else {
                    Log.d(TAG, "Server value " + getServerValue() + " local value before change" + _originalText + " changing to " + localText);
                    if(getServerValue().equals(_mergeServerValue)){
                        currentData.setValue(localText);
                    }else if(!getServerValue().equals(_originalText)){
                        //show popup with both values
                        startThreeWayMerge(getServerValue(), localText, _originalText);
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
                    _mergeServerValue = null;
                    _originalText = (String) dataSnapshot.getValue();
                    Log.d(TAG, "Sync success. Updating original value: " + _originalText );
                    finish();

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
                String resolvedValue = data.getExtras().getString(ThreeWayMerge.EXTRA_RESULT);
                _mergeServerValue = data.getExtras().getString(ThreeWayMerge.EXTRA_MERGE_SERVER_VALUE);
                _localValue = resolvedValue;
                _editText.setText(resolvedValue);
                updateItem(resolvedValue);
            }
        }
    }

    private void toastFirebaseError(FirebaseError firebaseError) {
        Toast.makeText(this, firebaseError.toString(), Toast.LENGTH_LONG).show();
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
