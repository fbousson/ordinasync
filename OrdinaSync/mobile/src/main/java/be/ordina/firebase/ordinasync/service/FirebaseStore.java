package be.ordina.firebase.ordinasync.service;

import com.firebase.client.Firebase;

/**
 * Created by fbousson on 16/11/14.
 */
public class FirebaseStore {


    private final String STORE_REF;
    private final Firebase myFirebaseRef;


    public FirebaseStore(String storeRef) {
        STORE_REF = storeRef;
        myFirebaseRef  = new Firebase(STORE_REF);
    }


    public Firebase getChileFireBaseRef(String childStoreRef){
      return myFirebaseRef.child(childStoreRef);
    }


}
