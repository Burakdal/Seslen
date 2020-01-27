package com.burakdal.voiceproject.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.burakdal.voiceproject.IMainActivity;
import com.burakdal.voiceproject.R;
import com.burakdal.voiceproject.models.Post;
import com.burakdal.voiceproject.models.PostLocations;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.PropertyResourceBundle;

public class MyProfileRecyclerViewAdapter extends RecyclerView.Adapter<MyProfileRecyclerViewAdapter.ViewHolder> {
    private static final String TAG="RecyclerViewAdapter";



    private String mUserId;
    private Context mContext;

    private IMainActivity mInterface;
    private ArrayList<Post> mPostArrayList=new ArrayList<>();
    private CollectionReference mRef;


    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mInterface=(IMainActivity)mContext;
    }

    public MyProfileRecyclerViewAdapter(String userId, Context context,ArrayList<Post> postlist) {
        mUserId=userId;
        mContext = context;
        mPostArrayList=postlist;

    }





    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_view,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        RequestOptions requestOptions=new RequestOptions().placeholder(R.drawable.my_profile);
        Log.d(TAG,"onBind:POST ");


        Post post=mPostArrayList.get(i);
        Log.d(TAG,"onBind:POST "+post.getTitle());
        if (post != null) {
            final int pos=i;
            Glide.with(mContext).load(post.getImage()).apply(requestOptions).into(viewHolder.mPostImage);
            viewHolder.mPostTitle.setText(post.getTitle());
            viewHolder.mPostImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext,"clicked",Toast.LENGTH_SHORT).show();
                    mInterface.inflateViewPostFragment(mPostArrayList.get(pos));
                }
            });

        }
        else{
            Log.d(TAG,"post is null");
        }



    }

    @Override
    public int getItemCount() {
        Log.d(TAG,"size in override :"+mPostArrayList.size());
        return mPostArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView mPostImage;
        TextView mPostTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mPostImage = itemView.findViewById(R.id.post_image);
            this.mPostTitle = itemView.findViewById(R.id.post_title);
        }
    }
}
