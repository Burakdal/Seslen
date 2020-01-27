package com.burakdal.voiceproject.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.burakdal.voiceproject.R;
import com.burakdal.voiceproject.SelectLocationFragment;
import com.google.android.libraries.places.api.model.Place;

import java.util.ArrayList;

public class SelectLocationAdapter extends RecyclerView.Adapter<SelectLocationAdapter.ViewHolder> {
    private final String TAG="SelectLocationAdapter";
    private ArrayList<Place> mPlaceArrayList=new ArrayList<>();
    private Context mContex;
    private SelectLocationFragment mFragment;


    public interface ISelectLocation{
        void getPlace(Place place);
    }
    public interface ISelectLocationClose{
        void closeDialog();
    }




    private ISelectLocationClose mInterfaceClose;
    private ISelectLocation mInterfaceLocation;


    public SelectLocationAdapter(ArrayList<Place> places, Context contex,ISelectLocationClose closeListener,ISelectLocation locationListener) {
        mPlaceArrayList = places;
        mContex = contex;
        mInterfaceClose=closeListener;
        mInterfaceLocation=locationListener;

    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.location_card,viewGroup,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final Place place=mPlaceArrayList.get(i);
        Log.d(TAG,"place name: "+place.getName());
        Log.d(TAG,"place id: "+place.getId());
        Log.d(TAG,"place address: "+place.getLatLng());
        viewHolder.placeName.setText(place.getName());
        viewHolder.placeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInterfaceLocation.getPlace(place);
                mInterfaceClose.closeDialog();
                Toast.makeText(mContex,"location selected",Toast.LENGTH_SHORT).show();



            }
        });

    }

    @Override
    public int getItemCount() {
        Log.d(TAG,"list size: "+mPlaceArrayList.size());
        return mPlaceArrayList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView placeName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.placeName = itemView.findViewById(R.id.place_name);
        }
    }
}
