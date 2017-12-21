package com.prajwal.firebasesos;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by PRAJWAL on 20-09-2017.
 */

public class ChatMessageLog {
    public String uid;
    public String message;
    public String chatName;
    Object timestamp;
    //Map<String, Object> timestamp = new HashMap<>();

    public ChatMessageLog(){

    }

    public ChatMessageLog(String uid, String message, String chatName,Object timestamp) {
        this.uid = uid;
        this.message = message;
        this.chatName = chatName;
        this.timestamp = timestamp;
    }
}