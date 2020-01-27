package com.burakdal.voiceproject;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.burakdal.voiceproject.models.ClusterMarker;
import com.burakdal.voiceproject.models.Post;
import com.burakdal.voiceproject.models.PostLocations;
import com.burakdal.voiceproject.models.User;
import com.burakdal.voiceproject.utils.ClusterManagerRenderer;
import com.burakdal.voiceproject.utils.CustomInfoWindowAdapter;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.maps.android.clustering.ClusterManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class SearchFragment extends Fragment implements OnMapReadyCallback {
    private static final float DEFAULT_ZOOM = 15f;
    private final static String TAG = "SearchFragment";
    private final int PLACE_PICKER_REQUEST = 1;
    private Toolbar mToolbar;
    private AppCompatActivity mActivity;
    private SearchView mSearchView;
    private ArrayList<User> mUsers = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private Button mMapBtn;
    private Context mContext;
    private String mSearchedWord;
    private ArrayList<PostLocations> mPostLocations = new ArrayList<>();
    //MAP
    private MapView mMapView;
    private GeoPoint mUserGeoPoint;
    private FirebaseStorage mStorage;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleMap mGoogleMap;
    private LatLngBounds mMapBounds;
    private ClusterManager mClusterManager;
    private ClusterManagerRenderer mClusterManagerRenderer;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private boolean INFO_STATE=false;
    private IMainActivity mInterface;
    private MainActivity mMainActivity;


    private ImageView mPlacePicker;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mInterface=(IMainActivity)context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        Log.d(TAG,"oncreate in tablayout");


        getLastKnownLocation();
        getPostLocations();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment, container, false);

//        mRecyclerView=(RecyclerView)view.findViewById(R.id.searchRecycler);


//        mPlacePicker=(ImageView)view.findViewById(R.id.placePickerObj);
//        mPlacePicker.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
//
//                if (getActivity()!=null){
//                    try {
//                        startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
//                    } catch (GooglePlayServicesRepairableException e) {
//                        e.printStackTrace();
//                    } catch (GooglePlayServicesNotAvailableException e) {
//                        e.printStackTrace();
//                    }
//                }else{
//                    Toast.makeText(getActivity(),"getActivity() is null",Toast.LENGTH_SHORT).show();
//                }
//
//            }
//        });

        mContext = getContext();



        //MAP
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mMapView = (MapView) view.findViewById(R.id.map_toolbar);
        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);





        return view;
    }



//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == PLACE_PICKER_REQUEST) {
//            if (resultCode == RESULT_OK) {
//
//                Place place = PlacePicker.getPlace(getContext(),data);
//
//                String toastMsg = String.format("Place: %s", place.getName());
//                Toast.makeText(getActivity(), toastMsg, Toast.LENGTH_LONG).show();
//            }
//        }
//    }

    private void geoLocate(String s) {
        Geocoder geocoder=new Geocoder(getActivity());
        List<Address> list=new ArrayList<>();

        try {
            list=geocoder.getFromLocationName(s,1);

        } catch (IOException e) {
            e.printStackTrace();
        }
        if(list.size()>0){
            Address address=list.get(0);

            Log.d(TAG,"ADDRESS: "+address.toString());
            moveCamera(new LatLng(address.getLatitude(),address.getLongitude()),DEFAULT_ZOOM,address.getAddressLine(0));
        }
    }


    private void getPostLocations() {
        CollectionReference ref = FirebaseFirestore.getInstance().collection("postLocations");
        ref.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    mPostLocations.clear();
                    mPostLocations = new ArrayList<>();

                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                        PostLocations locations = snapshot.toObject(PostLocations.class);
                        Log.d(TAG, "user_id: " + locations.getUser().getUser_id());
                        Log.d(TAG, "post_id: " + locations.getPost().getPost_id());
                        Log.d(TAG, "latitude: " + locations.getLocation().getLatitude());
                        Log.d(TAG, "longitude: " + locations.getLocation().getLongitude());


                        mPostLocations.add(locations);
                    }
                    Log.d(TAG, "list size: " + mPostLocations.size());
                }
            }
        });

    }
    private void moveCamera(LatLng latLng, float zoom, String title){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if(!title.equals("My Location")){
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mGoogleMap.addMarker(options);
        }
        hideSoftKeyboard();


    }

    private void addMarkers() {
        if (mGoogleMap != null) {

            if (mClusterManager == null) {
                mClusterManager = new ClusterManager<ClusterMarker>(getActivity().getApplicationContext(), mGoogleMap);
            }
            if (mClusterManagerRenderer == null) {
                mClusterManagerRenderer = new ClusterManagerRenderer(
                        getActivity(),
                        mGoogleMap,
                        mClusterManager
                );
                mClusterManager.setRenderer(mClusterManagerRenderer);


                mGoogleMap.setOnInfoWindowClickListener(mClusterManager);
                mGoogleMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
                mGoogleMap.setOnMarkerClickListener(mClusterManager);
                mGoogleMap.setOnCameraIdleListener(mClusterManager);


            }
            int count=0;
            for (PostLocations postLocation : mPostLocations) {
                count++;
                Log.d(TAG, "addMapMarkers: location: " + postLocation.getLocation());
                try {
                    String snippet = "this is :"+count;

                    String avatar = "";
                    try {
                        avatar = postLocation.getPost().getImage();
                        Log.d(TAG, "image url" + postLocation.getPost().getImage());
                    } catch (NumberFormatException e) {
                        Log.d(TAG, "addMapMarkers: no avatar for " + postLocation.getPost().getTitle() + ", setting default.");
                    }
                    ClusterMarker newClusterMarker = new ClusterMarker(
                            new LatLng(postLocation.getLocation().getLatitude(), postLocation.getLocation().getLongitude()),
                            postLocation.getPost().getTitle(),
                            snippet,
                            avatar,
                            postLocation.getPost(),
                            postLocation.getUser()
                    );

                    mClusterManager.addItem(newClusterMarker);

                    mClusterMarkers.add(newClusterMarker);


                } catch (NullPointerException e) {
                    Log.e(TAG, "addMapMarkers: NullPointerException: " + e.getMessage());
                }

            }
            mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<ClusterMarker>() {
                @Override
                public boolean onClusterItemClick(ClusterMarker clusterMarker) {


                    Marker marker=mClusterManagerRenderer.getMarker(clusterMarker);




                    if (INFO_STATE){

                        marker.hideInfoWindow();
                        INFO_STATE=false;
                        return true;


                    }else {
                        marker.showInfoWindow();

                        INFO_STATE=true;
                        return false;


                    }


                }


            });
            mClusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<ClusterMarker>() {
                @Override
                public void onClusterItemInfoWindowClick(ClusterMarker clusterMarker) {
                    Post post=clusterMarker.getPost();
                    mInterface.inflateViewPostFragment(post);
                }


            });
            mClusterManager.onCameraIdle();

            mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(new CustomInfoWindowAdapter(getActivity(),mClusterMarkers));

            mClusterManager.cluster();


            setCameraView();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling

            return;
        }


        mGoogleMap = googleMap;


        addMarkers();


    }


    private void getLastKnownLocation() {


        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {

                Location location = task.getResult();
                mUserGeoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
            }
        });


    }
    private void setCameraView(){
        double bottomBoundary=mUserGeoPoint.getLatitude()-.1;
        double leftBoundary=mUserGeoPoint.getLongitude()-.1;
        double topBoundary=mUserGeoPoint.getLatitude()+.1;
        double rightBoundary=mUserGeoPoint.getLongitude()+.1;

        mMapBounds=new LatLngBounds(
                new LatLng(bottomBoundary,leftBoundary),
                new LatLng(topBoundary,rightBoundary)
        );

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBounds,0));





    }

    private void hideSoftKeyboard(){
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);

    }
    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }
    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


    //    private void setFilteredList(final String text){
//        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("users");
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()){
//                    for (DataSnapshot snapshot:dataSnapshot.getChildren()){
//                        User user=snapshot.getValue(User.class);
//                        if (user.getName().contains(text)){
//                            mUsers.add(user);
//                        }
//
//
//                    }
//                    Log.d(TAG,"list size: "+mUsers.size());
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }


}
