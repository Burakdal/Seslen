package com.burakdal.voiceproject;

import android.content.Context;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.v4.app.Fragment;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.burakdal.voiceproject.models.Comment;
import com.burakdal.voiceproject.models.Liking;
import com.burakdal.voiceproject.models.NotificationObject;
import com.burakdal.voiceproject.models.Post;
import com.burakdal.voiceproject.models.User;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.like.LikeButton;
import com.like.OnLikeListener;


import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewPostFragment extends Fragment {
    private final String TAG="ViewPostFragment";

    private Post mPost;

    private DatabaseReference mDatabaseReference;
    private Context mContext;
    private CircleImageView mPostUserImage;
    private TextView mPostUserUsername,mPostDescription,mPostLocation,mLikeCount,mCommentCount;
    private ImageView mPostImage;
    private ImageButton mBackBtn,mPlayBtn;
    private SeekBar mSeekBar;
    private android.support.v7.widget.Toolbar mToolbar;
    private IMainActivity mInterface;
    private User mPostUser;
    private PlacesClient mPlacesClient;
    private ImageButton mCommentBtn;
    private RecyclerView mRecyclerView;
    private LikeButton mLikeBtn;
    //play
    private boolean LIKING_STATE=false;
    private String mVoiceUrl;
    private boolean PLAYING_STATE=false;
    private MediaPlayer mMediaPlayer=new MediaPlayer();
    private int mMediaFileLength;
    final Handler mHandler = new Handler();


    private String mLikingId;
    private int mCurrentPosition=0;
    private int mRealTimeLength=0;







    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle=this.getArguments();
        if (bundle!=null){
            mPost=bundle.getParcelable("ClickedPost");
            mVoiceUrl=mPost.getAudio();



        }
        Query query=FirebaseFirestore.getInstance().collection("likings").whereEqualTo("post",mPost);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().size()>0){
                        mLikeCount.setText(Integer.toString(task.getResult().size()));
                    }
                }
            }
        });
        Query queryForComment=FirebaseFirestore.getInstance().collection("comments").whereEqualTo("postId",mPost.getPost_id());
        queryForComment.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){

                    if (task.getResult().size()>0){
                        mCommentCount.setText(Integer.toString(task.getResult().size()));
                    }
                }
            }
        });
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mInterface=(IMainActivity)context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.view_post,container,false);
        Places.initialize(getActivity(), getString(R.string.google_maps_key));
        mPlacesClient = Places.createClient(getActivity());

        init(view);

        initUserParams();
        initPostParams();





        return view;
    }
    private void init(View view){
        mPostUserImage=(CircleImageView) view.findViewById(R.id.post_user_image);
        mPostUserUsername=(TextView) view.findViewById(R.id.post_user_username);
        mPostImage=(ImageView)view.findViewById(R.id.post_image);
        mPlayBtn=(ImageButton)view.findViewById(R.id.play_btn);
        mSeekBar=(SeekBar)view.findViewById(R.id.seekBar2);
        mPostDescription=(TextView)view.findViewById(R.id.post_description);
        mPostLocation=(TextView)view.findViewById(R.id.view_post_location_name);
        mDatabaseReference=FirebaseDatabase.getInstance().getReference();
        mLikeCount=(TextView)view.findViewById(R.id.like_count);
        mCommentCount=(TextView)view.findViewById(R.id.comment_count);
        mLikeBtn=(LikeButton)view.findViewById(R.id.like_button);
        mCommentBtn=(ImageButton)view.findViewById(R.id.comment_button);
        mContext=(MainActivity)getContext();
        mToolbar=(android.support.v7.widget.Toolbar)view.findViewById(R.id.view_post_toolbar);
        mRecyclerView=(RecyclerView)view.findViewById(R.id.post_view_recyclerView);



    }
    private void initUserParams(){

        mToolbar.setNavigationIcon(R.drawable.back_ic);
        mToolbar.inflateMenu(R.menu.map_toolbar_menu);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInterface.onBackPressed();
            }
        });
        mToolbar.setTitle("post");

        com.google.firebase.firestore.Query query=FirebaseFirestore.getInstance().collection("users").whereEqualTo("user_id",mPost.getUser_id());
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for(final QueryDocumentSnapshot snapshot:task.getResult()){
                        mPostUser=snapshot.toObject(User.class);
                        mCommentBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mInterface.inflateCommentThread(mPost.getPost_id(),mPostUser);
                            }
                        });
                        if (mPostUser!=null){
                            likeButtonParams();
                            mPostUserUsername.setText(mPostUser.getName());
                            if(mPostUser.getProfileImage()!=null){
//                                RequestOptions requestOptions=new RequestOptions().placeholder(R.drawable.my_profile);

                                Glide.with(mContext).load(mPostUser.getProfileImage()).into(mPostUserImage);
                                mPostUserUsername.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mInterface.inflateViewProfile(mPostUser);
                                    }
                                });
                                mPostUserImage.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        mInterface.inflateViewProfile(mPostUser);
                                    }
                                });
                                Query query=FirebaseFirestore.getInstance().collection("likings").whereEqualTo("liker",mInterface.getUser()).whereEqualTo("post",mPost);
                                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if(task.isSuccessful()){
                                            if (task.getResult().size()>0){
                                                Log.d(TAG,"there is like");
                                                LIKING_STATE=true;
                                                for(QueryDocumentSnapshot snapshot1:task.getResult()){
                                                    mLikingId=snapshot1.toObject(Liking.class).getLikingId();
                                                }
                                                mLikeBtn.setLiked(true);
                                            }else{
                                                Log.d(TAG,"there is no like");
                                                LIKING_STATE=false;
                                                mLikeBtn.setLiked(false);

                                            }
                                        }
                                    }
                                });


                            }


                        }else {
                            Toast.makeText(mContext,"User is null",Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            }
        });

        mMediaPlayer= new MediaPlayer();

        mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                mSeekBar.setSecondaryProgress(percent);
                mRealTimeLength=mCurrentPosition;

            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mCurrentPosition=0;
                Toast.makeText(getActivity(),"Playing is finished", Toast.LENGTH_SHORT).show();
//                PLAYING_STATE=false;
                mPlayBtn.setImageResource(R.drawable.play_ic);


            }
        });
        mPlayBtn.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {


                AsyncTask<String,String,String> audioPlayer=new AsyncTask<String, String, String>() {


                    @Override
                    protected String doInBackground(String... strings) {
                        try {

                            if (!PLAYING_STATE){

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
                        mMediaFileLength=mMediaPlayer.getDuration();


                        mRealTimeLength=mMediaPlayer.getCurrentPosition();
                        if (!mMediaPlayer.isPlaying()){
                            if (!PLAYING_STATE){

                                mMediaPlayer.start();
                                PLAYING_STATE=true;
                            }else{
                                mRealTimeLength=mCurrentPosition;

                                mMediaPlayer.seekTo(mCurrentPosition);
                                mMediaPlayer.start();
                            }









                            mPlayBtn.setImageResource(R.drawable.pause_ic);
                            Toast.makeText(getActivity(),"Playing", Toast.LENGTH_SHORT).show();

                        }else{
                            mMediaPlayer.pause();
                            mCurrentPosition=mMediaPlayer.getCurrentPosition();
//                            mRealTimeLength=mMediaFileLength-mCurrentPosition;


                            mPlayBtn.setImageResource(R.drawable.play_ic);
                        }
                        updateSeekBar();

                    }
                };
                audioPlayer.execute(mVoiceUrl);
            }
        });











    }

    private void updateSeekBar() {
        mSeekBar.setProgress((int)(((float)mMediaPlayer.getCurrentPosition()/mMediaFileLength)*100));
        if (mMediaPlayer.isPlaying()){
            Runnable updater=new Runnable() {
                @Override
                public void run() {
                    updateSeekBar();

                    mRealTimeLength+=1000;
                }

            };
            mHandler.postDelayed(updater,1000);
        }
    }



    private void likeButtonParams(){
        mLikeBtn.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                final DocumentReference notRef=FirebaseFirestore.getInstance().collection("notifications").document(mPostUser.getUser_id()).collection("nots").document();

                final DocumentReference ref=FirebaseFirestore.getInstance().collection("likings").document();
                Liking liking=new Liking();
                liking.setLiker(mInterface.getUser());
                liking.setPost(mPost);
                liking.setLikingId(ref.getId());
                mLikingId=ref.getId();
                ref.set(liking).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getActivity(),"successfully liked",Toast.LENGTH_SHORT).show();
                        int likeCount=Integer.parseInt(mLikeCount.getText().toString());
                        likeCount=likeCount+1;
                        NotificationObject notObj=new NotificationObject();
                        notObj.setSender(mInterface.getUser());
                        notObj.setSended(mPostUser);
                        notObj.setMessage(" likes your post");
                        notRef.set(notObj).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d(TAG,"like not uploaded");
                            }
                        });

                        mLikeCount.setText(Integer.toString(likeCount));

                    }
                });


            }

            @Override
            public void unLiked(LikeButton likeButton) {
                DocumentReference ref=FirebaseFirestore.getInstance().collection("likings").document(mLikingId);

                ref.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getActivity(),"deleted like",Toast.LENGTH_SHORT).show();
                        int likeCount=Integer.parseInt(mLikeCount.getText().toString());
                        likeCount=likeCount-1;
                        mLikeCount.setText(Integer.toString(likeCount));

                    }
                });
            }
        });
    }
    private void initPostParams(){
        RequestOptions requestOptions=new RequestOptions().placeholder(R.drawable.my_profile);
        if (mPost.getPlaceId()!=null){
            final String placeId=mPost.getPlaceId();
            List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME);
            FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();
            mPlacesClient.fetchPlace(request).addOnCompleteListener(new OnCompleteListener<FetchPlaceResponse>() {
                @Override
                public void onComplete(@NonNull Task<FetchPlaceResponse> task) {
                    if (task.isSuccessful()){
                        Place place=task.getResult().getPlace();
                        mPostLocation.setText(place.getName());

                    }else{
                        mPostLocation.setText("location is not found");

                    }
                }
            });

        }




        Glide.with(mContext).load(mPost.getImage()).apply(requestOptions).into(mPostImage);
        mPostDescription.setText(mPost.getDescription());


    }




}
