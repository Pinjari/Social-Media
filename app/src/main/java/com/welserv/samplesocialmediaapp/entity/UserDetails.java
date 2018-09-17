package com.welserv.samplesocialmediaapp.entity;

public class UserDetails {


    public UserDetails(String userName, String userEmail, String userID, String userToken) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userID = userID;
        this.userToken = userToken;
    }

    public UserDetails() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    String userName;
    String userEmail;
    String userID;
    String userToken;


}
