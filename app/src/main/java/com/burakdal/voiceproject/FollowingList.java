package com.burakdal.voiceproject;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.FontRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.burakdal.voiceproject.models.FollowObject;
import com.burakdal.voiceproject.models.User;
import com.burakdal.voiceproject.utils.SearchRecyclerViewAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.PropertyResourceBundle;

public class FollowingList extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private final String TAG="FollowingList";
    private ArrayList<User> mUserList=new ArrayList<>();
    private SearchRecyclerViewAdapter mRecyclerViewAdapter;
    private RecyclerView mRecyclerView;
    private DocumentSnapshot mLastQueriedDocument;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Toolbar mToolbar;
    private IMainActivity mInterface;
    private ProgressBar mProgressBar;
    private String mUserId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle=this.getArguments();
        if (bundle!=null){
            mUserId=bundle.getString("ViewedUser");


        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mInterface=(IMainActivity)context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.following_list_layout,container,false);
        mRecyclerView=(RecyclerView)view.findViewById(R.id.following_list_rec);
        mToolbar=(Toolbar)view.findViewById(R.id.following_list_toolbar);
        mProgressBar=(ProgressBar)view.findViewById(R.id.following_list_progressBar);

        mToolbar.setTitle("following list");
        mToolbar.setNavigationIcon(R.drawable.back_ic);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInterface.onBackPressed();
            }
        });
        mSwipeRefreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.following_list_swipe);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        initRecyclerView();
        getFollowingUsers();
        return view;

    }
    private void getFollowingUsers(){
        Query ref=null;

        if (mLastQueriedDocument!=null){
            ref =FirebaseFirestore.getInstance()
                    .collection("followings")
                    .whereEqualTo("follower_id",mUserId).orderBy("timeStamp",Query.Direction.DESCENDING)
                    .startAfter(mLastQueriedDocument);

        }else {
            ref =FirebaseFirestore.getInstance().collection("followings").whereEqualTo("follower_id",mUserId);
        }

        ref.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Log.d("interfaceMethod","in interface2");

                if (task.isSuccessful()){
                    Log.d("interfaceMethod","in interface3");

                    for (DocumentSnapshot snapshot:task.getResult()){
                        FollowObject obj=snapshot.toObject(FollowObject.class);
                        DocumentReference ref1=FirebaseFirestore.getInstance().collection("users").document(obj.getFollowed_id());
                        ref1.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                                if (documentSnapshot.exists()){
                                    User user=documentSnapshot.toObject(User.class);
                                    mUserList.add(user);
                                }
                            }
                        });
                    }
                    if (task.getResult().size()!=0){
                        mLastQueriedDocument=task.getResult().getDocuments().get(task.getResult().size()-1);

                    }
                    changeStateOfProgressBar();


                    mRecyclerViewAdapter.notifyDataSetChanged();






                }

            }

        });
    }
    private void initRecyclerView() {
        if (mRecyclerViewAdapter==null){
            mRecyclerViewAdapter=new SearchRecyclerViewAdapter((MainActivity)getContext(),mUserList);
        }


        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        changeStateOfProgressBar();

    }

    @Override
    public void onRefresh() {
        getFollowingUsers();
        mSwipeRefreshLayout.setRefreshing(false);

    }
    private void changeStateOfProgressBar(){
        if (mProgressBar.getVisibility()==View.INVISIBLE){
            mProgressBar.setVisibility(View.VISIBLE);
        }else {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }
}
