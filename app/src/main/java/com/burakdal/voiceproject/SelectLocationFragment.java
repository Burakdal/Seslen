package com.burakdal.voiceproject;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.burakdal.voiceproject.utils.SelectLocationAdapter;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelectLocationFragment extends DialogFragment implements SelectLocationAdapter.ISelectLocationClose {
    private final String TAG="SelectLocationFragment";
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private SelectLocationAdapter mRecAdapter;
    private ArrayList<Place> mPlaces=new ArrayList<>();
    private PlacesClient mPlacesClient;
    private SelectLocationAdapter.ISelectLocationClose mCloseListener;
    private SelectLocationAdapter.ISelectLocation mSelectLocationListener;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.select_location_fragment,container,false);
        mToolbar=view.findViewById(R.id.select_location_toolbar);
        initToolbar();
        mRecyclerView=view.findViewById(R.id.select_location_recView);
        Places.initialize(getActivity(), getString(R.string.google_maps_key));
        mPlacesClient = Places.createClient(getActivity());
        getPlaces();







        return view;
    }
    private void getPlaces(){
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME,Place.Field.ID);
        FindCurrentPlaceRequest request =
                FindCurrentPlaceRequest.builder(placeFields).build();
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<FindCurrentPlaceResponse> placeResponse = mPlacesClient.findCurrentPlace(request);
        placeResponse.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
            @Override
            public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                if (task.isSuccessful()){
                    FindCurrentPlaceResponse response = task.getResult();
                    int count=0;
                    for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                        count++;
                        Log.i(TAG, String.format("%d-Place '%s' has id: %s likelihood: %f",count,
                                placeLikelihood.getPlace().getName(),
                                placeLikelihood.getPlace().getId(),
                                placeLikelihood.getLikelihood()));
                        mPlaces.add(placeLikelihood.getPlace());

                    }


                } else {
                    Exception exception = task.getException();
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                    }
                }
                mCloseListener=SelectLocationFragment.this;
                mSelectLocationListener=(SelectLocationAdapter.ISelectLocation) getTargetFragment();
                mRecAdapter=new SelectLocationAdapter(mPlaces,getActivity(),mCloseListener,mSelectLocationListener);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                mRecyclerView.setAdapter(mRecAdapter);
            }
        });
    }
    private void initToolbar(){
        mToolbar.setTitle("SelectLocation");
        mToolbar.inflateMenu(R.menu.post_fragment_layout_menu);
        MenuItem menuItem=mToolbar.getMenu().findItem(R.id.close_icon_post_fragment_toolbar);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                getDialog().dismiss();
                return false;

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }

    @Override
    public void closeDialog() {
        getDialog().dismiss();
    }
}


