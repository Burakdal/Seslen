package com.burakdal.voiceproject;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import com.burakdal.voiceproject.models.Post;
import com.burakdal.voiceproject.models.PostLocations;
import com.burakdal.voiceproject.utils.HomeRecyclerView;
import com.burakdal.voiceproject.utils.MyProfileRecyclerViewAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import retrofit2.http.GET;

import static com.burakdal.voiceproject.models.Constants.ERROR_DIALOG_REQUEST;
import static com.burakdal.voiceproject.models.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.burakdal.voiceproject.models.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

public class Home extends Fragment{
    final static String TAG="HOME";
    private DatabaseReference mDatabaseReference;
    private RecyclerView mRecyclerView;
    private HomeRecyclerView mAdapter;
    private boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private ArrayList<Post> mPostList=new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private HomeRecyclerView mHomeRecyclerViewAdapter;
    private DocumentSnapshot mLastQueriedDocument;
    private DocumentSnapshot mLastQueriedDocumentForScroll;

    private ProgressBar mProgressBar;
    private GeoPoint mUserGeoPoint;
    private android.support.v7.widget.Toolbar mToolbar;
    private boolean PAGINATE_STATE=false;
    private boolean IS_SCROLLING=false;
    private int currentItems,totalItems,scrollOutItems;
    private LinearLayoutManager mLayoutManager;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.home_fragment,container,false);
        mRecyclerView=(RecyclerView)view.findViewById(R.id.homeRecy);
        mProgressBar=(ProgressBar)view.findViewById(R.id.home_scroll_progress_bar);
        mSwipeRefreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.home_swipe);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG,"refreshing");
                getWholePost();


                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        mDatabaseReference=FirebaseDatabase.getInstance().getReference();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());



        return view;
    }

    private void getWholePost(){
        Log.d(TAG,"in getwholepost");
        Query ref=null;
        if (mLastQueriedDocument!=null){
            PAGINATE_STATE=true;
            Log.d(TAG,"in getwholepost1");
            ref=FirebaseFirestore.getInstance()
                    .collection("postLocations")
                    .orderBy("timeStamp",Query.Direction.ASCENDING).startAfter(mLastQueriedDocument).limit(3);

        }else {
            Log.d(TAG,"in getwholepost2");
            ref=FirebaseFirestore.getInstance().collection("postLocations").orderBy("timeStamp",Query.Direction.DESCENDING).limit(10);
        }
        Log.d(TAG,"in getwholepost3");

        ref.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int count=0;

                for (DocumentSnapshot doc:queryDocumentSnapshots){
                    PostLocations postLocation=doc.toObject(PostLocations.class);
                    Log.d(TAG,"LAST POST: "+postLocation.getPost().getPlaceId());
                    double bottomBoundary=mUserGeoPoint.getLatitude()-.01;
                    double leftBoundary=mUserGeoPoint.getLongitude()-.01;
                    double topBoundary=mUserGeoPoint.getLatitude()+.01;
                    double rightBoundary=mUserGeoPoint.getLongitude()+.01;
                    if (postLocation.getLocation().getLatitude()>bottomBoundary && postLocation.getLocation().getLatitude()<topBoundary && postLocation.getLocation().getLongitude()>leftBoundary && postLocation.getLocation().getLongitude()<rightBoundary){
                        count++;
                        Log.d(TAG,"POST "+count);
                        if (PAGINATE_STATE){
                            mPostList.add(0,postLocation.getPost());
                        }else{
                            mPostList.add(postLocation.getPost());

                        }

                    }

                }
                if (queryDocumentSnapshots.size()!=0){

                    mLastQueriedDocument=queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size()-(queryDocumentSnapshots.size()-1)-1);
                    mLastQueriedDocumentForScroll=queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size()-1);
                    Log.d(TAG,"mLastQuery: "+mLastQueriedDocument.getId());

                }
                mHomeRecyclerViewAdapter.notifyDataSetChanged();
            }
        });


    }
    private void initRecyclerView() {
        Log.d(TAG,"init rec");

        if (mHomeRecyclerViewAdapter==null){
            Log.d(TAG,"init rec1");

            mHomeRecyclerViewAdapter=new HomeRecyclerView((MainActivity)getContext(),mPostList);

        }

        Log.d(TAG,"init rec3");


        mLayoutManager=new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mHomeRecyclerViewAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState==AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                    IS_SCROLLING=true;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                currentItems=mLayoutManager.getChildCount();
                totalItems=mLayoutManager.getItemCount();
                scrollOutItems=mLayoutManager.findFirstVisibleItemPosition();
                if (IS_SCROLLING && (currentItems+scrollOutItems==totalItems)){
                    IS_SCROLLING=false;
                    fetchData();
                }
            }
        });


    }

    private void fetchData() {
        mProgressBar.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Query query=FirebaseFirestore.getInstance().collection("postLocations")
                        .orderBy("timeStamp",Query.Direction.DESCENDING).startAfter(mLastQueriedDocumentForScroll).limit(3);
                query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        int count=0;

                        for (DocumentSnapshot doc:queryDocumentSnapshots){
                            PostLocations postLocation=doc.toObject(PostLocations.class);
                            Log.d(TAG,"LAST POST: "+postLocation.getPost().getPlaceId());
                            double bottomBoundary=mUserGeoPoint.getLatitude()-.01;
                            double leftBoundary=mUserGeoPoint.getLongitude()-.01;
                            double topBoundary=mUserGeoPoint.getLatitude()+.01;
                            double rightBoundary=mUserGeoPoint.getLongitude()+.01;
                            if (postLocation.getLocation().getLatitude()>bottomBoundary && postLocation.getLocation().getLatitude()<topBoundary && postLocation.getLocation().getLongitude()>leftBoundary && postLocation.getLocation().getLongitude()<rightBoundary){
                                count++;
                                Log.d(TAG,"POST "+count);

                                mPostList.add(postLocation.getPost());

                            }

                        }
                        if (queryDocumentSnapshots.size()!=0){

                            mLastQueriedDocumentForScroll=queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size()-1);
                            Log.d(TAG,"mLastQueryForScroll: "+mLastQueriedDocumentForScroll.getId());

                        }
                        mHomeRecyclerViewAdapter.notifyDataSetChanged();
                        mProgressBar.setVisibility(View.GONE);


                    }
                });
            }
        }, 5000);
    }

    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }
    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getActivity().getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }
    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity());

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance()
                    .getErrorDialog(getActivity(), available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(getActivity(), "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(mLocationPermissionGranted){
                    initRecyclerView();
                    getWholePost();


                }
                else{
                    getLocationPermission();
                }
            }

        }


    }
    private void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling

            return;
        }
        mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location=task.getResult();
                if (location!=null){
                    mUserGeoPoint=new GeoPoint(location.getLatitude(),location.getLongitude());

                }else {
                    Log.d(TAG,"Location: null");
                }








            }
        });
    }
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
//            getChatrooms();
            initRecyclerView();

            getWholePost();
            getLastKnownLocation();

        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if(checkMapServices()){
            if(mLocationPermissionGranted){
                initRecyclerView();

                getWholePost();


            }
            else{
                getLocationPermission();
            }
        }
    }
}
