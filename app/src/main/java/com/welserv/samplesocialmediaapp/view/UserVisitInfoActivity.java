package com.welserv.samplesocialmediaapp.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.welserv.samplesocialmediaapp.R;
import com.welserv.samplesocialmediaapp.adapter.DashboardFeedAdapter;
import com.welserv.samplesocialmediaapp.entity.UserFeedDetails;
import com.welserv.samplesocialmediaapp.utils.AppSharedPref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.welserv.samplesocialmediaapp.utils.AppSharedPref.USER_NAME;

public class UserVisitInfoActivity extends AppCompatActivity {

    private static final String TAG = "visitAct";
    TextView mTextViewUserName, mTextViewUserEmail;
    RecyclerView mRecyclerViewUserPostList;
    private DatabaseReference databaseUsers;
    private DatabaseReference databaseUserFeed;
    private Parcelable recyclerViewState;
    private LinearLayout mLinearLayoutLoader;
    private DashboardFeedAdapter feedAdapter;
    private ArrayList<UserFeedDetails> userFeedList;
    private String mUserName, mUserID;
    private LinearLayoutManager mLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseUsers = FirebaseDatabase.getInstance().getReference("users");
        databaseUserFeed = FirebaseDatabase.getInstance().getReference("feed");


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_user_visit_info);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);


        initUI();

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                mUserName = null;
                mUserID = null;
            } else {
                mUserID = extras.getString("post_user_id");
                mUserName = extras.getString("post_user_name");

            }
        } else {
            mUserID = (String) savedInstanceState.getSerializable("post_user_id");
            mUserName = (String) savedInstanceState.getSerializable("post_user_name");
        }


        getSupportActionBar().setTitle(mUserName);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    private void initUI() {

        mRecyclerViewUserPostList = findViewById(R.id.profile_visit_recyclerview);

        mTextViewUserEmail = findViewById(R.id.tv_user_email_profile_visit_info);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerViewUserPostList.setLayoutManager(mLayoutManager);


    }


    @Override
    protected void onStart() {
        super.onStart();
        //attaching value event listener
        getAllFeeds();
        getSelfID();

    }

    private void getAllFeeds() {
        databaseUserFeed = FirebaseDatabase.getInstance().getReference("feed").child(mUserID);
        Query query = databaseUserFeed.orderByChild("postTime");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userFeedList = new ArrayList<>();

                userFeedList.clear();

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    UserFeedDetails track = ds.getValue(UserFeedDetails.class);
                    userFeedList.add(track);
                    String detailCategory = ds.child("postTitle").getValue(String.class);

                    Log.d(TAG, "onDataChange: " + track.getPostTitle()
                            + ds.getChildrenCount());


//                    UserFeedDetails track = postSnapshot.getValue(UserFeedDetails.class);
//                    userFeedList.add(track);
                }


//                mLinearLayoutLoader.setVisibility(View.GONE);
                Collections.sort(userFeedList, new Comparator<UserFeedDetails>() {
                    @Override
                    public int compare(UserFeedDetails mcOne, UserFeedDetails mcTwo) {
                        return mcOne.getPostTime().compareTo(mcTwo.getPostTime()) * -1;
                    }
                });

                recyclerViewState = mRecyclerViewUserPostList.getLayoutManager().onSaveInstanceState();
                feedAdapter = new DashboardFeedAdapter(UserVisitInfoActivity.this, userFeedList);
                mRecyclerViewUserPostList.setAdapter(feedAdapter);
                mRecyclerViewUserPostList.getLayoutManager().onRestoreInstanceState(recyclerViewState);

//                if (changeData) {
//                    recyclerViewState = mRecyclerViewFeed.getLayoutManager().onSaveInstanceState();
//
//                    trackListAdapter = new DashboardFeedAdapter(HomeActivity.this, userFeedList);
//                    mRecyclerViewFeed.setAdapter(trackListAdapter);
//
//                    mRecyclerViewFeed.getLayoutManager().onRestoreInstanceState(recyclerViewState);
//
//                } else {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            trackListAdapter.notifyDataSetChanged();
//                        }
//                    });
//                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getSelfID() {

        final Query userQuery = databaseUsers.child(mUserID).child("userEmail");

        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String userEmail = dataSnapshot.getValue(String.class);

                mTextViewUserEmail.setText(userEmail + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
