package com.burakdal.voiceproject;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.burakdal.voiceproject.models.FollowObject;
import com.burakdal.voiceproject.models.NotificationObject;
import com.burakdal.voiceproject.models.Post;
import com.burakdal.voiceproject.models.User;
import com.burakdal.voiceproject.utils.MyProfileRecyclerViewAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfileFragment extends Fragment {

    private final static String TAG="ViewProfileFragment";
    private final static Integer COL=1;
    //vars
    private CircleImageView mProfileImage;
    private TextView mName,mDescription;
    private ImageButton mPlayBtn;
    private Button mFollow;
    private String mVoiceUrl;
    //Media Player
    private MediaPlayer mMediaPlayer;
    private boolean PLAYING_STATE=false;
    private int mCurrentPosition=0;
    private boolean IS_MY_FOLLOWING=false;

    private IMainActivity mInterface;
    private ArrayList<Post> mPostArrayList=new ArrayList<>();


    private User mUser;
    private FirebaseFirestore mDb;
    private MyProfileRecyclerViewAdapter mRecyclerViewAdapter;

    private Context mContext;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle=this.getArguments();
        if (bundle!=null){
            mUser=bundle.getParcelable("ViewUser");


        }
    }

    private DatabaseReference mDatabaseReference;

    private RecyclerView mRecyclerView;

    private MainActivity mMainActivity;
    //toolbar
    private Toolbar mToolbar;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mInterface=(MainActivity)context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.view_profile,container,false);
        mProfileImage=(CircleImageView)view.findViewById(R.id.view_profile_img);
        mFollow=(Button)view.findViewById(R.id.view_profile_follow);
        mName=(TextView)view.findViewById(R.id.view_profile_name);
        mDescription=(TextView)view.findViewById(R.id.view_profile_description);
        mPlayBtn=(ImageButton)view.findViewById(R.id.view_profile_play);
        mRecyclerView=(RecyclerView) view.findViewById(R.id.view_profile_recycler_view);
        mContext=(MainActivity)getContext();
        mDatabaseReference=FirebaseDatabase.getInstance().getReference();
        mToolbar=(Toolbar)view.findViewById(R.id.view_profile_toolbar);
        initWholePage();



        return view;
    }
    private void initWholePage(){
        mToolbar.setNavigationIcon(R.drawable.back_ic);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInterface.onBackPressed();
            }
        });
        mToolbar.setTitle(mUser.getName());
        RequestOptions requestOptions=new RequestOptions().placeholder(R.drawable.my_profile);
        Log.d(TAG,"YOU CAN FOLLOW");
        mFollow.setText("follow");
        mFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),"FOLLOWED",Toast.LENGTH_SHORT).show();
                FollowObject followObject=new FollowObject();
                followObject.setFollower_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
                followObject.setFollowed_id(mUser.getUser_id());
                DocumentReference ref=FirebaseFirestore.getInstance().collection("followings").document(mUser.getUser_id());
                final DocumentReference notRef=FirebaseFirestore.getInstance().collection("notifications").document(mUser.getUser_id()).collection("nots").document();

                ref.set(followObject).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(getActivity(),"successfully uploaded",Toast.LENGTH_SHORT).show();
                            NotificationObject notificationObject=new NotificationObject();
                            notificationObject.setSender(mInterface.getUser());
                            notificationObject.setSended(mUser);
                            notificationObject.setMessage(" followed you");
                            notRef.set(notificationObject).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.d(TAG,"following not successfully uploaded");
                                }
                            });
                            mFollow.setText("Message");
                        }else{
                            Toast.makeText(getActivity(),"there is a problem in the process of following",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        if (mUser.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            mFollow.setText("EDIT");
            mFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mInterface.inflateEditFragment(mUser);
                }
            });
        }else{

            Log.d(TAG,"query getting: "+mUser.getUser_id());

            Query query=FirebaseFirestore.getInstance().collection("followings").whereEqualTo("follower_id",FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .whereEqualTo("followed_id",mUser.getUser_id());

            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){

                        for (DocumentSnapshot snapshot:task.getResult()){
                            FollowObject object=snapshot.toObject(FollowObject.class);
                            if (object!=null){
                                mFollow.setText("message");
                                mFollow.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Toast.makeText(getActivity(),"sending messsage",Toast.LENGTH_SHORT).show();
                                        mInterface.inflateMainMessageFragment(mUser);
                                    }
                                });
                            }

                        }

                    }
            };


        });



        }
        Glide.with(mContext).load(mUser.getProfileImage()).apply(requestOptions).into(mProfileImage);
        mName.setText(mUser.getName());
        mDescription.setText(mUser.getDescription());
        mVoiceUrl=mUser.getDescriptionVoice();
        getPosts();
        initRecyclerView();
    }



    private void initRecyclerView() {
        if (mRecyclerViewAdapter==null){
            mRecyclerViewAdapter=new MyProfileRecyclerViewAdapter(mUser.getUser_id(),(MainActivity)getContext(),mPostArrayList);
        }
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
    }

    private void getPosts(){
        Log.d(TAG,"init recycler view:mUser id: "+mUser.getUser_id());
        mDb=FirebaseFirestore.getInstance();
        CollectionReference ref=mDb.collection("posts");
        com.google.firebase.firestore.Query postQuery=ref.whereEqualTo("user_id",mUser.getUser_id());
        postQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot snapshot:task.getResult()){
                        Post post=snapshot.toObject(Post.class);
                        mPostArrayList.add(post);



                    }
                    mRecyclerViewAdapter.notifyDataSetChanged();



                }
            }
        });

    }
}
