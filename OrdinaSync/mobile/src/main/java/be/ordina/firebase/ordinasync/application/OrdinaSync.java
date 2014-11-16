package be.ordina.firebase.ordinasync.application;

import android.app.Application;

import com.firebase.client.Firebase;

import be.ordina.firebase.ordinasync.R;
import be.ordina.firebase.ordinasync.service.FirebaseStore;

/**
 * Created by fbousson on 16/11/14.
 */
public class OrdinaSync extends Application {


    private static OrdinaSync _instance;

    private FirebaseStore _firebaseStore;

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        _instance = this;
        _firebaseStore = new FirebaseStore(getResources().getString(R.string.firebase_store_ref));
    }

    public static OrdinaSync getInstance(){
        return _instance;
    }

    public FirebaseStore getFireBaseStore(){
        return _firebaseStore;
    }





}
