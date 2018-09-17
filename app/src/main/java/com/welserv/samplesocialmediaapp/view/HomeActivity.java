package com.welserv.samplesocialmediaapp.view;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.twitter.sdk.android.core.TwitterCore;
import com.welserv.samplesocialmediaapp.R;
import com.welserv.samplesocialmediaapp.adapter.DashboardFeedAdapter;
import com.welserv.samplesocialmediaapp.entity.PostComments;
import com.welserv.samplesocialmediaapp.entity.SharedPostUserDetails;
import com.welserv.samplesocialmediaapp.entity.UserDetails;
import com.welserv.samplesocialmediaapp.entity.UserFeedDetails;
import com.welserv.samplesocialmediaapp.utils.AppSharedPref;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;


public class HomeActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 23;
    public static boolean changeData = true;

    private static final String TAG = "homeAct";
    RecyclerView mRecyclerViewFeed;
    private RecyclerView.Adapter mAdapter;
    LinearLayoutManager mLayoutManager;
    AppCompatSeekBar mAppCompatSeekBarPhotoUpload;
    ProgressBar mProgressBarUploadPhoto;
    TextView mTextViewProgressUpdatePhotoUpload, mTextViewUserName;
    LinearLayout mLinearLayoutLoader;
    private Uri filePath;

    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;

    private final int PICK_IMAGE_REQUEST = 71;
    private ImageView attachmentImage;
    private DatabaseReference databaseUsers;
    private ArrayList<UserFeedDetails> userFeedList;
    private DatabaseReference databaseUserFeed;
    private String mPostTitle;
    private DatabaseReference databaseUserFeedChild;
    private Dialog dialog;
    private DashboardFeedAdapter trackListAdapter;
    private Parcelable recyclerViewState;
    private FloatingActionButton fab;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);


        databaseUsers = FirebaseDatabase.getInstance().getReference("users");
        databaseUserFeed = FirebaseDatabase.getInstance().getReference("feed");


        setContentView(R.layout.activity_home);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        getSupportActionBar().setTitle("Demo");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Log.d(TAG, "onCreate: username" + AppSharedPref.getStringPreference(this, AppSharedPref.USER_NAME));
        configFirebase();
//        databaseUsers = FirebaseDatabase.getInstance().getReference("users");

        initUI();

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPostShowDialogue();
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkStoragePermission();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkStoragePermission() {
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG,"Permission is granted");
            //File write logic here
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);

        }
    }
    private void addPostShowDialogue() {
        filePath = null;

        final View promptView = LayoutInflater.from(this).inflate(R.layout.dialog_add_post_layout, null);
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setView(promptView);
        alertBuilder.setCancelable(false);


        attachmentImage = promptView.findViewById(R.id.photo_imageview_dialogue_addpost);

        mTextViewProgressUpdatePhotoUpload = promptView.findViewById(R.id.tv_perc_progress_photo_upload);

        mProgressBarUploadPhoto = promptView.findViewById(R.id.progrebar_upload_photo);
        final EditText editTextPostTitle = promptView.findViewById(R.id.edittext_post_title);

        final TextView attachmentButton = promptView.findViewById(R.id.attachment_new_post);
        attachmentButton.setPaintFlags(attachmentButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        attachmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(editTextPostTitle.getText().toString().trim()))
                    editTextPostTitle.setError(getString(R.string.this_field_is_mednetory));
                else {
                    chooseImage();
                }

            }
        });

        Button cancelButton = (Button) promptView.findViewById(R.id.cancel_button_dialogue);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        Button postButton = (Button) promptView.findViewById(R.id.post_button_dialogue);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mPostTitle = editTextPostTitle.getText().toString();
                if (TextUtils.isEmpty(mPostTitle.trim()))
                    editTextPostTitle.setError(getString(R.string.this_field_is_mednetory));
                else if (filePath == null) {
                    attachmentButton.setError(getString(R.string.please_select_photo));

                    Toast toast= Toast.makeText(getApplicationContext(),
                            getString(R.string.please_select_photo), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER|Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();

                } else
                    uploadImage();

//                dialog.dismiss();
            }
        });


        alertDialog = alertBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();


    }

    private void uploadImage() {

        if (filePath != null) {
//            final ProgressDialog progressDialog = new ProgressDialog(this);
//            progressDialog.setTitle("Uploading...");
//            progressDialog.show();

            final StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Uri downloadUrl = uri;
                                    //Do what you want with the url
                                    Log.d(TAG, "onSuccess: " + downloadUrl);
                                    //     Toast.makeText(HomeActivity.this, "downloadURL: " + downloadUrl, Toast.LENGTH_SHORT).show();

                                    if (downloadUrl.toString().length() > 1)
                                        uploadPostNow(downloadUrl);
                                    else
                                        Toast.makeText(HomeActivity.this, getString(R.string.please_select_photo), Toast.LENGTH_SHORT).show();
                                }
                            });

                            //    progressDialog.dismiss();
                            Log.d(TAG, "onSuccess: uploaded");
//                            Toast.makeText(HomeActivity.this, "Your post", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //   progressDialog.dismiss();
                            Toast.makeText(HomeActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            final double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            //    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                            Log.d(TAG, "onProgress: " + progress);


                            mTextViewProgressUpdatePhotoUpload.setVisibility(View.VISIBLE);


                            mProgressBarUploadPhoto.setProgress((int) progress);
                            mProgressBarUploadPhoto.setVisibility(View.VISIBLE);
                            mTextViewProgressUpdatePhotoUpload.setText(getString(R.string.processing_please_wait) + " " + String.format("%.2f", progress) + "%");


                        }
                    });
        }
    }

    private void uploadPostNow(Uri downloadUrl) {
        changeData = true;
        databaseUserFeedChild = FirebaseDatabase.getInstance().getReference("feed").child(AppSharedPref.getStringPreference(this, AppSharedPref.USER_ID));
        String id = databaseUserFeedChild.push().getKey();


        ArrayList<String> strings = new ArrayList<>();
        ArrayList<PostComments> postComments = new ArrayList<>();

        ArrayList<SharedPostUserDetails> sharedPostUserDetailsList = new ArrayList<>();

        //        strings.add(AppSharedPref.getStringPreference(this, AppSharedPref.USER_ID));
        UserFeedDetails userFeedDetails = new UserFeedDetails(AppSharedPref.getStringPreference(this, AppSharedPref.USER_NAME),
                AppSharedPref.getStringPreference(this, AppSharedPref.USER_ID)
                , mPostTitle, downloadUrl.toString(), strings, postComments, sharedPostUserDetailsList, String.valueOf(System.currentTimeMillis()), id,
                AppSharedPref.getStringPreference(HomeActivity.this, AppSharedPref.USER_TOKEN), id, AppSharedPref.getStringPreference(this, AppSharedPref.USER_ID), AppSharedPref.getStringPreference(HomeActivity.this, AppSharedPref.USER_TOKEN), String.valueOf(System.currentTimeMillis())

        );


        databaseUserFeedChild.child(id).setValue(userFeedDetails);

        if (alertDialog != null && alertDialog.isShowing())
            alertDialog.dismiss();

    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private void initUI() {
        mLinearLayoutLoader = findViewById(R.id.loader_layout);
        mRecyclerViewFeed = findViewById(R.id.feed_recyclerview_acthome);
        mRecyclerViewFeed.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //  fab.show();
                }

                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    // Scroll Down
                    if (fab.isShown()) {
                        fab.hide();
                    }
                } else if (dy < 0) {
                    // Scroll Up
                    if (!fab.isShown()) {
                        fab.show();
                    }
                }
            }
        });
// use a linear layout

        mLayoutManager = new LinearLayoutManager(this);


        RecyclerView.ItemAnimator animator = mRecyclerViewFeed.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        mRecyclerViewFeed.setLayoutManager(mLayoutManager);
        mTextViewUserName = findViewById(R.id.user_name);
        mTextViewUserName.setText("Welcome " + AppSharedPref.getStringPreference(this, AppSharedPref.USER_NAME));

        // specify an adapter (see also next example)
        //  mAdapter = new DashboardFeedAdapter(myDataset);
        //mRecyclerViewFeed.setAdapter(mAdapter);
    }

    private void configFirebase() {
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, HomeActivity.class);
        context.startActivity(starter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:

                if (AppSharedPref.getIntegerPreference(HomeActivity.this, AppSharedPref.LOGIN_STATUS, 0) == 1) {
                    logoutTwitter();
                } else
                    logoutFb();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                attachmentImage.setVisibility(View.VISIBLE);


                attachmentImage.setImageBitmap(getResizedBitmap(bitmap, 800));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void logoutFb() {
        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
                .Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {

//                        SharedPreferences pref = DashBoard.this.getPreferences(Context.MODE_PRIVATE);
//                        SharedPreferences.Editor editor = pref.edit();
//                        editor.clear();
//                        editor.commit();
                LoginManager.getInstance().logOut();
                AppSharedPref.setIntegerPreference(HomeActivity.this, AppSharedPref.LOGIN_STATUS, 0);

                HomeActivity.this.finish();
                LoginActivity.start(HomeActivity.this);

            }
        }).executeAsync();

    }

    private void logoutTwitter() {
        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeSessionCookie();
        TwitterCore.getInstance().getSessionManager().clearActiveSession();

        AppSharedPref.setIntegerPreference(HomeActivity.this, AppSharedPref.LOGIN_STATUS, 0);

        HomeActivity.this.finish();
        LoginActivity.start(HomeActivity.this);
    }

    /**
     * Logout From Facebook
     */


    @Override
    protected void onStart() {
        super.onStart();
        //attaching value event listener
        getAllFeeds();
        getSelfID();

    }

    private void getAllFeeds() {

        databaseUserFeed.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userFeedList = new ArrayList<>();

                userFeedList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: " + postSnapshot.getKey());

                    for (DataSnapshot ds : postSnapshot.getChildren()) {
                        UserFeedDetails track = ds.getValue(UserFeedDetails.class);
                        userFeedList.add(track);
                        String detailCategory = ds.child("postTitle").getValue(String.class);

                        Log.d(TAG, "onDataChange: " + track.getPostTitle()
                                + ds.getChildrenCount());

                    }


//                    UserFeedDetails track = postSnapshot.getValue(UserFeedDetails.class);
//                    userFeedList.add(track);
                }

                Collections.sort(userFeedList, new Comparator<UserFeedDetails>() {
                    @Override
                    public int compare(UserFeedDetails mcOne, UserFeedDetails mcTwo) {
                        return mcOne.getPostTime().compareTo(mcTwo.getPostTime()) * -1;
                    }
                });
                mLinearLayoutLoader.setVisibility(View.GONE);
                recyclerViewState = mRecyclerViewFeed.getLayoutManager().onSaveInstanceState();

                trackListAdapter = new DashboardFeedAdapter(HomeActivity.this, userFeedList);
                mRecyclerViewFeed.setAdapter(trackListAdapter);

                mRecyclerViewFeed.getLayoutManager().onRestoreInstanceState(recyclerViewState);

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


        final Query userQuery = databaseUsers.orderByChild("userEmail");

        userQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                //Get the node from the datasnapshot
                String myParentNode = dataSnapshot.getKey();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    String key = child.getKey().toString();
                    String value = child.getValue().toString();

                    if (value.contains(AppSharedPref.getStringPreference(HomeActivity.this, AppSharedPref.USER_EMAIL))) {
                        Log.d(TAG, "onChildAdded " + key + " " + value + " " + myParentNode);
                        AppSharedPref.setStringPreference(HomeActivity.this, AppSharedPref.USER_ID, myParentNode);
                        break;
                    }
                }

            }


            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }


}
