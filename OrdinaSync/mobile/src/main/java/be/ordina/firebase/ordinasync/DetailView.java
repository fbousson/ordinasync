package be.ordina.firebase.ordinasync;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
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
public class DetailView extends ActionBarActivity {

    private static String TAG = DetailView.class.getSimpleName();

    private Firebase _firebase;

    private Firebase _itemFirebase;

    private Message _message;

    private String _textBeforeChange;


    public static final String DETAILVIEW_MESSAGE = "detailview_message";
    public static final String DETAILVIEW_FIREBASE = "detailview_firebase";

    public static final int DELETE_SUCCESS = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailview);

        _message = getMessage(getIntent());
        _firebase = getFirebase(getIntent());

        _itemFirebase = _firebase.child(_message.getKey());

        final EditText editText = (EditText) findViewById(R.id.activity_detailview_edit_text);
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                _textBeforeChange = s.toString();
                Log.d(TAG, "Text before change: " + _textBeforeChange);

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                final String text = s.toString();

                _itemFirebase.runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData currentData) {

                        String serverValue = (String) currentData.getValue();

                        if(serverValue == null) {
                            currentData.setValue(text);
                        } else {
                            Log.d(TAG,"Server value " + serverValue + " local value before change" + _textBeforeChange + " changing to " + text);

                           if(!serverValue.equals(_textBeforeChange)){
                               //show popup with both values
                               return Transaction.abort();
                           }else{
                               currentData.setValue(text);
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

                    }
                });

            }
        });


        //might want to populate the field with the firebase valued instead of the other value.
        _itemFirebase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = (String) dataSnapshot.getValue();
                editText.setText(value);

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.w(TAG, "Firebase error onCancelled" + firebaseError);
            }
        });


        Button deleteButton = (Button) findViewById(R.id.activity_detailview_delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });



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
