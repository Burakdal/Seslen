package com.burakdal.voiceproject.utils;

import android.content.Context;
import android.support.annotation.NonNull;
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

import java.util.ArrayList;

public class SearchRecyclerViewAdapter extends RecyclerView.Adapter<SearchRecyclerViewAdapter.ViewHolder> {
    private  final static String TAG="SearchRecyclerView";
    private Context mContext;
    private ArrayList<User> mUsers;
    private IMainActivity mInterface;

    public SearchRecyclerViewAdapter(Context context, ArrayList<User> users) {
        mContext = context;
        mUsers = users;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mInterface=(IMainActivity)mContext;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_view_profile,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        RequestOptions requestOptions=new RequestOptions().placeholder(R.drawable.my_profile);

        final User user=mUsers.get(i);
        if (user != null) {

            Glide.with(mContext).load(user.getProfileImage()).apply(requestOptions).into(viewHolder.mUserProfileImage);
            viewHolder.mUsername.setText(user.getName());
            viewHolder.mUserProfileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext,"clicked",Toast.LENGTH_SHORT).show();
                    mInterface.inflateViewProfile(user);

                }
            });

        }
        else{
            Log.d(TAG,"post is null");
        }
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView mUserProfileImage;
        TextView mUsername;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mUserProfileImage = itemView.findViewById(R.id.user_image);
            this.mUsername = itemView.findViewById(R.id.username_s);
        }
    }
    public void setFilter(ArrayList<User> filterList){
        mUsers=new ArrayList<>();
        mUsers.addAll(filterList);
        notifyDataSetChanged();

    }



}
