package com.burakdal.voiceproject.utils;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.burakdal.voiceproject.IMainActivity;
import com.burakdal.voiceproject.R;
import com.burakdal.voiceproject.models.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.EventListener;

import javax.annotation.Nullable;

public class MessagingThreadsAdapter extends RecyclerView.Adapter<MessagingThreadsAdapter.ViewHolder>  {

    private final static String TAG="MessagingThreadsAdapter";
    private ArrayList<User> mSnapshots;
    private IMainActivity mInterface;
    private Context mContext;



    public MessagingThreadsAdapter(Context context,ArrayList<User> userlist) {
        mContext = context;
        mSnapshots=userlist;




    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.messaging_thread_card,viewGroup,false);

        return new ViewHolder(view);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mInterface=(IMainActivity)mContext;

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final User user=mSnapshots.get(i);
        RequestOptions requestOptions=new RequestOptions().placeholder(R.drawable.my_profile);


        if (user != null) {
            Log.d(TAG,"user is not null");
            Glide.with(mContext).load(user.getProfileImage()).apply(requestOptions).into(viewHolder.mUserImage);
            viewHolder.mUserName.setText(user.getName());
            viewHolder.mLastMessage.setText("The last Message");
            viewHolder.mUserImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext,"clicked",Toast.LENGTH_SHORT).show();
                    mInterface.inflateMainMessageFragment(user);

                }
            });

        }
        else{
            Log.d(TAG,"user is null");
        }

    }

    @Override
    public int getItemCount() {
        return mSnapshots.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView mUserImage;
        TextView mUserName;
        TextView mLastMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mUserImage = itemView.findViewById(R.id.user_image);
            this.mUserName = itemView.findViewById(R.id.user_name);
            this.mLastMessage=itemView.findViewById(R.id.last_message);
        }
    }
}
