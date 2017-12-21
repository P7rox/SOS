package com.prajwal.firebasesos;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ListRequestActivity extends AppCompatActivity
{

        private FirebaseAuth.AuthStateListener authListener;
        private FirebaseAuth auth;
        private DatabaseReference mFirebaseDatabase;
        private DatabaseReference rFirebaseDatabase;
        private DatabaseReference dFirebaseDatabase;
        private FirebaseDatabase mFirebaseInstance;
        private DatabaseReference eFirebaseDatabase;
        private FirebaseUser user;

        ListView requestList;
        TextView noRequestText;
        ArrayList<Item> arrayList = new ArrayList<Item>();
        ArrayList<String> uid = new ArrayList<String>();
        ArrayList<String> name = new ArrayList<String>();
        ArrayList<LatLng> latLng = new ArrayList<LatLng>();
        ArrayList<Integer> count = new ArrayList<Integer>();
        ArrayList<Double> directDistance = new ArrayList<Double>();
        ArrayList<String> travelDistance = new ArrayList<String>();
        Location locationM;

        Integer updateCount;

        String result_in_kms;
        String tag[] = { "text" };
        String tagAddress[] = { "end_address" };
        String url;

        int totalRequest = 0;
        ProgressDialog pd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_request);

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
                    startActivity(new Intent(ListRequestActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDatabase = mFirebaseInstance.getReference("users");
        rFirebaseDatabase = mFirebaseInstance.getReference("request");

        requestList = (ListView)findViewById(R.id.requestList);
        noRequestText = (TextView)findViewById(R.id.noRequestText);

            pd = new ProgressDialog(ListRequestActivity.this);
            pd.setMessage("Loading...");
            pd.show();


            requestList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    increaseRequestCount(uid.get(position));
                    Intent intent = new Intent(ListRequestActivity.this, HelperMapsActivity.class);
                    intent.putExtra("uid",uid.get(position));
                    startActivity(intent);
                }
            });

            fetchData();
    }

    private void increaseRequestCount(String s) {

        rFirebaseDatabase.child(s).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                updateCount = dataSnapshot.child("count").getValue(Integer.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        if(updateCount != null) {
            updateCount++;
            rFirebaseDatabase.child(s).child("count").setValue(updateCount);
        }
    }

    private void fillList() {
        if (!arrayList.isEmpty()) {
            noRequestText.setVisibility(View.GONE);
            requestList.setVisibility(View.VISIBLE);
            MyAdapter myAdapter = new MyAdapter(this, R.layout.list_view_items, arrayList);
            requestList.setAdapter(myAdapter);
        }
        else {
            noRequestText.setVisibility(View.VISIBLE);
            requestList.setVisibility(View.GONE);
        }
        pd.dismiss();
    }

    private void calLoc() {
        rFirebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                arrayList.clear();
                uid.clear();
                travelDistance.clear();
                name.clear();
                for(DataSnapshot child : dataSnapshot.getChildren() ){
                    // Do magic here
                    Item item = new Item();

                    String uidR = child.child("uid").getValue(String.class);
                    String nameR = child.child("name").getValue(String.class);
                    Location locationR = new Location(LocationManager.GPS_PROVIDER);
                    locationR.setLatitude(child.child("latLng").child("latitude").getValue(double.class));
                    locationR.setLongitude(child.child("latLng").child("longitude").getValue(double.class));
                    Integer count = child.child("count").getValue(Integer.class);
                    float distanceInM = locationR.distanceTo(locationM);
                    String travel = getDistanceOnRoad(locationM.getLatitude(),locationM.getLongitude(),locationR.getLatitude(),locationR.getLongitude());
                    String time = getDurationOnRoad(locationM.getLatitude(),locationM.getLongitude(),locationR.getLatitude(),locationR.getLongitude());
                    String address = getAddress(locationM.getLatitude(),locationM.getLongitude(),locationR.getLatitude(),locationR.getLongitude());

                    Log.d("output",String.valueOf(distanceInM));

                    if (count <6 && distanceInM < 10000) {
                        item.personName = nameR;
                        item.distance = travel;
                        item.personImage = R.drawable.sos_image;
                        item.time = time;
                        item.address = address;

                        arrayList.add(item);

                        uid.add(uidR);
                        travelDistance.add(travel);
                        name.add(nameR);
                    }
                }
                fillList();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void fetchData() {
        mFirebaseDatabase.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                locationM = new Location("My");
                locationM.setLatitude(dataSnapshot.child("myLocation").child("latitude").getValue(double.class));
                locationM.setLongitude(dataSnapshot.child("myLocation").child("longitude").getValue(double.class));
                calLoc();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private String getDistanceOnRoad(double latitude, double longitude,
                                     double prelatitute, double prelongitude) {
        result_in_kms = "";
            url = "https://maps.google.com/maps/api/directions/xml?origin="
                + latitude + "," + longitude + "&destination=" + prelatitute
                + "," + prelongitude + "&key=AIzaSyDbqAYTFPtUaj_2Wd3R5G7dt2e1-SdEYX8&sensor=true&units=metric";

        try {
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        URL urlObj = new URL(url);
                        HttpURLConnection urlConnection = (HttpURLConnection) urlObj.openConnection();
                        InputStream is = urlConnection.getInputStream();

                        DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                                .newDocumentBuilder();
                        Document doc = builder.parse(is);
                        if (doc != null) {
                            NodeList nl;
                            ArrayList args = new ArrayList();
                            for (String s : tag) {
                                nl = doc.getElementsByTagName(s);
                                if (nl.getLength() > 0) {
                                    Node node = nl.item(nl.getLength() - 1);
                                    args.add(node.getTextContent());
                                } else {
                                    args.add(" - ");
                                }
                            }
                            result_in_kms = String.format("%s", args.get(0));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
            thread.join();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return result_in_kms;
    }

    private String getDurationOnRoad(double latitude, double longitude,
                                     double prelatitute, double prelongitude) {
        result_in_kms = "";
        url = "https://maps.google.com/maps/api/directions/xml?origin="
                + latitude + "," + longitude + "&destination=" + prelatitute
                + "," + prelongitude + "&key=AIzaSyDbqAYTFPtUaj_2Wd3R5G7dt2e1-SdEYX8&sensor=true&units=metric";

        try {
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        URL urlObj = new URL(url);
                        HttpURLConnection urlConnection = (HttpURLConnection) urlObj.openConnection();
                        InputStream is = urlConnection.getInputStream();

                        DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                                .newDocumentBuilder();
                        Document doc = builder.parse(is);
                        if (doc != null) {
                            NodeList nl;
                            ArrayList args = new ArrayList();
                            for (String s : tag) {
                                nl = doc.getElementsByTagName(s);
                                if (nl.getLength() > 0) {
                                    Node node = nl.item(nl.getLength() - 2);
                                    args.add(node.getTextContent());
                                } else {
                                    args.add(" - ");
                                }
                            }
                            result_in_kms = String.format("%s", args.get(0));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
            thread.join();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return result_in_kms;
    }

    private String getAddress(double latitude, double longitude,
                              double prelatitute, double prelongitude) {
        result_in_kms = "";
        url = "https://maps.google.com/maps/api/directions/xml?origin="
                + latitude + "," + longitude + "&destination=" + prelatitute
                + "," + prelongitude + "&key=AIzaSyDbqAYTFPtUaj_2Wd3R5G7dt2e1-SdEYX8&sensor=true&units=metric";
        Log.v("output",url);
        try {
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        URL urlObj = new URL(url);
                        HttpURLConnection urlConnection = (HttpURLConnection) urlObj.openConnection();
                        InputStream is = urlConnection.getInputStream();

                        DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                                .newDocumentBuilder();
                        Document doc = builder.parse(is);
                        if (doc != null) {
                            NodeList nl;
                            ArrayList args = new ArrayList();
                            for (String s : tagAddress) {
                                nl = doc.getElementsByTagName(s);
                                if (nl.getLength() > 0) {
                                    Node node = nl.item(nl.getLength() - 1);
                                    args.add(node.getTextContent());
                                } else {
                                    args.add(" - ");
                                }
                            }
                            result_in_kms = String.format("%s", args.get(0));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
            thread.join();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return result_in_kms;
    }
}
