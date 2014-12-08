package be.ordina.firebase.ordinasync;

/**
 * Created by fbousson on 22/11/14.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by fbousson on 22/11/14.
 */
public class ThreeWayMerge extends ActionBarActivity {


    public static final String SERVER_VALUE = "threewaymerge_servervalue";
    public static final String LOCAL_VALUE= "threewaymerge_localvalue";
    public static final String ORIGINAL_VALUE ="threewaymerge_originalvalue";

    public static final String EXTRA_RESULT = "extra_threewaymerge_result";
    public static final String EXTRA_MERGE_SERVER_VALUE = "extra_threewaymerge_servervalue";

    private String _serverValueAtMergeTime;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_threeway_merge);

        Bundle bundle = getIntent().getExtras();

        final EditText serverEditText = (EditText) findViewById(R.id.threeway_merge_server_edit);
        _serverValueAtMergeTime = bundle.getString(SERVER_VALUE);
        serverEditText.setText(_serverValueAtMergeTime);
        Button pickServerButton = (Button) findViewById(R.id.threeway_merge_pick_server);
        pickServerButton.setOnClickListener(new SelectionListener(serverEditText));

        final EditText localEditText = (EditText) findViewById(R.id.threeway_merge_local_edit);
        localEditText.setText(bundle.getString(LOCAL_VALUE));
        Button pickLocalButton = (Button) findViewById(R.id.threeway_merge_pick_local);
        pickLocalButton.setOnClickListener(new SelectionListener(localEditText));

        final EditText originalEditText = (EditText) findViewById(R.id.threeway_merge_original_edit);
        originalEditText.setText(bundle.getString(ORIGINAL_VALUE));
        Button pickOriginalButton = (Button) findViewById(R.id.threeway_merge_pick_original);
        pickOriginalButton.setOnClickListener(new SelectionListener(originalEditText));


    }



    private class SelectionListener implements View.OnClickListener{

        private EditText _editText;


        protected SelectionListener(EditText editText) {
            _editText = editText;
        }

        @Override
        public void onClick(View v) {
            //getListener().onValueSelected(_editText.getText().toString());
            //return to previous activity with result
            String selectedValue = _editText.getText().toString();
            Intent intent = new Intent();
            intent.putExtra(EXTRA_RESULT, selectedValue);
            intent.putExtra(EXTRA_MERGE_SERVER_VALUE, _serverValueAtMergeTime);
            Toast.makeText(ThreeWayMerge.this, selectedValue, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK, intent);
            finish();
        }
    }

}