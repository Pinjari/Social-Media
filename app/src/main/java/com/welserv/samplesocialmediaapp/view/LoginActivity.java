package com.welserv.samplesocialmediaapp.view;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.models.User;
import com.twitter.sdk.android.core.services.AccountService;
import com.welserv.samplesocialmediaapp.R;
import com.welserv.samplesocialmediaapp.entity.UserDetails;
import com.welserv.samplesocialmediaapp.utils.AppSharedPref;
import com.welserv.samplesocialmediaapp.utils.CommonFun;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import retrofit2.Call;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = LoginActivity.class.getName();
    private TextView mLoginButtonTwitter;
    private TwitterAuthClient mTwitterAuthClient;
    private TextView mLoginButtonFb;
    private CallbackManager callbackManager;
    private DatabaseReference databaseUsers;
    String mUserName;
    private DatabaseReference postRef;
    private String mUserEmail, userName;

    boolean fb = false;

    int updateUser = 0;

    public static void start(Context context) {
        Intent starter = new Intent(context, LoginActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //    Twitter.initialize(this);

        databaseUsers = FirebaseDatabase.getInstance().getReference("users");

        initTwitterLogin();
        initFB();

        setContentView(R.layout.activity_login);

        initUI();


        getSupportActionBar().hide();

        if (AppSharedPref.getIntegerPreference(this, AppSharedPref.LOGIN_STATUS, 0) != 0) {
            Log.d(TAG, "onCreate: " + AppSharedPref.getIntegerPreference(this, AppSharedPref.LOGIN_STATUS, 0));
            HomeActivity.start(this);
            this.finish();
        }

    }

    private void initFB() {
        callbackManager = CallbackManager.Factory.create();
    }

    private void initUI() {

        mLoginButtonTwitter = findViewById(R.id.login_button_twitter);
        mLoginButtonFb = findViewById(R.id.login_button_fb);


        mLoginButtonTwitter.setOnClickListener(this);
        mLoginButtonFb.setOnClickListener(this);

    }

    private void initTwitterLogin() {
        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig("nNlQ9h4hs2sgoIHY2qAjR7QC2", "HfjJWtdztOg7MQMObDnX6L7ZMyiZEML5Foc1cvbukbG7RWQbBb"))
                .debug(true)
                .build();
        Twitter.initialize(config);

        mTwitterAuthClient = new TwitterAuthClient();

    }


    @Override

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the login button.
        if (fb)
            callbackManager.onActivityResult(requestCode, resultCode, data);
        else
            mTwitterAuthClient.onActivityResult(requestCode, resultCode, data);


    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.login_button_twitter:
                fb = false;

                if (CommonFun.haveNetworkConnection(this))
                    loginWithTwitter();
                else
                    Toast.makeText(this, getString(R.string.check_your_internet_connection_try_again), Toast.LENGTH_LONG).show();

                break;

            case R.id.login_button_fb:


                fb = true;
                if (CommonFun.haveNetworkConnection(this))
                    loginFromFacebook();
                else
                    Toast.makeText(this, getString(R.string.check_your_internet_connection_try_again), Toast.LENGTH_LONG).show();


                break;
        }

    }

    private void loginFromFacebook() {

        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("user_photos", "email", "public_profile"));
//      LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("publish_actions"));
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {
                                        Log.v("LoginActivity", response.toString());

                                        // Application code
                                        try {

                                            String name = object.getString("name");
                                            String email = object.getString("email"); // 01/31/1980 format

                                            AppSharedPref.setIntegerPreference(LoginActivity.this, AppSharedPref.LOGIN_STATUS, 2);

                                            updateDatabase(name, email);


                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email,gender,birthday");
                        request.setParameters(parameters);
                        request.executeAsync();

                        // App code
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });
    }

    private void updateDatabase(final String name, final String email) {

        Log.d(TAG, "updateDatabase: email" + email);
        AppSharedPref.setStringPreference(this, AppSharedPref.USER_EMAIL, email);
        AppSharedPref.setStringPreference(this, AppSharedPref.USER_NAME, name);

        databaseUsers.orderByChild("userEmail").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {


                            Log.d(TAG, "onDataChange: " + dataSnapshot);
                            Toast.makeText(LoginActivity.this, "Welcome " + mUserName, Toast.LENGTH_SHORT).show();

                            if (AppSharedPref.getIntegerPreference(LoginActivity.this, AppSharedPref.LOGIN_STATUS, 0) != 0) {
                                HomeActivity.start(LoginActivity.this);
                                LoginActivity.this.finish();
                            }


                        } else {

                            mUserName = name;
                            String id = databaseUsers.push().getKey();


                            //creating an Artist Object
                            UserDetails userDetails = new UserDetails(name, email, id, AppSharedPref.getStringPreference(LoginActivity.this, AppSharedPref.USER_TOKEN));

                            //Saving the Artist
                            databaseUsers.child(id).setValue(userDetails);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


    }

    private void loginWithTwitter() {
        mTwitterAuthClient.authorize(this, new com.twitter.sdk.android.core.Callback<TwitterSession>() {


            @Override
            public void success(Result<TwitterSession> result) {
                // Success

                Log.d(TAG, "success: " +
                        "getId: " + result.data.getId() +
                        "--getUserName: " + result.data.getUserName());
                final TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
                TwitterAuthToken authToken = session.getAuthToken();

                String token = authToken.token;
                String secret = authToken.secret;

                mTwitterAuthClient.requestEmail(session, new Callback<String>() {
                    @Override
                    public void success(Result<String> resultD) {
                        // Do something with the result, which provides the email address

                        mUserEmail = resultD.data;
                        Log.d(TAG, "success: email id here.. " + resultD.data);

                        updateUser++;
                        if (updateUser > 1)
                            updateDatabase(userName, mUserEmail);

                    }

                    @Override
                    public void failure(TwitterException exception) {
                        // Do something on failure
                        Log.d(TAG, "failure: " + exception);
                        Toast.makeText(LoginActivity.this, "" + getString(R.string.something_went_wrong_try_again), Toast.LENGTH_SHORT).show();
                    }
                });


                TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
                AccountService accountService = twitterApiClient.getAccountService();
                Call<User> call = accountService.verifyCredentials(true, true, true);
                call.enqueue(new Callback<com.twitter.sdk.android.core.models.User>() {
                    @Override
                    public void success(Result<com.twitter.sdk.android.core.models.User> result) {
                        //here we go User details
                        Log.d(TAG, "success: " + result.data.name);
                        userName = result.data.name;
                        AppSharedPref.setIntegerPreference(LoginActivity.this, AppSharedPref.LOGIN_STATUS, 1);
                        updateUser++;
                        if (updateUser > 1)
                            updateDatabase(userName, mUserEmail);

                    }

                    @Override
                    public void failure(TwitterException exception) {
                    }
                });


                // Do something with result, which provides a TwitterSession for making API calls
            }

            @Override
            public void failure(TwitterException e) {
                e.printStackTrace();

                Toast.makeText(LoginActivity.this, getString(R.string.something_went_wrong_try_again), Toast.LENGTH_SHORT).show();

            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        //attaching value event listener
        databaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                if (AppSharedPref.getIntegerPreference(LoginActivity.this, AppSharedPref.LOGIN_STATUS, 0) != 0) {
                    HomeActivity.start(LoginActivity.this);
                    LoginActivity.this.finish();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
