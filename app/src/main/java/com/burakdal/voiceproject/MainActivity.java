package com.burakdal.voiceproject;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.burakdal.voiceproject.account.SignIn;
import com.burakdal.voiceproject.models.FollowObject;
import com.burakdal.voiceproject.models.FragmentTag;
import com.burakdal.voiceproject.models.NotificationToken;
import com.burakdal.voiceproject.models.Post;
import com.burakdal.voiceproject.models.User;
import com.burakdal.voiceproject.utils.SectionPagerAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity implements IMainActivity {
    private final static String TAG = "MainFragment";

    //constants
    private static final int HOME = 0;
    public static final String CHANNEL_1="channel1";
    public static final String CHANNEL_2="channel2";
    private User mUser;


    private static final int SEARCH = 1;
    private static final int NOTIFICATIONS = 3;
    private static final int MYPROFILE = 4;
    private FirebaseAuth.AuthStateListener mAuthStateListener;


    private FirebaseFirestore mDb;
    private boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;


    public SectionPagerAdapter mPagerAdapter;
    private BottomNavigationView mBottomNavigationView;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {

                case R.id.navigation_search: {

//                    inflateSearchFragment();
                    if (mSearchTotalFragment == null) {
                        mSearchTotalFragment = new SearchTotalFragment();
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.add(R.id.main_container, mSearchTotalFragment, getString(R.string.tag_searchtotal_fragment));
                        transaction.commit();
                        mFragmentTags.add(getString(R.string.tag_searchtotal_fragment));
                        mFragments.add(new FragmentTag(mSearchTotalFragment, getString(R.string.tag_searchtotal_fragment)));

                    } else {
                        mFragmentTags.remove(getString(R.string.tag_searchtotal_fragment));
                        mFragmentTags.add(getString(R.string.tag_searchtotal_fragment));
                    }

                    setFragmentVisibilities(getString(R.string.tag_searchtotal_fragment));
                    break;
                }
                case R.id.navigation_notifications: {
                    inflateNotificationFragment();


                    break;
                }

                case R.id.navigation_myprofile: {
                    inflateMyProfileFragment();


                    break;
                }

                case R.id.navigation_post: {
                    inflatePostFragment();
                    break;
                }


                case R.id.navigation_home: {
                    Log.d(TAG,"home clicked");
                    mFragmentTags.clear();
                    mFragmentTags = new ArrayList<>();
                    inflateHomeFragment();
                    break;
                }


            }
            return false;
        }
    };
    //fragments
    private EditProfileFragment mEditProfileFragment;
    private Home mHome;
    private MyProfile mMyProfile;
    private Notifications mNotifications;
    private SearchFragment mSearchFragment;
    private ViewPostFragment mViewPostFragment;
    private ViewProfileFragment mViewProfileFragment;
    private PostFragment mPostFragment;
    private SelectLocationFragment mSelectLocationFragment;
    private SearchTotalFragment mSearchTotalFragment;
    private FollowingList mFollowingListFragment;
    private FollowerList mFollowerListFragment;
    private HomeTotal mHomeTotal;
    private MainMessageFragment mMainMessageFragment;
    private MessagingThreads mMessagingThreadsFragment;
    private CommentThread mCommentThreadFragment;
    //vars
    private ArrayList<String> mFragmentTags = new ArrayList<>();
    private ArrayList<FragmentTag> mFragments = new ArrayList<>();
    private int mExitcount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupFirebaseListener();
//        createNotificationsChannels();
        setUserDetails();
        setUserToken();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigation);

        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        initFragment();
    }

    private void initFragment() {
        Log.d(TAG,"initFragment");
        if (mHomeTotal == null) {
            mHomeTotal = new HomeTotal();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.main_container, mHomeTotal, getString(R.string.tag_home_total));
            transaction.commit();
            mFragmentTags.add(getString(R.string.tag_home_total));
            mFragments.add(new FragmentTag(mHomeTotal, getString(R.string.tag_home_total)));

        } else {
            mFragmentTags.remove(getString(R.string.tag_home_total));
            mFragmentTags.add(getString(R.string.tag_home_total));
        }

        setFragmentVisibilities(getString(R.string.tag_home_total));


    }


    private void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling

            return;
        }
        mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location=task.getResult();
                if (location!=null){
                    GeoPoint geoPoint=new GeoPoint(location.getLatitude(),location.getLongitude());

                }else {
                    Log.d(TAG,"Location: null");
                }








            }
        });
    }
    private void setUserDetails(){

        DocumentReference userRef=FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getUid());
        userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot.exists()){
                    User user=documentSnapshot.toObject(User.class);
                    ((UserClient)(getApplicationContext())).setUser(user);

                }
            }
        });
    }







    private void setupFirebaseListener(){
        Log.d(TAG, "setupFirebaseListener: setting up the auth state listener.");
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    Log.d(TAG, "onAuthStateChanged: signed_in: " + user.getUid());
                }else{
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                    Toast.makeText(MainActivity.this, "Signed out", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, SignIn.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        };
    }




    private void setFragmentVisibilities(String tagname){
        if (mBottomNavigationView.getVisibility()==View.INVISIBLE){
            mBottomNavigationView.setVisibility(View.VISIBLE);
        }
        Log.d(TAG,"visibility: "+mBottomNavigationView.getVisibility());
        Log.d(TAG,"fragment: "+tagname);
        for (int i=0; i < mFragments.size();i++){
            if (tagname.equals(mFragments.get(i).getTag())){
                Log.d(TAG,"Showing: "+mFragments.get(i).getTag());
                if (mFragments.get(i).getTag()== getString(R.string.tag_post_fragment)| mFragments.get(i).getTag()== getString(R.string.tag_main_message_tag) | mFragments.get(i).getTag()== getString(R.string.tag_comment_threads)){
                    mBottomNavigationView.setVisibility(View.INVISIBLE);
                }
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.show(mFragments.get(i).getFragment());
                transaction.commit();
            }else{
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.hide(mFragments.get(i).getFragment());
                transaction.commit();
            }

        }
        setNavigationIcon(tagname);
    }

    @Override
    public void onBackPressed() {

        int BackStagCount=mFragmentTags.size();
        if (BackStagCount>1){
            String TopFragmentTag=mFragmentTags.get(BackStagCount-1);
            String NewFragmentTag=mFragmentTags.get(BackStagCount-2);
            setFragmentVisibilities(NewFragmentTag);
            mFragmentTags.remove(TopFragmentTag);
            mExitcount=0;
        }else if (BackStagCount==1){
            mExitcount++;
            Toast.makeText(MainActivity.this,"1 more click to exit",Toast.LENGTH_SHORT).show();
        }
        if (mExitcount>=2){
            super.onBackPressed();
        }

    }

    @Override
    public void inflateViewProfile(User user) {
        if (mViewProfileFragment !=null){
            getSupportFragmentManager().beginTransaction().remove(mViewProfileFragment).commitAllowingStateLoss();
        }

        mViewProfileFragment =new ViewProfileFragment();

        Bundle args=new Bundle();
        args.putParcelable("ViewUser",user);
        mViewProfileFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.main_container, mViewProfileFragment,getString(R.string.tag_viewprofile_fragment));
        transaction.commit();

        mFragmentTags.add(getString(R.string.tag_viewprofile_fragment));
        mFragments.add(new FragmentTag(mViewProfileFragment,getString(R.string.tag_viewprofile_fragment)));
        setFragmentVisibilities(getString(R.string.tag_viewprofile_fragment));
    }

    @Override
    public User getUser() {
        User user=((UserClient)(getApplicationContext())).getUser();
        return user;
    }

    @Override
    public void inflateMyProfileFragment() {
        if (mMyProfile == null) {
            mMyProfile = new MyProfile();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.main_container, mMyProfile, getString(R.string.tag_myprofile_fragment));
            transaction.commit();
            mFragmentTags.add(getString(R.string.tag_myprofile_fragment));
            mFragments.add(new FragmentTag(mMyProfile, getString(R.string.tag_myprofile_fragment)));

        } else {
            if (mEditProfileFragment!=null){
                mFragmentTags.remove(getString(R.string.tag_editprofile_fragment));
            }


            mFragmentTags.remove(getString(R.string.tag_myprofile_fragment));
            mFragmentTags.add(getString(R.string.tag_myprofile_fragment));
        }

        setFragmentVisibilities(getString(R.string.tag_myprofile_fragment));

    }
    @Override
    public void inflateMessageThreads() {
        if (mMessagingThreadsFragment == null) {
            mMessagingThreadsFragment = new MessagingThreads();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.main_container, mMessagingThreadsFragment, getString(R.string.tag_message_threads));
            transaction.commit();
            mFragmentTags.add(getString(R.string.tag_message_threads));
            mFragments.add(new FragmentTag(mMessagingThreadsFragment, getString(R.string.tag_message_threads)));

        } else {
            if (mEditProfileFragment!=null){
                mFragmentTags.remove(getString(R.string.tag_message_threads));
            }


            mFragmentTags.remove(getString(R.string.tag_message_threads));
            mFragmentTags.add(getString(R.string.tag_message_threads));
        }

        setFragmentVisibilities(getString(R.string.tag_message_threads));

    }

    @Override
    public void inflateHomeFragment() {
        if (mHomeTotal == null) {
            mHomeTotal = new HomeTotal();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.main_container, mHomeTotal, getString(R.string.tag_home_total));
            transaction.commit();
            mFragmentTags.add(getString(R.string.tag_home_total));
            mFragments.add(new FragmentTag(mHomeTotal, getString(R.string.tag_home_total)));

        } else {



            mFragmentTags.remove(getString(R.string.tag_home_total));
            mFragmentTags.add(getString(R.string.tag_home_total));
        }

        setFragmentVisibilities(getString(R.string.tag_home_total));
    }

    @Override
    public void inflatePostFragment() {
        if (mPostFragment!=null){
            getSupportFragmentManager().beginTransaction().remove(mPostFragment).commitAllowingStateLoss();

        }
        mPostFragment = new PostFragment();
        Log.d(TAG,"PostFragment inflating");

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.main_container, mPostFragment, getString(R.string.tag_post_fragment));
        transaction.commit();

        mFragments.add(new FragmentTag(mPostFragment,getString(R.string.tag_post_fragment)));


        Log.d(TAG,"PostFragment inflating 3");


        setFragmentVisibilities(getString(R.string.tag_post_fragment));
    }

    @Override
    public void inflateNotificationFragment() {
        if (mNotifications == null) {
            mNotifications = new Notifications();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.main_container, mNotifications, getString(R.string.tag_notification_fragment));
            transaction.commit();
            mFragmentTags.add(getString(R.string.tag_notification_fragment));
            mFragments.add(new FragmentTag(mNotifications, getString(R.string.tag_notification_fragment)));

        } else {
            mFragmentTags.remove(getString(R.string.tag_notification_fragment));
            mFragmentTags.add(getString(R.string.tag_notification_fragment));
        }

        setFragmentVisibilities(getString(R.string.tag_notification_fragment));
    }

    @Override
    public void inflateSearchFragment() {
        if (mSearchFragment == null) {
            mSearchFragment = new SearchFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.main_container, mSearchFragment, getString(R.string.tag_search_fragment));
            transaction.commit();
            mFragmentTags.add(getString(R.string.tag_search_fragment));
            mFragments.add(new FragmentTag(mSearchFragment, getString(R.string.tag_search_fragment)));

        } else {
            mFragmentTags.remove(getString(R.string.tag_search_fragment));
            mFragmentTags.add(getString(R.string.tag_search_fragment));
        }

        setFragmentVisibilities(getString(R.string.tag_search_fragment));
    }

    @Override
    public void inflateFollowingListFragment(String userId ) {
        if (mFollowingListFragment!=null){
            getSupportFragmentManager().beginTransaction().remove(mFollowingListFragment).commitAllowingStateLoss();
        }

        mFollowingListFragment=new FollowingList();

        Bundle args=new Bundle();
        args.putString("ViewedUser",userId);
        mFollowingListFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.main_container,mFollowingListFragment,getString(R.string.tag_following_list_fragment));
        transaction.commit();

        mFragmentTags.add(getString(R.string.tag_following_list_fragment));
        mFragments.add(new FragmentTag(mFollowingListFragment,getString(R.string.tag_following_list_fragment)));
        setFragmentVisibilities(getString(R.string.tag_following_list_fragment));
    }

    @Override
    public void inflateCommentThread(String postId,User postUser) {
        if (mCommentThreadFragment!=null){
            getSupportFragmentManager().beginTransaction().remove(mCommentThreadFragment).commitAllowingStateLoss();
        }

        mCommentThreadFragment=new CommentThread();

        Bundle args=new Bundle();
        args.putString("postId",postId);
        args.putParcelable("postUser",postUser);

        mCommentThreadFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.main_container,mCommentThreadFragment,getString(R.string.tag_comment_threads));
        transaction.commit();

        mFragmentTags.add(getString(R.string.tag_comment_threads));
        mFragments.add(new FragmentTag(mCommentThreadFragment,getString(R.string.tag_comment_threads)));
        setFragmentVisibilities(getString(R.string.tag_comment_threads));
    }

    @Override
    public void inflateFollowerListFragment(String userId) {

        if (mFollowerListFragment!=null){
            getSupportFragmentManager().beginTransaction().remove(mFollowerListFragment).commitAllowingStateLoss();
        }

        mFollowerListFragment=new FollowerList();

        Bundle args=new Bundle();
        args.putString("ViewedUser",userId);
        mFollowerListFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.main_container,mFollowerListFragment,getString(R.string.tag_follower_list_fragment));
        transaction.commit();

        mFragmentTags.add(getString(R.string.tag_follower_list_fragment));
        mFragments.add(new FragmentTag(mFollowerListFragment,getString(R.string.tag_follower_list_fragment)));
        setFragmentVisibilities(getString(R.string.tag_follower_list_fragment));
    }


    private void setNavigationIcon(String tagname){
        Menu menu=mBottomNavigationView.getMenu();
        MenuItem menuItem=null;
        if (tagname.equals(getString(R.string.tag_home_total))){
            menuItem=menu.getItem(HOME);
            menuItem.setChecked(true);
        }else if (tagname.equals(getString(R.string.tag_searchtotal_fragment))){
            menuItem=menu.getItem(SEARCH);
            menuItem.setChecked(true);
        }else if (tagname.equals(getString(R.string.tag_notification_fragment))){
            menuItem=menu.getItem(NOTIFICATIONS);
            menuItem.setChecked(true);
        }else if (tagname.equals(getString(R.string.tag_myprofile_fragment))){
            menuItem=menu.getItem(MYPROFILE);
            menuItem.setChecked(true);
        }
    }

    @Override
    public void inflateViewPostFragment(Post post) {

        if (mViewPostFragment!=null){
            getSupportFragmentManager().beginTransaction().remove(mViewPostFragment).commitAllowingStateLoss();
        }

        mViewPostFragment=new ViewPostFragment();

        Bundle args=new Bundle();
        args.putParcelable("ClickedPost",post);
        mViewPostFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.main_container,mViewPostFragment,getString(R.string.tag_viewpost_fragment));
        transaction.commit();

        mFragmentTags.add(getString(R.string.tag_viewpost_fragment));
        mFragments.add(new FragmentTag(mViewPostFragment,getString(R.string.tag_viewpost_fragment)));
        setFragmentVisibilities(getString(R.string.tag_viewpost_fragment));
    }


    @Override
    public void inflateEditFragment(User user) {
        if (mEditProfileFragment!=null){
            getSupportFragmentManager().beginTransaction().remove(mEditProfileFragment).commitAllowingStateLoss();

        }

        mEditProfileFragment=new EditProfileFragment();

        Bundle args=new Bundle();
        args.putParcelable("editedUser",user);
        mEditProfileFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.main_container,mEditProfileFragment,getString(R.string.tag_editprofile_fragment));
        transaction.commit();

        mFragmentTags.add(getString(R.string.tag_editprofile_fragment));
        mFragments.add(new FragmentTag(mEditProfileFragment,getString(R.string.tag_editprofile_fragment)));
        setFragmentVisibilities(getString(R.string.tag_editprofile_fragment));

    }
    @Override
    public void inflateMainMessageFragment(User user) {
        if (mMainMessageFragment!=null){
            getSupportFragmentManager().beginTransaction().remove(mMainMessageFragment).commitAllowingStateLoss();

        }

        mMainMessageFragment=new MainMessageFragment();

        Bundle args=new Bundle();
        args.putParcelable("clientUser",user);
        mMainMessageFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.main_container,mMainMessageFragment,getString(R.string.tag_main_message_tag));
        transaction.commit();

        mFragmentTags.add(getString(R.string.tag_main_message_tag));
        mFragments.add(new FragmentTag(mMainMessageFragment,getString(R.string.tag_main_message_tag)));
        setFragmentVisibilities(getString(R.string.tag_main_message_tag));

    }
    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mAuthStateListener != null){
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener);
        }
    }
    public void logout(){
        FirebaseAuth.getInstance().signOut();
    }
//    private void createNotificationsChannels(){
//        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
//            NotificationChannel channel1=new NotificationChannel(
//                    CHANNEL_1,
//                    "channel1",
//                    NotificationManager.IMPORTANCE_DEFAULT
//            );
//            channel1.setDescription("this is channel 1");
//            NotificationChannel channel2=new NotificationChannel(
//                    CHANNEL_2,
//                    "channel2",
//                    NotificationManager.IMPORTANCE_DEFAULT
//            );
//            channel2.setDescription("this is channel 2");
//
//            NotificationManager manager=getSystemService(NotificationManager.class);
//            manager.createNotificationChannel(channel1);
//            manager.createNotificationChannel(channel2);
//
//        }
//    }
    private void setUserToken(){
        final String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (task.isSuccessful()){
                    String token=task.getResult().getToken();
                    NotificationToken obj=new NotificationToken();
                    obj.setToken(token);
                    DocumentReference ref=FirebaseFirestore.getInstance().collection("notificationTokens").document(userId);
                    ref.set(obj).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(MainActivity.this,"upload is succesfull",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
