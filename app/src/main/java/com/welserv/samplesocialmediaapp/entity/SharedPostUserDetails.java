package com.welserv.samplesocialmediaapp.entity;

public class SharedPostUserDetails {


    public SharedPostUserDetails() {
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

    public SharedPostUserDetails getSharedPostUserDetails() {
        return sharedPostUserDetails;
    }

    public void setSharedPostUserDetails(SharedPostUserDetails sharedPostUserDetails) {
        this.sharedPostUserDetails = sharedPostUserDetails;
    }

    public String getSharedTime() {
        return sharedTime;
    }

    public void setSharedTime(String sharedTime) {
        this.sharedTime = sharedTime;
    }

    String userName;
    String userEmail;
    String userID;
    String userToken;
    SharedPostUserDetails sharedPostUserDetails;
    String sharedTime;

    public SharedPostUserDetails(String userName, String userEmail, String userID, String userToken, SharedPostUserDetails sharedPostUserDetails, String sharedTime, String sharedPostID) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userID = userID;
        this.userToken = userToken;
        this.sharedPostUserDetails = sharedPostUserDetails;
        this.sharedTime = sharedTime;
        this.sharedPostID = sharedPostID;
    }

    public String getSharedPostID() {
        return sharedPostID;
    }

    public void setSharedPostID(String sharedPostID) {
        this.sharedPostID = sharedPostID;
    }

    String sharedPostID;

}
