package com.example.apptradeup;

import java.util.List;

public class Chat {
    private String id;
    private List<String> members;
    private String lastMessage;
    private long lastTimestamp;

    public Chat() {} // Bắt buộc phải có constructor rỗng cho Firestore

    // Getter, Setter
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public List<String> getMembers() {
        return members;
    }
    public void setMembers(List<String> members) {
        this.members = members;
    }

    public String getLastMessage() {
        return lastMessage;
    }
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastTimestamp() {
        return lastTimestamp;
    }
    public void setLastTimestamp(long lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }

    // Format thời gian ra "hh:mm"
    public String getTimeString() {
        if (lastTimestamp == 0) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
        return sdf.format(new java.util.Date(lastTimestamp));
    }
}
