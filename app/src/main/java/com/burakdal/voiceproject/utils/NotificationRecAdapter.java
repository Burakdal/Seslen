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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.burakdal.voiceproject.IMainActivity;
import com.burakdal.voiceproject.R;
import com.burakdal.voiceproject.models.Comment;
import com.burakdal.voiceproject.models.InstantMessage;
import com.burakdal.voiceproject.models.NotificationObject;
import com.burakdal.voiceproject.models.Post;
import com.burakdal.voiceproject.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
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

public class NotificationRecAdapter extends RecyclerView.Adapter<NotificationRecAdapter.ViewHolder> {
    private static final String TAG="NotificationRecAdapter";

    private IMainActivity mInterface;



    private Context mContext;


    private ArrayList<NotificationObject> notlist=new ArrayList<>();

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mInterface=(IMainActivity)mContext;
    }

    public NotificationRecAdapter(Context context, ArrayList<NotificationObject> postlist) {

        mContext = context;
        notlist=postlist;


    }





    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comment_card,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        RequestOptions requestOptions=new RequestOptions().placeholder(R.drawable.my_profile);

        NotificationObject not=notlist.get(i);
        if (not!=null){
            viewHolder.mUsername.setText(not.getSender().getName());
            viewHolder.mNotMessage.setText(" "+not.getMessage());
            Glide.with(mContext).load(not.getSender().getProfileImage()).apply(requestOptions).into(viewHolder.mUserImage);
            viewHolder.mUserImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG,"CLİCKED");
                }
            });
            viewHolder.mUsername.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG,"CLİCKED");

                }
            });

        }




    }

    @Override
    public int getItemCount() {
        return notlist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView mUserImage;
        TextView mNotMessage;
        TextView mUsername;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mUserImage = itemView.findViewById(R.id.user_image);
            this.mNotMessage = itemView.findViewById(R.id.comment_message);
            this.mUsername = itemView.findViewById(R.id.username);

        }
    }
}
