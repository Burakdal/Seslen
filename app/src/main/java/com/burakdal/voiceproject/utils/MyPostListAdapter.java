//package com.burakdal.voiceproject.utils;
//
//import android.app.Activity;
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.ImageButton;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import com.burakdal.voiceproject.R;
//import com.burakdal.voiceproject.UniversalImageLoader;
//import com.burakdal.voiceproject.models.Post;
//import com.google.firebase.database.ChildEventListener;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.Query;
//
//import java.util.ArrayList;
//
//public class MyPostListAdapter extends BaseAdapter {
//
//
//    private Activity mActivity;
//    private DatabaseReference mDatabaseReference;
//
//    private ArrayList<DataSnapshot> mSnapshotList;
//
//    private ChildEventListener mListener = new ChildEventListener() {
//        @Override
//        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//
//            mSnapshotList.add(dataSnapshot);
//            notifyDataSetChanged();
//
//        }
//
//        @Override
//        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//        }
//
//        @Override
//        public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//        }
//
//        @Override
//        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//        }
//
//        @Override
//        public void onCancelled(DatabaseError databaseError) {
//
//        }
//    };
//
//    public MyPostListAdapter(Activity activity, DatabaseReference ref,String uid) {
//
//        mActivity = activity;
//
//        // common error: typo in the db location. Needs to match what's in MainChatActivity.
//        mDatabaseReference = ref.child("posts");
//        Query queryCurrentUser=mDatabaseReference.orderByChild("user_id").equalTo(uid);
//        queryCurrentUser.addChildEventListener(mListener);
////        mDatabaseReference.addChildEventListener(mListener);
//
//        mSnapshotList = new ArrayList<>();
//    }
//
//    private static class ViewHolder{
//        TextView mTitle;
//        TextView mDescription,mCommentCount,mLikeCount;
//        SquareImageView mPostImage;
//        LinearLayout.LayoutParams params;
//        ImageButton mPlayButton,mLikeBtn,mCommentBtn;
//
//
//    }
//
//
//    @Override
//    public int getCount() {
//        return mSnapshotList.size();
//    }
//
//    @Override
//    public Post getItem(int position) {
//        DataSnapshot snapshot = mSnapshotList.get(position);
//        return snapshot.getValue(Post.class) ;
//    }
//
//    @Override
//    public long getItemId(int position) {
//
//        return 0;
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        if (convertView == null) {
//            LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            convertView = inflater.inflate(R.layout.audio_row, parent, false);
//
//            final ViewHolder holder = new ViewHolder();
//            holder.mTitle = (TextView) convertView.findViewById(R.id.post_title);
//            holder.mPostImage = (SquareImageView) convertView.findViewById(R.id.audio_row_post_img);
//            holder.mCommentBtn = (ImageButton) convertView.findViewById(R.id.comment_btn);
//            holder.mPlayButton=(ImageButton)convertView.findViewById(R.id.row_play);
//            holder.mCommentCount=(TextView)convertView.findViewById(R.id.comment_count);
//            holder.mLikeCount=(TextView)convertView.findViewById(R.id.like_count);
//            holder.mLikeBtn=(ImageButton)convertView.findViewById(R.id.like_btn);
//
//            convertView.setTag(holder);
//
//
//        }
//
//        final Post post=getItem(position);
//        final ViewHolder holder = (ViewHolder) convertView.getTag();
//
//        holder.mTitle.setText(post.getTitle());
//        UniversalImageLoader.setImage(post.getImage(),holder.mPostImage);
//
//        return convertView;
//    }
//}
