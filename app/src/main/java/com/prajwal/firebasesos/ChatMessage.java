package com.prajwal.firebasesos;

/**
 * Created by PRAJWAL on 20-09-2017.
 */

public class ChatMessage {
    public String uid;
    public String message;
    public String chatName;

    public ChatMessage(){

    }

    public ChatMessage(String uid, String message, String chatName) {
        this.uid = uid;
        this.message = message;
        this.chatName = chatName;
    }
}
