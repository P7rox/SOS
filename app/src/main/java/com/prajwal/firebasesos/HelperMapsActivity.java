package com.prajwal.firebasesos;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HelperMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private DatabaseReference mFirebaseDatabase;
    private DatabaseReference rFirebaseDatabase;
    private DatabaseReference dFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private DatabaseReference eFirebaseDatabase;
    private FirebaseUser user;

    private String helperName;
    private LatLng helperLocation;
    private LatLng requestLocation;
    private ArrayList<Marker> markers;
    private Request request;

    private Button btnRoute;

    private List<Polyline> polylinePaths = new ArrayList<>();
    static InputStream is = null;
    static JSONObject json = null;
    static String output = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helper_maps);


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
                    startActivity(new Intent(HelperMapsActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        Intent intent = getIntent();

        mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDatabase = mFirebaseInstance.getReference("users");
        rFirebaseDatabase = mFirebaseInstance.getReference("request");
        Log.e("helper",String.valueOf(intent.getStringExtra("uid")));
        dFirebaseDatabase = mFirebaseDatabase.child(intent.getStringExtra("uid")).child("Helper");
        eFirebaseDatabase = rFirebaseDatabase.child(intent.getStringExtra("uid"));

        markers = new ArrayList<>();
        btnRoute = (Button) findViewById(R.id.routeMarker);
        RelativeLayout mapLayout = (RelativeLayout)findViewById(R.id.mapHelper);


        fetchData();

        btnRoute.setEnabled(false);
        btnRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent directionsIntent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?saddr=" + helperLocation.latitude + "," + helperLocation.longitude + "&daddr=" + requestLocation.latitude + "," + requestLocation.longitude));
                startActivity(directionsIntent);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void fetchData() {
        mFirebaseDatabase.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                helperName = dataSnapshot.child("name").getValue(String.class);
                helperLocation = new LatLng(dataSnapshot.child("myLocation").child("latitude").getValue(double.class), dataSnapshot.child("myLocation").child("longitude").getValue(double.class));
                addVictimHelperMarker(user.getUid(),helperName,helperLocation);
                addRequestMarker();
                //updateMap(markers);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void addVictimHelperMarker(String uid, String name, LatLng location){
        //dFirebaseDatabase.child(uid).removeValue();
        HelperMarker helperMarker = new HelperMarker(uid, name, location);
        dFirebaseDatabase.child(uid).setValue(helperMarker);
    }


    private void addOwnMarker() {
        if(helperLocation!=null && helperName!=null) {
            LatLng locationLatLng = helperLocation;
            String name = helperName;
            markers.add(mMap.addMarker(new MarkerOptions().position(locationLatLng).title("hero")));
        }
    }

    private void addRequestMarker() {
        eFirebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                request = new Request();
                request.uid = dataSnapshot.child("uid").getValue(String.class);
                request.name = dataSnapshot.child("name").getValue(String.class);
                request.latLng = new LatLng(dataSnapshot.child("latLng").child("latitude").getValue(double.class), dataSnapshot.child("latLng").child("longitude").getValue(double.class));
                requestLocation = request.latLng;
                markers.clear();
                mMap.clear();

                if(request.latLng!=null && request.name!=null) {
                    LatLng locationLatLng = request.latLng;
                    String name = request.name;
                    if (user.getUid() == request.uid)
                        markers.add(mMap.addMarker(new MarkerOptions().position(locationLatLng).title(name)));
                    else
                        markers.add(mMap.addMarker(new MarkerOptions().position(locationLatLng).title(name).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));
                }

                addOwnMarker();
                updateMap(markers);
                btnRoute.setEnabled(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void updateMap(ArrayList<Marker> Markers) {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : Markers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();

        int padding = 300; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        drawPolyLine();
        mMap.animateCamera(cu);

    }

    public void drawPolyLine() {

        try {
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        int SDK_INT = android.os.Build.VERSION.SDK_INT;
                        if (SDK_INT > 8) {
                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                                    .permitAll().build();
                            StrictMode.setThreadPolicy(policy);

                        }

                        String origin = helperLocation.latitude + "," + helperLocation.longitude;
                        String destination = requestLocation.latitude + "," + requestLocation.longitude;
                        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + origin + "&destination=" + destination + "&key=AIzaSyDbqAYTFPtUaj_2Wd3R5G7dt2e1-SdEYX8\n";

                        URL Url = null;
                        HttpURLConnection urlConnection = null;


                        try {
                            Url = new URL(url);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        try {
                            urlConnection = (HttpURLConnection) Url.openConnection();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        try {
                            is = new BufferedInputStream(urlConnection.getInputStream());
                            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                            StringBuilder total = new StringBuilder(is.available());
                            String line;
                            while ((line = reader.readLine()) != null) {
                                total.append(line).append('\n');
                            }
                            output = total.toString();
                        } catch (IOException e) {
                            Log.e("JSON Parser", "IO error " + e.toString());

                        } finally {
                            urlConnection.disconnect();
                        }

                        try {
                            json = new JSONObject(output);
                        } catch (JSONException e) {
                            Log.e("JSON Parser", "Error parsing data " + e.toString());
                        }


                        List<Route> routes = new ArrayList<>();
                        JSONObject jsonData = json;
                        JSONArray jsonRoutes = null;


                        try {
                            jsonRoutes = jsonData.getJSONArray("routes");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        for (int j = 0; j < jsonRoutes.length(); j++) {

                            JSONObject jsonRoute = null;
                            try {
                                jsonRoute = jsonRoutes.getJSONObject(j);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Route route = new Route();


                            JSONObject overview_polylineJson = null;
                            try {
                                overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            try {
                                route.points = decodePolyLine(overview_polylineJson.getString("points"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            routes.add(route);
                        }

                        plot(routes);

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
    }

    private List<LatLng> decodePolyLine(final String poly) {
        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng((((double) lat / 1e5)),
                    (((double) lng / 1e5))));
        }

        return decoded;
    }

    public void plot(List<Route> routes) {


        for (Route route : routes) {

            PolylineOptions polylineOptions = new PolylineOptions().
                    color(Color.BLUE).
                    width(10);

            for (int i = 0; i < route.points.size(); i++) {
                polylineOptions.add(route.points.get(i));
            }

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }


}
