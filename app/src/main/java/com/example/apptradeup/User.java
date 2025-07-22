package com.example.apptradeup;

public class User {
    private String id;
    private String display_name;
    private String email;
    private String role;
    private String status;
    private boolean banned;
    public User() {}

    public User(String id, String display_name, String email, String role, boolean banned,String status) {
        this.id = id;
        this.display_name = display_name;
        this.email = email;
        this.role = role;
        this.banned = banned;
        this.status= status;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDisplay_name() { return display_name; }
    public void setDisplay_name(String display_name) { this.display_name = display_name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
