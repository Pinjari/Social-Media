package com.welserv.samplesocialmediaapp.entity;

public class PostComments {

    public PostComments() {
    }

    public PostComments(String userID, String userName, String userComments, String commentTime) {
        this.userID = userID;
        this.userName = userName;
        this.userComments = userComments;
        this.commentTime = commentTime;
    }

    String userID;
    String userName;
    String userComments;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserComments() {
        return userComments;
    }

    public void setUserComments(String userComments) {
        this.userComments = userComments;
    }

    public String getCommentTime() {
        return commentTime;
    }

    public void setCommentTime(String commentTime) {
        this.commentTime = commentTime;
    }

    String commentTime;

}
