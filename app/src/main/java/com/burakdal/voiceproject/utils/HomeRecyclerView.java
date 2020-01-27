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
import com.burakdal.voiceproject.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class HomeRecyclerView extends RecyclerView.Adapter<HomeRecyclerView.ViewHolder> {
    private static final String TAG="HRecyclerViewAdapter";

    private ArrayList<DataSnapshot> mSnapshotList=new ArrayList<>();


    private Context mContext;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mDatabaseReference1;
    private IMainActivity mInterface;

    private ArrayList<Post> postList=new ArrayList<>();

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mInterface=(IMainActivity)mContext;
    }

    public HomeRecyclerView(Context context, ArrayList<Post> postlist) {

        mContext = context;
        postList=postlist;


    }





    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_view_home,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        RequestOptions requestOptions=new RequestOptions().placeholder(R.drawable.my_profile);

        Post post=postList.get(i);
        if (post != null) {
            final int pos=i;
            com.google.firebase.firestore.Query query=FirebaseFirestore.getInstance()
                    .collection("users")
                    .whereEqualTo("user_id",post.getUser_id());
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()){

                        for (QueryDocumentSnapshot snapshot:task.getResult()){
                            User user=snapshot.toObject(User.class);
                            viewHolder.mUsername.setText(user.getName());
                        }

                    }
                }
            });
//            Query reference=FirebaseDatabase.getInstance().getReference().child("users").orderByChild("user_id").equalTo(post.getUser_id());
//            reference.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    if (dataSnapshot.exists()){
//                        for (DataSnapshot snapshot:dataSnapshot.getChildren()){
//                            User user=snapshot.getValue(User.class);
//                            viewHolder.mUsername.setText(user.getName());
//                        }
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });
            Glide.with(mContext).load(post.getImage()).apply(requestOptions).into(viewHolder.mPostImage);
            viewHolder.mPostTitle.setText(post.getTitle());
            viewHolder.mPostImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext,"clicked",Toast.LENGTH_SHORT).show();
                    mInterface.inflateViewPostFragment(postList.get(pos));
                }
            });

        }
        else{
            Log.d(TAG,"post is null");
        }



    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView mPostImage;
        TextView mPostTitle;
        TextView mUsername;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mPostImage = itemView.findViewById(R.id.post_image_home);
            this.mPostTitle = itemView.findViewById(R.id.post_title_home);
            this.mUsername = itemView.findViewById(R.id.username_home);

        }
    }
}
