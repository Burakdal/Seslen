package com.burakdal.voiceproject.utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.burakdal.voiceproject.R;
import com.burakdal.voiceproject.models.ClusterMarker;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.PropertyResourceBundle;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter{
    private final String TAG="CustomInfoAdapter";
    private final View mWindow;
    private Context mContext;
    private ArrayList<ClusterMarker> mClusterMarkers;

    public CustomInfoWindowAdapter(Context context,ArrayList<ClusterMarker> markers){
        mContext = context;
        mClusterMarkers=markers;
        mWindow=LayoutInflater.from(context).inflate(R.layout.infowindow,null);
        mWindow.setBackgroundColor(mContext.getResources().getColor(R.color.defaultColor));
    }
    private void renderWindow(Marker marker,View view){
        TextView username=(TextView) view.findViewById(R.id.infowindow_username);

        CircleImageView userImage=(CircleImageView)view.findViewById(R.id.infowindow_userimage);
        ImageView postImage=(ImageView)view.findViewById(R.id.infowindow_postImage);
        ImageButton playBtn=(ImageButton) view.findViewById(R.id.infowindow_play_btn);



        for(ClusterMarker clusterMarker:mClusterMarkers){
            if (marker.getSnippet().equals(clusterMarker.getSnippet())){
                username.setText(clusterMarker.getUser().getName());

                Glide.with(mContext).load(clusterMarker.getImageurl()).into(postImage);
                Glide.with(mContext).load(clusterMarker.getUser().getProfileImage()).into(userImage);
                playBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG,"clicked");
                    }
                });



            }
        }
    }


    @Override
    public View getInfoWindow(Marker marker) {

        renderWindow(marker,mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {


        return null;
    }



}
