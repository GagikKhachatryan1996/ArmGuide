package com.ArmGuide.tourapplication.Repositories;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RepositoryForSubscribedPlacesNames {

    private MutableLiveData<List<String>> mutableLiveData;

    private static RepositoryForSubscribedPlacesNames repositoryForSubscribedPlacesNames;

    public static RepositoryForSubscribedPlacesNames getInstance() {
        if (repositoryForSubscribedPlacesNames == null)
            repositoryForSubscribedPlacesNames = new RepositoryForSubscribedPlacesNames();
        return repositoryForSubscribedPlacesNames;
    }

    private RepositoryForSubscribedPlacesNames() {
        mutableLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<List<String>> getLiveData() {
        getSubscribedPlaces();
        return mutableLiveData;
    }

    private void getSubscribedPlaces() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase.getInstance().getReference().child("Tourists").child(userId).child("getSubscribedPlacesIds")
                    .addValueEventListener(new ValueEventListener() {
                        List<String> placesSubscribed = new ArrayList<>();

                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()
                            ) {
                                placesSubscribed.add(snapshot.getValue(String.class));
                            }
                            if (placesSubscribed.size() > 0) {
                                mutableLiveData.setValue(placesSubscribed);
                                Log.d("dobbi", "placesSubscribed from onDataChange/" + placesSubscribed.size());
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
        }
    }
}
