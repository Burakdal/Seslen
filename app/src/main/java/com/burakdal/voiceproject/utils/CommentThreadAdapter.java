package com.burakdal.voiceproject.utils;


import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.burakdal.voiceproject.R;
import com.burakdal.voiceproject.models.Comment;
import com.burakdal.voiceproject.models.Post;
import com.burakdal.voiceproject.models.User;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class CommentThreadAdapter extends BaseAdapter {
    private final static String TAG="CommentThreadAdapter";
    private String mPostId;
    private Context mContext;
    private Activity mActivity;
    private ArrayList<QueryDocumentSnapshot> mSnapshotList;
    private Query mDatabaseReference;
    private EventListener<QuerySnapshot> mListener=new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
            for(DocumentChange snapshot:queryDocumentSnapshots.getDocumentChanges()){

                if (snapshot.getType()==DocumentChange.Type.ADDED){
                    mSnapshotList.add(snapshot.getDocument());
                }



            }
            notifyDataSetChanged();
        }
    };

    public CommentThreadAdapter(Activity activity, String postId,Context context) {
        mPostId=postId;
        mActivity = activity;
        mContext=context;

        mDatabaseReference=FirebaseFirestore.getInstance().collection("comments")
                .whereEqualTo("postId",mPostId);
        mDatabaseReference.addSnapshotListener(mListener);







        mSnapshotList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mSnapshotList.size();
    }

    @Override
    public Comment getItem(int position) {
        return mSnapshotList.get(position).toObject(Comment.class);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView==null){
            LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.comment_card, parent, false);

            final ViewHolder holder = new ViewHolder();
            holder.username = (TextView) convertView.findViewById(R.id.username);
            holder.commentMessage = (TextView) convertView.findViewById(R.id.comment_message);
            holder.userImage = (ImageView) convertView.findViewById(R.id.user_image);
            convertView.setTag(holder);
        }
        final Comment comment=getItem(position);
        final ViewHolder holder = (ViewHolder) convertView.getTag();
        holder.username.setText(comment.getCommenter().getName());
        holder.commentMessage.setText(comment.getMessage());
        RequestOptions requestOptions=new RequestOptions().placeholder(R.drawable.my_profile);
        Glide.with(mContext).load(comment.getCommenter().getProfileImage()).apply(requestOptions).into(holder.userImage);


        return convertView;
    }
    private static class ViewHolder{
        TextView username;
        TextView commentMessage;
        ImageView userImage;
    }
}