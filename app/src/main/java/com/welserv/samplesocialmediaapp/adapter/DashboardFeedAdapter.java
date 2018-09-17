package com.welserv.samplesocialmediaapp.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.twitter.sdk.android.core.models.Card;
import com.twitter.sdk.android.core.models.Image;
import com.welserv.samplesocialmediaapp.R;
import com.welserv.samplesocialmediaapp.entity.PostComments;
import com.welserv.samplesocialmediaapp.entity.SharedPostUserDetails;
import com.welserv.samplesocialmediaapp.entity.UserDetails;
import com.welserv.samplesocialmediaapp.entity.UserFeedDetails;
import com.welserv.samplesocialmediaapp.helper.PushNotifictionHelper;
import com.welserv.samplesocialmediaapp.utils.AppSharedPref;
import com.welserv.samplesocialmediaapp.utils.CommonFun;
import com.welserv.samplesocialmediaapp.view.HomeActivity;
import com.welserv.samplesocialmediaapp.view.UserVisitInfoActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class DashboardFeedAdapter extends RecyclerView.Adapter<DashboardFeedAdapter.VoewHolder> {

    private static final String TAG = "mActAdapter";
    private final Context mContext;
    private final ArrayList<UserFeedDetails> feedList;
    private final DatabaseReference databaseUserFeed;
    private DatabaseReference databaseUsers;
    private Dialog dialog;
    private AlertDialog alertDialog;
    private String currentUserID;
    private RecyclerView mRecyclerViewComments;
    private ArrayList<PostComments> postCommentsList;
    private DatabaseReference databaseUserFeedChild;
    private LinearLayoutManager mLayoutManager;
    private SharedByUsersAdapter mAdapter;


    public DashboardFeedAdapter(Context context, ArrayList<UserFeedDetails> userFeedList) {
        feedList = userFeedList;
        mContext = context;
        databaseUserFeed = FirebaseDatabase.getInstance().getReference("feed");

    }

    @NonNull
    @Override
    public VoewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        CardView linearLayout = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_feed_row_layout, parent, false);
        return new VoewHolder(linearLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull final VoewHolder holder, int position) {

        currentUserID = AppSharedPref.getStringPreference(mContext, AppSharedPref.USER_ID);
        final UserFeedDetails postDetails = feedList.get(holder.getAdapterPosition());


        if (postDetails.getSharedBy() != null && postDetails.getSharedBy().size() > 0) {
            showSharedUsers(postDetails, holder);
        }

        holder.userName.setText(postDetails.getUserName());
        holder.userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callVisitActivity(postDetails);
            }
        });
        holder.postDate.setText(CommonFun.getDate(Long.parseLong(postDetails.getOriginalShareTime()), "hh:mm aa  dd/MMM/yyyy"));
        holder.postTitle.setText(postDetails.getPostTitle());


        String url = postDetails.getPostImage();


        Glide.with(mContext)
                .load(url)
                .apply(new RequestOptions()
                        .error(R.drawable.ic_broken_image_blue_grey_600_48dp)
                        .placeholder(R.drawable.ic_cloud_download_indigo_300_48dp))
                .into(holder.imageView);

        holder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                holder.imageView.setDrawingCacheEnabled(true);

                Bitmap bitmap = holder.imageView.getDrawingCache();

                File root = Environment.getExternalStorageDirectory();
                File cachePath = new File(root.getAbsolutePath() + "/DCIM/Camera/image.jpg");

                try {
                    cachePath.createNewFile();
                    FileOutputStream ostream = new FileOutputStream(cachePath);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                    ostream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("image/*");
                share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(cachePath));
                mContext.startActivity(Intent.createChooser(share, "Share via"));


            }
        });


        ArrayList<String> likes = new ArrayList<>();

        if (postDetails.getPostLikes() != null)
            likes = postDetails.getPostLikes();

        int likesCount = 0;
        likesCount = likes.size();
        Log.d(TAG, "onBindViewHolder: " + likesCount);
        holder.likeCount.setText(likesCount + "");
        holder.likeCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // showLikes(postDetails);
            }
        });

        holder.commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                addCommentPostShowDialogue(postDetails, holder.getAdapterPosition());
            }
        });

        holder.forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forwardPostNow(postDetails);
            }
        });


        if (likes.contains(currentUserID))
            holder.likeButton.setImageResource(R.drawable.ic_thumb_up_blue_a200_24dp);
        else
            holder.likeButton.setImageResource(R.drawable.ic_thumb_up_gray_a200_24dp);

        final int finalLikesCount = likesCount;
        final ArrayList<String> finalLikes = likes;
        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (finalLikes.size() > 0 && finalLikes.contains(currentUserID)) {
                    holder.likeButton.setImageResource(R.drawable.ic_thumb_up_gray_a200_24dp);
                    holder.likeCount.setText((finalLikesCount - 1) + "");
                    finalLikes.remove(currentUserID);
                    postDetails.setPostLikes(finalLikes);

                    Log.d(TAG, "onClick: 1 " + finalLikes.size());

                } else {

                    Log.d(TAG, "onClick: 2");
                    holder.likeButton.setImageResource(R.drawable.ic_thumb_up_blue_a200_24dp);
                    finalLikes.add(currentUserID);
                    postDetails.setPostLikes(finalLikes);
                    holder.likeCount.setText((finalLikesCount + 1) + "");


                    PushNotifictionHelper.sendPushNotification(postDetails.getSharedPostUserToken(), mContext.getString(R.string.post_liked), AppSharedPref.getStringPreference(mContext, AppSharedPref.USER_NAME) + " " + mContext.getString(R.string.liked_your_post), postDetails.getSharedPostID(), postDetails.getSharedPostUserID());
                    if (!postDetails.getSharedPostUserToken().contains(postDetails.getUserToken()))
                        PushNotifictionHelper.sendPushNotification(postDetails.getUserToken(), mContext.getString(R.string.post_liked), AppSharedPref.getStringPreference(mContext, AppSharedPref.USER_NAME) + " " + mContext.getString(R.string.liked_your_post), postDetails.getPostID(), postDetails.getUserID());

                }

                Log.d(TAG, "onClick: 1 likes.size()" + finalLikes.size());
                HomeActivity.changeData = false;
                feedList.get(holder.getAdapterPosition()).setPostLikes(finalLikes);
                databaseUserFeed.child(postDetails.getSharedPostUserID()).child(postDetails.getSharedPostID()).child("postLikes").setValue(finalLikes);


                notifyItemChanged(holder.getAdapterPosition());

            }
        });

        int postCommentsListSize = 0;
        if (postDetails.getPostComments() != null)
            postCommentsListSize = postDetails.getPostComments().size();
//        holder.commentButton.setText(postDetails.getUserName());
        holder.commentsCount.setText(postCommentsListSize + "");


    }

    private void showSharedUsers(UserFeedDetails postDetails, VoewHolder holder) {
        holder.mLinearLayoutSharedBy.setVisibility(View.VISIBLE);
        mLayoutManager = new LinearLayoutManager(mContext);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        holder.recyclerViewSharedBy.setLayoutManager(mLayoutManager);
        mAdapter = new SharedByUsersAdapter(postDetails.getSharedBy(), mContext);
        holder.recyclerViewSharedBy.setAdapter(mAdapter);

    }

    private void forwardPostNow(UserFeedDetails postDetails) {
        databaseUserFeedChild = FirebaseDatabase.getInstance().getReference("feed").child(AppSharedPref.getStringPreference(mContext, AppSharedPref.USER_ID));
        String id = databaseUserFeedChild.push().getKey();
        SharedPostUserDetails sharedPostUserDetailsNew = new SharedPostUserDetails();
        SharedPostUserDetails tempPost = null;
        ArrayList<SharedPostUserDetails> sharedPostUserDetailsList = new ArrayList<>();


        if (postDetails.getSharedBy() != null)

        {
            sharedPostUserDetailsList = postDetails.getSharedBy();
        }


        SharedPostUserDetails sharedPostUserDetails = new SharedPostUserDetails();
        SharedPostUserDetails userDetails = new SharedPostUserDetails(
                AppSharedPref.getStringPreference(mContext, AppSharedPref.USER_NAME),

                AppSharedPref.getStringPreference(mContext, AppSharedPref.USER_EMAIL),
                AppSharedPref.getStringPreference(mContext, AppSharedPref.USER_ID),
                AppSharedPref.getStringPreference(mContext, AppSharedPref.USER_TOKEN),
                sharedPostUserDetails, String.valueOf(System.currentTimeMillis()), id);
        sharedPostUserDetailsList.add(userDetails);


        sharedPostUserDetailsNew.setSharedPostUserDetails(userDetails);

        Log.d(TAG, "forwardPostNow: check " + sharedPostUserDetailsNew.getSharedPostUserDetails().getSharedPostUserDetails().getUserName());
//        if (tempPost.getSharedPostUserDetails() == null)

        ArrayList<String> strings = new ArrayList<>();
        ArrayList<PostComments> postComments = new ArrayList<>();

//        strings.add(AppSharedPref.getStringPreference(this, AppSharedPref.USER_ID));
        UserFeedDetails userFeedDetails = new UserFeedDetails(postDetails.getUserName(),
                postDetails.getUserID(), postDetails.getPostTitle(), postDetails.getPostImage(), strings, postComments, sharedPostUserDetailsList, String.valueOf(System.currentTimeMillis()), postDetails.getPostID(),
                postDetails.getUserToken(), id, AppSharedPref.getStringPreference(mContext, AppSharedPref.USER_ID), AppSharedPref.getStringPreference(mContext, AppSharedPref.USER_TOKEN), postDetails.getOriginalShareTime());


        databaseUserFeedChild.child(id).setValue(userFeedDetails);

        PushNotifictionHelper.sendPushNotification(postDetails.getSharedPostUserToken(), mContext.getString(R.string.post_shared), AppSharedPref.getStringPreference(mContext, AppSharedPref.USER_NAME) + " " + mContext.getString(R.string.shared_your_post), postDetails.getSharedPostID(), postDetails.getSharedPostUserID());
        if (!postDetails.getSharedPostUserToken().contains(postDetails.getUserToken()))
            PushNotifictionHelper.sendPushNotification(postDetails.getUserToken(), mContext.getString(R.string.post_shared), AppSharedPref.getStringPreference(mContext, AppSharedPref.USER_NAME) + " " + mContext.getString(R.string.shared_your_post), postDetails.getPostID(), postDetails.getUserID());


    }

    private void callVisitActivity(UserFeedDetails postDetails) {
        mContext.startActivity(new Intent(mContext, UserVisitInfoActivity.class)
                .putExtra("post_user_id", postDetails.getUserID())
                .putExtra("post_user_name", postDetails.getUserName().toString()));
    }

    private void showLikes(UserFeedDetails postDetails) {
        Log.d(TAG, "showLikes: user ID: " + postDetails.getPostLikes().get(0));
        databaseUsers = FirebaseDatabase.getInstance().getReference("users").child(postDetails.getPostLikes().get(0)).child("userName");
        databaseUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String userDetails = dataSnapshot.getValue(String.class);
                Log.d(TAG, "onDataChange: " + userDetails);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });


    }


    @Override
    public int getItemCount() {
        return feedList.size();
    }


    public class VoewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        ImageButton shareButton, likeButton, forwardButton;
        TextView userName, postDate, postTitle, likeCount, commentButton, commentsCount;
        LinearLayout mLinearLayoutSharedBy;
        RecyclerView recyclerViewSharedBy;

        public VoewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name_row_feed);
            postDate = itemView.findViewById(R.id.post_date_row_feed);
            postTitle = itemView.findViewById(R.id.post_details_textview_row_feed);
            likeButton = itemView.findViewById(R.id.like_button_row_feed);
            likeCount = itemView.findViewById(R.id.likes_count_row_feed);
            commentButton = itemView.findViewById(R.id.comment_button_row_feed);
            commentsCount = itemView.findViewById(R.id.comments_count_row_feed);
            imageView = itemView.findViewById(R.id.post_photo_imageview_row_feed);
            shareButton = itemView.findViewById(R.id.share_post_row_feed);
            forwardButton = itemView.findViewById(R.id.forward_post_row_feed);
            mLinearLayoutSharedBy = itemView.findViewById(R.id.layout_shared_by);
            recyclerViewSharedBy = itemView.findViewById(R.id.shared_users_recyclerview);


        }
    }


    private void addCommentPostShowDialogue(final UserFeedDetails postDetails, final int adapterPosition) {


        final View promptView = LayoutInflater.from(mContext).inflate(R.layout.dialogue_post_comment, null);
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext);
        alertBuilder.setView(promptView);
        alertBuilder.setCancelable(false);


        ImageButton buttonComment = promptView.findViewById(R.id.send_comment_dial);
        mRecyclerViewComments = promptView.findViewById(R.id.comments_recycler_post_dial);


        RecyclerView.LayoutManager mLayoutManager;
        mRecyclerViewComments.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerViewComments.setLayoutManager(mLayoutManager);


        fetchDisplayComments(postDetails);
        final EditText editTextPostComment = promptView.findViewById(R.id.edittext_post_comment);
        alertDialog = alertBuilder.create();
        alertDialog.setCancelable(false);

        buttonComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String commentText = editTextPostComment.getText().toString();
                if (TextUtils.isEmpty(commentText))
                    editTextPostComment.setError("" + mContext.getString(R.string.put_your_comment_here));
                else updloadComment(postDetails, commentText, adapterPosition);

            }
        });

        Button cancelButton = (Button) promptView.findViewById(R.id.cancel_button_dialogue);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();


    }

    private void fetchDisplayComments(UserFeedDetails postDetails) {


//        recyclerViewState = mRecyclerViewFeed.getLayoutManager().onSaveInstanceState();

        if (postDetails.getPostComments() != null && postDetails.getPostComments().size() > 0) {
            PostCommentsAdapter dashboardFeedAdapter = new PostCommentsAdapter(mContext, postDetails.getPostComments());
            mRecyclerViewComments.setAdapter(dashboardFeedAdapter);
        }
//        mRecyclerViewFeed.getLayoutManager().onRestoreInstanceState(recyclerViewState);

    }

    private void updloadComment(UserFeedDetails postDetails, String commentText,
                                int adapterPosition) {

        ArrayList<PostComments> commentsList = new ArrayList<>();
        if (postDetails.getPostComments() != null)
            commentsList = postDetails.getPostComments();
        String id = postDetails.getUserID();
        PostComments track = new PostComments(currentUserID, AppSharedPref.getStringPreference(mContext, AppSharedPref.USER_NAME), commentText, String.valueOf(System.currentTimeMillis()));

        commentsList.add(track);
        databaseUserFeed.child(postDetails.getSharedPostUserID()).child(postDetails.getSharedPostID()).child("postComments").setValue(commentsList);

        // feedList.get(adapterPosition).setPostComments();
        alertDialog.dismiss();


        PushNotifictionHelper.sendPushNotification(postDetails.getSharedPostUserToken(), mContext.getString(R.string.new_comment), AppSharedPref.getStringPreference(mContext, AppSharedPref.USER_NAME) + " " + mContext.getString(R.string.commented_onyour_post), postDetails.getSharedPostID(), postDetails.getSharedPostUserID());
        if (!postDetails.getSharedPostUserToken().contains(postDetails.getUserToken()))
            PushNotifictionHelper.sendPushNotification(postDetails.getUserToken(), mContext.getString(R.string.new_comment), AppSharedPref.getStringPreference(mContext, AppSharedPref.USER_NAME) + " " + mContext.getString(R.string.commented_onyour_post), postDetails.getPostID(), postDetails.getUserID());


    }


}

