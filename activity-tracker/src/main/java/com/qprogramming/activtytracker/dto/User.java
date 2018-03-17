package com.qprogramming.activtytracker.dto;

public class User {
    private String apiKey;
    private String secret;
    private String role;

    public User() {
    }

    public User(String apiKey, String secret) {
        this.apiKey = apiKey;
        this.secret = secret;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        if (!apiKey.equals(user.apiKey)) {
            return false;
        }
        return secret.equals(user.secret);
    }

    @Override
    public int hashCode() {
        int result = apiKey.hashCode();
        result = 31 * result + secret.hashCode();
        return result;
    }
}
