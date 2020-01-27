package com.burakdal.voiceproject;

import android.app.Notification;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.burakdal.voiceproject.models.FollowObject;
import com.burakdal.voiceproject.models.NotificationToken;
import com.burakdal.voiceproject.models.Post;
import com.burakdal.voiceproject.models.User;
import com.burakdal.voiceproject.utils.MyProfileRecyclerViewAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.burakdal.voiceproject.MainActivity.CHANNEL_1;
import static com.burakdal.voiceproject.MainActivity.CHANNEL_2;

public class MyProfile extends Fragment {
    private final static String TAG="MyProfile";
    private final static Integer COL=1;
    private final static String NOTIFICATION_ID="1234";
    //vars
    private CircleImageView mProfileImage;
    private TextView mName,mDescription,mPostCount,mFollowerCount,mFollowingCount;
    private Button mEditBtn;
    private ImageButton mPlayBtn;
    private String mVoiceUrl;
    private ArrayList<Post> mPostArrayList=new ArrayList<>();
   //Media Player
    private MediaPlayer mMediaPlayer;
    private boolean PLAYING_STATE=false;
    private int mCurrentPosition=0;
    //notifications
    private NotificationManagerCompat mNotificationManagerCompat;
    private IMainActivity mInterface;

    private User mUser;

    private Context mContext;




    private DatabaseReference mDatabaseReference;
    private FirebaseFirestore mDb;

    private RecyclerView mRecyclerView;
    private MyProfileRecyclerViewAdapter mRecyclerViewAdapter;

    private MainActivity mMainActivity;
    //toolbar
    private Toolbar mToolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private DocumentSnapshot mLastQueriedDocument;
    private ArrayList<String> mFollowerList=new ArrayList<>();
    private ArrayList<String> mFollowingList=new ArrayList<>();









    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.myprofile_fragment,container,false);


        init(view);
        mToolbar=(Toolbar) view.findViewById(R.id.toolbar);
        mNotificationManagerCompat=NotificationManagerCompat.from(getActivity());

        mToolbar.inflateMenu(R.menu.my_profile_menu);
        mToolbar.getMenu().findItem(R.id.logout).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                DocumentReference ref=FirebaseFirestore.getInstance()
                        .collection("notificationTokens").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                NotificationToken token=new NotificationToken();
                ref.set(token).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseAuth.getInstance().signOut();

                    }
                });

                return false;
            }
        });









        initWholePage();





        mPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"mPATH in playbtn: "+mVoiceUrl);
                AsyncTask<String,String,String> audioPlayer=new AsyncTask<String, String, String>() {


                    @Override
                    protected String doInBackground(String... strings) {
                        try {





                            if (!PLAYING_STATE){
                                mMediaPlayer= new MediaPlayer();

                                mMediaPlayer.setDataSource(strings[0]);
                                mMediaPlayer.prepare();

                            }





                            ;

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(String s) {




                        if (!mMediaPlayer.isPlaying()){
                            if (!PLAYING_STATE){

                                mMediaPlayer.start();
                                PLAYING_STATE=true;
                            }else{


                                mMediaPlayer.seekTo(mCurrentPosition);
                                mMediaPlayer.start();

                            }
                            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    mCurrentPosition=0;
                                    Toast.makeText(mContext,"Playing is finished", Toast.LENGTH_SHORT).show();

                                    mp.release();
                                    PLAYING_STATE=false;

                                    mPlayBtn.setImageResource(R.drawable.play_ic);


                                }
                            });

                            mPlayBtn.setImageResource(R.drawable.pause_ic);







                            Toast.makeText(mContext,"Playing", Toast.LENGTH_SHORT).show();

                        }else{
                            mMediaPlayer.pause();
                            mCurrentPosition=mMediaPlayer.getCurrentPosition();
//                            mRealTimeLength=mMediaFileLength-mCurrentPosition;


                            mPlayBtn.setImageResource(R.drawable.play_ic);
                        }


                    }
                };
                if (mVoiceUrl!=null){
                    audioPlayer.execute(mVoiceUrl);

                }else{
                    Toast.makeText(getActivity(),"you have to define your description voice",Toast.LENGTH_SHORT).show();
                }

            }
        });











        return view;
    }

    private void init(View view){

        mProfileImage=(CircleImageView)view.findViewById(R.id.myprofile_img);
        mName=(TextView)view.findViewById(R.id.myprofile_name);
        mPostCount=(TextView)view.findViewById(R.id.my_profile_post_count);
        mFollowerCount=(TextView)view.findViewById(R.id.my_profile_follower_count);
        mFollowingCount=(TextView)view.findViewById(R.id.my_profile_following_count);

        mDescription=(TextView)view.findViewById(R.id.myprofile_description);
        mPlayBtn=(ImageButton)view.findViewById(R.id.myprofile_play);
        mSwipeRefreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.myprofile_swipe);
        mRecyclerView=(RecyclerView) view.findViewById(R.id.recycler_view1);

        mContext=getActivity();
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getUserPosts();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });








    }
    private void initWholePage(){

        DocumentReference userRef=FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (documentSnapshot.exists()){
                    mUser=documentSnapshot.toObject(User.class);
                    RequestOptions requestOptions=new RequestOptions().placeholder(R.drawable.my_profile);

                    Glide.with(mContext).load(mUser.getProfileImage()).apply(requestOptions).into(mProfileImage);
                    mName.setText(mUser.getName());
                    mDescription.setText(mUser.getDescription());
                    mVoiceUrl=mUser.getDescriptionVoice();
                    mToolbar.getMenu().findItem(R.id.edit).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            mInterface.inflateEditFragment(mUser);
                            return false;
                        }
                    });
                    mToolbar.setTitle(mUser.getName());

                    Log.d(TAG,"follower size: "+mFollowerList.size());
                    Log.d(TAG,"following size: "+mFollowingList.size());






                    initRecyclerView();
                    getUserPosts();




                }

            }
        });



        getFollowersId();
        getFollowingId();
        getPostCount();










    }

    private void getPostCount() {
        com.google.firebase.firestore.Query query=FirebaseFirestore.getInstance().collection("posts").whereEqualTo("user_id",FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    mPostCount.setText(Integer.toString(task.getResult().size()));
                }
            }
        });
    }

    private void getFollowersId(){
        com.google.firebase.firestore.Query ref2 =FirebaseFirestore.getInstance().collection("followings").whereEqualTo("followed_id",FirebaseAuth.getInstance().getCurrentUser().getUid());

        ref2.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Log.d("interfaceMethod","in interface2");

                if (task.isSuccessful()){
                    Log.d("interfaceMethod","in interface3");


                    mFollowerCount.setText(Integer.toString(task.getResult().size()));
                    mFollowerCount.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mInterface.inflateFollowerListFragment(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        }
                    });



                }
            }

        });
    }
    private void getFollowingId(){
        com.google.firebase.firestore.Query ref =FirebaseFirestore.getInstance().collection("followings").whereEqualTo("follower_id",FirebaseAuth.getInstance().getCurrentUser().getUid());

        ref.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Log.d("interfaceMethod","in interface2");

                if (task.isSuccessful()){
                    Log.d("interfaceMethod","in interface3");


                    mFollowingCount.setText(Integer.toString(task.getResult().size()));
                    mFollowingCount.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mInterface.inflateFollowingListFragment(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        }
                    });



                }
            }

        });
    }

    private void initRecyclerView() {
        if (mRecyclerViewAdapter==null){
            mRecyclerViewAdapter=new MyProfileRecyclerViewAdapter(mUser.getUser_id(),(MainActivity)getContext(),mPostArrayList);
        }

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mRecyclerViewAdapter);

        Log.d(TAG,"the size of list: "+mPostArrayList.size());
    }





    private void getUserPosts(){
        Log.d(TAG,"init recycler view:mUser id: "+mUser.getUser_id());

        mDb=FirebaseFirestore.getInstance();

        CollectionReference ref=mDb.collection("posts");
        com.google.firebase.firestore.Query postQuery=null;
        if (mLastQueriedDocument!=null){
            postQuery=ref.whereEqualTo("user_id",FirebaseAuth.getInstance().getCurrentUser().getUid()).orderBy("timeStamp", com.google.firebase.firestore.Query.Direction.ASCENDING).startAfter(mLastQueriedDocument);
        }else{
            postQuery=ref.whereEqualTo("user_id",FirebaseAuth.getInstance().getCurrentUser().getUid()).orderBy("timeStamp", com.google.firebase.firestore.Query.Direction.ASCENDING);
        }

        postQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot snapshot:task.getResult()){
                        Post post=snapshot.toObject(Post.class);
                        mPostArrayList.add(post);



                    }
                    if (task.getResult().size()!=0){
                        if (mLastQueriedDocument==null){
                            mLastQueriedDocument=task.getResult().getDocuments().get(task.getResult().size()-1);
                        }
                    }

                    mRecyclerViewAdapter.notifyDataSetChanged();



                }
            }
        });


    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mInterface=(IMainActivity)context;
    }
//    private void sendFromChannel1(){
//        NotificationCompat.Builder notification=new NotificationCompat.Builder(getActivity(),CHANNEL_1)
//                .setSmallIcon(R.drawable.not_icon)
//                .setContentTitle("this is first channel")
//                .setContentText("heyy you fuckers");
//        mNotificationManagerCompat.notify(1,notification.build());
//    }
//    private void sendFromChannel2(){
//        NotificationCompat.Builder notification=new NotificationCompat.Builder(getActivity(),CHANNEL_2)
//                .setSmallIcon(R.drawable.not_icon)
//                .setContentTitle("this is second channel")
//                .setContentText("heyy you fuckers");
//        mNotificationManagerCompat.notify(2,notification.build());
//    }





}
