package be.ordina.firebase.ordinasync.util;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.firebase.client.Firebase;

import be.ordina.firebase.ordinasync.application.OrdinaSync;

/**
 * Created by fbousson on 07/12/14.
 */
public class FirebaseUtil {

    private static final String TAG = FirebaseUtil.class.getSimpleName();

    public static final String EXTRA_FIREBASE_REF = "EXTRA_UTIL_FIREBASE_REF";
    public static final String EXTRA_FIREBASE_ITEM_REF = "EXTRA_UTIL_FIREBASE_CHILD_REF";

    public static Firebase getFirebase(Intent intent, String firebaseExtra) {
        Bundle bundle = intent.getExtras();
        Firebase firebase = null;
        if(bundle != null){
            String firebaseName = (String) bundle.getString(firebaseExtra);
            Log.d(TAG, "Found firebaseName: " + firebaseName);
            if(firebaseName != null){
                firebase =  OrdinaSync.getInstance().getFireBaseStore().getChileFireBaseRef(firebaseName);
            }
        }
        Log.d(TAG, "Firebase passed on intent: " + firebase);
        return firebase;
    }


}
