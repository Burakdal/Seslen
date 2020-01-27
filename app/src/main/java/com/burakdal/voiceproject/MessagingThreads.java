package com.burakdal.voiceproject;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.PluralsRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.burakdal.voiceproject.models.User;
import com.burakdal.voiceproject.utils.MessagingThreadsAdapter;
import com.burakdal.voiceproject.utils.SearchRecyclerViewAdapter;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MessagingThreads extends Fragment {
    private final static String TAG="MessagingThreads";
    private RecyclerView mRecyclerView;
    private MessagingThreadsAdapter mAdapter;
    private IMainActivity mInterface;
    private User mCurrentUser;
    private ArrayList<User> mUserList=new ArrayList<>();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.messaging_thread_layout,container,false);
        getCurrentUser();
        mRecyclerView=(RecyclerView)view.findViewById(R.id.messaging_thread_rec);



        return view;
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mInterface=(IMainActivity)context;
    }
    private void getCurrentUser(){
        mCurrentUser= mInterface.getUser();
    }
    private void getUserList(){
        CollectionReference ref=FirebaseFirestore.getInstance().collection("usersMessageThreads").document(mCurrentUser.getUser_id()).collection("listOfUsers");
        ref.addSnapshotListener(new com.google.firebase.firestore.EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                for(QueryDocumentSnapshot snapshots:queryDocumentSnapshots){
                    Log.d(TAG,"user is got");
                    User user=snapshots.toObject(User.class);

                    Log.d(TAG,"user name: "+user.getName());
                    mUserList.add(user);
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }
    private void initRecyclerView() {
        if (mAdapter==null){
            mAdapter=new MessagingThreadsAdapter((MainActivity)getContext(),mUserList);
        }


        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    public void onStart() {
        super.onStart();
        initRecyclerView();
        getUserList();
    }
}
