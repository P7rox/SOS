package com.prajwal.firebasesos;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class Chat extends AppCompatActivity {
    private static final String TAG1 = Chat.class.getSimpleName();
    LinearLayout layout;
    RelativeLayout layout_2;
    ImageView sendButton;
    EditText messageArea;
    ScrollView scrollView;
    private DatabaseReference mFirebaseDatabase;
    private DatabaseReference uFirebaseDatabase;
    private DatabaseReference lFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private String chatName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        setTitle("Global Chat");

        layout = (LinearLayout) findViewById(R.id.layout1);
        layout_2 = (RelativeLayout)findViewById(R.id.layout2);
        sendButton = (ImageView)findViewById(R.id.sendButton);
        messageArea = (EditText)findViewById(R.id.messageArea);
        scrollView = (ScrollView)findViewById(R.id.scrollView);

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
                    startActivity(new Intent(Chat.this, LoginActivity.class));
                    finish();
                }
            }
        };

        mFirebaseInstance = FirebaseDatabase.getInstance();

        uFirebaseDatabase = mFirebaseInstance.getReference("users");
        // get reference to 'message' node
        mFirebaseDatabase = mFirebaseInstance.getReference("messages");

        lFirebaseDatabase = mFirebaseInstance.getReference("logMessages");

        chatName = "New User";
        uFirebaseDatabase.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                chatName = dataSnapshot.child("name").getValue(String.class);
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mFirebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                if (chatMessage == null) {
                    Log.e(TAG1, "Chat data is null!");
                    return;
                }
                String message = chatMessage.message;
                String uid = chatMessage.uid;
                String ChatName = chatMessage.chatName;

                if(uid.equals(user.getUid())){
                    addMessageBox("You: \n" + message, 1);
                }
                else{
                    addMessageBox(ChatName + ": \n" + message, 2);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageArea.getText().toString();

                if(!messageText.equals("")){
                    ChatMessage message = new ChatMessage(user.getUid(),messageText,chatName);


                    ChatMessageLog logMessage = new ChatMessageLog(user.getUid(),messageText,chatName, ServerValue.TIMESTAMP);

                    String childID = lFirebaseDatabase.push().getKey();
                    lFirebaseDatabase.child(childID).setValue(logMessage);
                    mFirebaseDatabase.setValue(message);
                    messageArea.setText("");
                }
            }
        });

    }

    public void addMessageBox(String message, int type){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm a ddMMMyyyy");
        String currentDateandTime = sdf.format(new Date());
        TextView textView = new TextView(Chat.this);
        textView.setText(message + "\n" + currentDateandTime);

        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.weight = 1.0f;

        if(type == 1) {
            lp2.gravity = Gravity.LEFT;
            textView.setBackgroundResource(R.drawable.bubble_in);
        }
        else{
            lp2.gravity = Gravity.RIGHT;
            textView.setBackgroundResource(R.drawable.bubble_out);
        }
        textView.setLayoutParams(lp2);
        layout.addView(textView);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }
}
