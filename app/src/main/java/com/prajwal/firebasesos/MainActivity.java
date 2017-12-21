package com.prajwal.firebasesos;

        import android.content.Context;
        import android.content.Intent;
        import android.content.pm.PackageManager;
        import android.location.Location;
        import android.location.LocationListener;
        import android.location.LocationManager;
        import android.media.MediaPlayer;
        import android.os.Build;
        import android.os.Bundle;
        import android.provider.Settings;
        import android.support.annotation.NonNull;
        import android.support.v4.app.ActivityCompat;
        import android.support.v4.app.FragmentManager;
        import android.support.v4.content.ContextCompat;
        import android.support.v7.app.AppCompatActivity;
        import android.support.v7.widget.Toolbar;
        import android.view.View;
        import android.widget.Button;
        import android.widget.CompoundButton;
        import android.widget.ToggleButton;

        import com.google.android.gms.maps.model.LatLng;
        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.auth.FirebaseUser;
        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements  CompoundButton.OnCheckedChangeListener {

    private Button btnSOS, btnLogout, btnSendPicture, btnSetting, btnGlobalChat;
    private ToggleButton btnWhistle ;
    private MediaPlayer mPlayer;


    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;

    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Home");
        setSupportActionBar(toolbar);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };


        btnSOS = (Button) findViewById(R.id.sosButton);
        btnLogout = (Button) findViewById(R.id.logout);
        btnSendPicture = (Button) findViewById(R.id.picture);
        btnWhistle = (ToggleButton) findViewById(R.id.whistle);
        btnSetting = (Button) findViewById(R.id.setting);
        btnGlobalChat = (Button) findViewById(R.id.chat);


        mFirebaseInstance = FirebaseDatabase.getInstance();

        // get reference to 'users' node
        mFirebaseDatabase = mFirebaseInstance.getReference("users");

        //------------------------------------------

        LocationFragment locationFragment = new LocationFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.locationLayout, locationFragment).commit();

        //--------------------------------------------



        //--------------------------------------------------


        btnSOS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, VictimMapsActivity.class));
            }
        });

        btnSendPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ListRequestActivity.class));
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
            }
        });

        btnWhistle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    mPlayer = MediaPlayer.create(getApplicationContext(),R.raw.whistle_sound);
                    mPlayer.setLooping(true);//Create MediaPlayer object with MP3 file under res/raw folder
                    mPlayer.start();//Start playing the music
                } else {
                    // The toggle is disabled
                    if (mPlayer != null && mPlayer.isPlaying()) {//If music is playing already
                        mPlayer.stop();//Stop playing the music
                        mPlayer.setLooping(false);
                        mPlayer.release();

                    }
                }
            }
        });

        btnGlobalChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Chat.class));
            }
        });

        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
            }
        });

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateMap(location, user.getUid());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };


        if (Build.VERSION.SDK_INT < 23) {
            locationManager.requestLocationUpdates("gps", 5000, 5, locationListener);
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates("gps", 5000, 5, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation != null) {
                    //updateMap(lastKnownLocation, user.getUid());
                }
            }
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }

    public void updateMap(Location location, String uid) {
        LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        mFirebaseDatabase.child(uid).child("myLocation").setValue(newLatLng);
    }
}