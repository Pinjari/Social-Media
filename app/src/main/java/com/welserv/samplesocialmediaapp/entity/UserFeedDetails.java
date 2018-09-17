package com.welserv.samplesocialmediaapp.entity;

import java.util.ArrayList;

public class UserFeedDetails {


    public UserFeedDetails(String userName, String userID, String postTitle, String postImage, ArrayList<String> postLikes, ArrayList<PostComments> postComments, ArrayList<SharedPostUserDetails> sharedBy, String postTime, String postID, String userToken, String sharedPostID, String sharedPostUserID, String sharedPostUserToken, String originalShareTime) {
        this.userName = userName;
        this.userID = userID;
        this.postTitle = postTitle;
        this.postImage = postImage;
        this.postLikes = postLikes;
        this.postComments = postComments;
        this.sharedBy = sharedBy;
        this.postTime = postTime;
        this.postID = postID;
        this.userToken = userToken;
        this.sharedPostID = sharedPostID;
        this.sharedPostUserID = sharedPostUserID;
        this.sharedPostUserToken = sharedPostUserToken;
        this.originalShareTime = originalShareTime;
    }

    String userName;
    String userID;
    String postTitle;
    String postImage;
    ArrayList<String> postLikes;
    ArrayList<PostComments> postComments;
    ArrayList<SharedPostUserDetails> sharedBy;
    String postTime;
    String postID;
    String userToken;
    String sharedPostID;
    String sharedPostUserID;

    public String getSharedPostUserToken() {
        return sharedPostUserToken;
    }

    public void setSharedPostUserToken(String sharedPostUserToken) {
        this.sharedPostUserToken = sharedPostUserToken;
    }

    String sharedPostUserToken;


    public String getSharedPostID() {
        return sharedPostID;
    }

    public void setSharedPostID(String sharedPostID) {
        this.sharedPostID = sharedPostID;
    }

    public String getSharedPostUserID() {
        return sharedPostUserID;
    }

    public void setSharedPostUserID(String sharedPostUserID) {
        this.sharedPostUserID = sharedPostUserID;
    }


    String originalShareTime;

    public String getOriginalShareTime() {
        return originalShareTime;
    }

    public void setOriginalShareTime(String originalShareTime) {
        this.originalShareTime = originalShareTime;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public ArrayList<String> getPostLikes() {
        return postLikes;
    }

    public void setPostLikes(ArrayList<String> postLikes) {
        this.postLikes = postLikes;
    }

    public ArrayList<PostComments> getPostComments() {
        return postComments;
    }

    public void setPostComments(ArrayList<PostComments> postComments) {
        this.postComments = postComments;
    }

    public ArrayList<SharedPostUserDetails> getSharedBy() {
        return sharedBy;
    }

    public void setSharedBy(ArrayList<SharedPostUserDetails> sharedBy) {
        this.sharedBy = sharedBy;
    }

    public String getPostTime() {
        return postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public UserFeedDetails() {
    }


}
