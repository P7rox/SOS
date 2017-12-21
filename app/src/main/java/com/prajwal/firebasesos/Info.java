package com.prajwal.firebasesos;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Info extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView txtDetails;
    private EditText inputName, inputEmail, inputAddress1, inputAddress2, inputPincode, inputPhone;
    private Button btnSave;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

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
                    startActivity(new Intent(Info.this, LoginActivity.class));
                    finish();
                }
            }
        };


        txtDetails = (TextView) findViewById(R.id.txt_user);
        inputName = (EditText) findViewById(R.id.name);
        inputEmail = (EditText) findViewById(R.id.email);
        inputAddress1 = (EditText) findViewById(R.id.address_1);
        inputAddress2 = (EditText) findViewById(R.id.address_2);
        inputPincode = (EditText) findViewById(R.id.pincode);
        inputPhone = (EditText) findViewById(R.id.phone);
        btnSave = (Button) findViewById(R.id.btn_save);

        mFirebaseInstance = FirebaseDatabase.getInstance();

        // get reference to 'users' node
        mFirebaseDatabase = mFirebaseInstance.getReference("users");

//--------------------------------------------------------------------------------

        if (TextUtils.isEmpty(userId)) {
            //userId = mFirebaseDatabase.push().getKey();
            FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
            userId = currentFirebaseUser.getUid();
        }

        mFirebaseDatabase.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserNoLoc user = dataSnapshot.getValue(UserNoLoc.class);

                // Check for null
                if (user == null) {
                    Log.e(TAG, "User data is null!");
                    return;
                }

                Log.e(TAG, "User data is changed!" + user.name + ", " + user.email);

                // Display newly updated name and email
                txtDetails.setText(user.name + ", " + user.email);

                // clear edit text
                //inputEmail.setText("");
                inputName.setText(user.name);
                inputAddress1.setText(user.address1);
                inputAddress2.setText(user.address2);
                inputPincode.setText(user.pincode);
                inputPhone.setText(user.phone);

                toggleButton();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read user", error.toException());
            }
        });

//-------------------------------------------------------------------------------

        // Save / update the user
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = inputName.getText().toString();
                String email = user.getEmail();
                String address1 = inputAddress1.getText().toString();
                String address2 = inputAddress2.getText().toString();
                String pincode = inputPincode.getText().toString();
                String phone = inputPhone.getText().toString();

                // Check for already existed userId
                if (TextUtils.isEmpty(userId)) {
                    createUser(name, email, address1, address2, pincode, phone);
                    startActivity(new Intent(Info.this, MainActivity.class));
                } else {
                    updateUser(name, email, address1, address2, pincode, phone);
                    startActivity(new Intent(Info.this, SettingActivity.class));
                }
                finish();
            }
        });

        toggleButton();
    }

    // Changing button text
    private void toggleButton() {
        if (TextUtils.isEmpty(userId)) {
            btnSave.setText("Save");
        } else {
            btnSave.setText("Update");
        }
    }

    /**
     * Creating new user node under 'users'
     */
    private void createUser(String name, String email, String address1, String address2, String pincode, String phone) {
        // TODO
        // In real apps this userId should be fetched
        // by implementing firebase auth
        if (TextUtils.isEmpty(userId)) {
            //userId = mFirebaseDatabase.push().getKey();
            FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
            userId = currentFirebaseUser.getUid();
        }

        User user = new User(name, email, address1, address2, pincode, phone);

        mFirebaseDatabase.child(userId).setValue(user);

        addUserChangeListener();
    }

    /**
     * User data change listener
     */
    private void addUserChangeListener() {
        // User data change listener
        mFirebaseDatabase.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                // Check for null
                if (user == null) {
                    Log.e(TAG, "User data is null!");
                    return;
                }

                Log.e(TAG, "User data is changed!" + user.name + ", " + user.email);

                // Display newly updated name and email
                txtDetails.setText(user.name + ", " + user.email);

                // clear edit text
                //inputEmail.setText("");
                inputName.setText("");
                inputAddress1.setText("");
                inputAddress2.setText("");
                inputPincode.setText("");
                inputPhone.setText("");

                toggleButton();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read user", error.toException());
            }
        });
    }

    private void updateUser(String name, String email, String address1, String address2, String pincode, String phone) {
        // updating the user via child nodes
        if (!TextUtils.isEmpty(name))
            mFirebaseDatabase.child(userId).child("name").setValue(name);

        if (!TextUtils.isEmpty(email))
            mFirebaseDatabase.child(userId).child("email").setValue(email);

        if (!TextUtils.isEmpty(address1))
            mFirebaseDatabase.child(userId).child("address1").setValue(address1);

        if (!TextUtils.isEmpty(address2))
            mFirebaseDatabase.child(userId).child("address2").setValue(address2);

        if (!TextUtils.isEmpty(pincode))
            mFirebaseDatabase.child(userId).child("pincode").setValue(pincode);

        if (!TextUtils.isEmpty(phone))
            mFirebaseDatabase.child(userId).child("phone").setValue(phone);
    }
}
