package com.burakdal.voiceproject;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.burakdal.voiceproject.models.Comment;
import com.burakdal.voiceproject.models.NotificationObject;
import com.burakdal.voiceproject.models.User;
import com.burakdal.voiceproject.utils.CommentThreadAdapter;
import com.burakdal.voiceproject.utils.MainMessageListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class CommentThread extends Fragment {
    private final static String TAG="CommentThread";
    private ListView mRecyclerView;
    private ImageButton mRecordComment,mSendComment;
    private EditText mCommentInput;
    private Toolbar mToolbar;
    private IMainActivity mInterface;
    private String mPostId;
    private CommentThreadAdapter mAdapter;
    private User mCurrentUser;
    private String mPostUserId;
    private User mPostUser;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mInterface=(IMainActivity)context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle=this.getArguments();
        if (bundle!=null){
            mPostId=bundle.getString("postId");
            mCurrentUser=mInterface.getUser();
            mPostUserId=bundle.getString("postUserId");
            mPostUser=bundle.getParcelable("postUser");






        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"post id: "+mPostId);
        mAdapter=new CommentThreadAdapter(getActivity(),mPostId,(MainActivity)getContext());
        mRecyclerView.setAdapter(mAdapter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.comment_thread,container,false);
        mRecyclerView=(ListView)view.findViewById(R.id.comment_thread_rec);
        mRecordComment=(ImageButton)view.findViewById(R.id.recordBtn_for_comment);
        mSendComment=(ImageButton)view.findViewById(R.id.send_comment_btn);
        mCommentInput=(EditText)view.findViewById(R.id.comment_input);
        mCommentInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (v.getText()!=null){
                    sendComment(v.getText().toString());
                }
                return false;
            }
        });
        mToolbar=(Toolbar)view.findViewById(R.id.comment_thread_toolbar);
        initToolbar();
        mSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCommentInput.getText()!=null){
                    sendComment(mCommentInput.getText().toString());
                }
            }
        });



        return view;
    }
    private void initToolbar(){
        mToolbar.setNavigationIcon(R.drawable.back_ic);
        mToolbar.setTitle("Comments");
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInterface.onBackPressed();
            }
        });

    }
    private void sendComment(String message){
        DocumentReference ref=FirebaseFirestore.getInstance().collection("comments").document();
        final DocumentReference notRef=FirebaseFirestore.getInstance().collection("notifications").document(mPostUser.getUser_id()).collection("nots").document();
        Comment comment=new Comment();
        comment.setMessage(message);
        comment.setCommentId(ref.getId());
        comment.setPostId(mPostId);
        comment.setCommenter(mCurrentUser);
        ref.set(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(getActivity(),"successfully commented",Toast.LENGTH_SHORT).show();
                NotificationObject notificationObject=new NotificationObject();
                notificationObject.setSender(mCurrentUser);
                notificationObject.setSended(mPostUser);
                notificationObject.setMessage(" commented on your post");

                notRef.set(notificationObject).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG,"comment not is uploaded successfully");
                    }
                });


            }
        });

    }
//    private void getPostUser(){
//        DocumentReference ref=FirebaseFirestore.getInstance().collection("users").document(mPostUserId);
//        ref.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//            @Override
//            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
//                if (documentSnapshot.exists()){
//                    mPostUser=documentSnapshot.toObject(User.class);
//
//                }
//            }
//        });
//
//    }


}
