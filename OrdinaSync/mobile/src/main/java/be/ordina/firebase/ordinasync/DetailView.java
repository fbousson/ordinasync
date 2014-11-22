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

import com.firebase.client.Firebase;

import be.ordina.firebase.ordinasync.domain.Message;

/**
 * Created by fbousson on 18/11/14.
 */
public class DetailView extends ActionBarActivity {

    private static String TAG = DetailView.class.getSimpleName();

    private Firebase _firebase;

    private Message _message;


    public static final String DETAILVIEW_MESSAGE = "detailview_message";
    public static final String DETAILVIEW_FIREBASE = "detailview_firebase";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailview);

        EditText editText = (EditText) findViewById(R.id.activity_detailview_edit_text);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                String text = s.toString();
                //TODO fbousson update firebase item!
            }
        });

        _message = getMessage(getIntent());
        if(_message == null){
            Toast.makeText(this, getString(R.string.no_message_passed), Toast.LENGTH_LONG).show();
        }else{
            editText.setText(_message.getText());
        }


        Button deleteButton = (Button) findViewById(R.id.activity_detailview_delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });



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
