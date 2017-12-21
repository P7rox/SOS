package com.prajwal.firebasesos;

import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class VictimMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private DatabaseReference mFirebaseDatabase;
    private DatabaseReference dFirebaseDatabase;
    private DatabaseReference rFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private ArrayList<Marker> markers;
    private String helperName;
    private LatLng helperLocation;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_victim_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        auth = FirebaseAuth.getInstance();


        //get current user
        user = FirebaseAuth.getInstance().getCurrentUser();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(VictimMapsActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };
        mFirebaseInstance = FirebaseDatabase.getInstance();
        rFirebaseDatabase = mFirebaseInstance.getReference("request");
        mFirebaseDatabase = mFirebaseInstance.getReference("users");
        dFirebaseDatabase = mFirebaseDatabase.child(user.getUid()).child("Helper");

        fetchData();
    }

    private void filMap() {
                markers = new ArrayList<>();

                mFirebaseDatabase.child(user.getUid()).child("Helper").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        markers.clear();
                        mMap.clear();
                        for(DataSnapshot child : dataSnapshot.getChildren() ){
                            // Do magic here
                            HelperMarker requestMarker = new HelperMarker();
                            requestMarker.uidMarker = child.child("uidMarker").getValue(String.class);
                            requestMarker.nameMarker = child.child("nameMarker").getValue(String.class);
                            requestMarker.latLngMarker = new LatLng(child.child("latLngMarker").child("latitude").getValue(double.class), child.child("latLngMarker").child("longitude").getValue(double.class));
                            if(requestMarker.latLngMarker!=null && requestMarker.nameMarker!=null) {
                                LatLng locationLatLng = requestMarker.latLngMarker;
                                String name = requestMarker.nameMarker;
                                if (user.getUid().toString().equals(requestMarker.uidMarker)) {
                                    markers.add(mMap.addMarker(new MarkerOptions().position(locationLatLng).title(name)));
                                }
                                else {
                                    markers.add(mMap.addMarker(new MarkerOptions().position(locationLatLng).title(name).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));
                                }
                            }
                        }
                        updateMap(markers);
                    }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
                });
    }

    private void fetchData() {
        mFirebaseDatabase.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                helperName = dataSnapshot.child("name").getValue(String.class);
                helperLocation = new LatLng(dataSnapshot.child("myLocation").child("latitude").getValue(double.class), dataSnapshot.child("myLocation").child("longitude").getValue(double.class));
                addRequest(user.getUid(),helperName,helperLocation);
                addMarker(user.getUid(),helperName,helperLocation);
                filMap();
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    public void updateMap(ArrayList<Marker> Markers) {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : Markers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();


        int padding = 60; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        mMap.animateCamera(cu);

    }

    public void addRequest(String uid, String name, LatLng location){
        Request request = new Request(uid, name, location, 0);
        //String childID = rFirebaseDatabase.push().getKey();
        rFirebaseDatabase.child(uid).setValue(request);
    }
    public void addMarker(String uid, String name, LatLng location){
        //dFirebaseDatabase.child(uid).removeValue();
        HelperMarker helperMarker = new HelperMarker(uid, name, location);
        //String childID = dFirebaseDatabase.push().getKey();
        dFirebaseDatabase.child(uid).setValue(helperMarker);
    }
}
