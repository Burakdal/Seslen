package com.burakdal.voiceproject;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;

import com.burakdal.voiceproject.models.FollowObject;
import com.burakdal.voiceproject.models.Post;
import com.burakdal.voiceproject.models.PostLocations;
import com.burakdal.voiceproject.utils.HomeRecyclerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class HomeFollowings extends Fragment {
    private final String TAG = "HomeFollowings";
    private RecyclerView mRecyclerViewForFollowings;
    private ArrayList<String> mFollowingList = new ArrayList<>();


    private DatabaseReference mDatabaseReference;

    private ArrayList<Post> mPostListForFollowings = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayoutForFollowings;
    private HomeRecyclerView mHomeRecyclerViewAdapterForFollowings;
    private DocumentSnapshot mLastQueriedDocumentForFollowings;
    private DocumentSnapshot mLastQueriedDocumentForScrollForFollowings;

    private ProgressBar mProgressBarForFollowings;
    private android.support.v7.widget.Toolbar mToolbar;
    private boolean PAGINATE_STATE_FOR_FOLLOWINGS = false;
    private boolean IS_SCROLLING_FOR_FOLLOWINGS = false;
    private int currentItemsForFollowings, totalItemsFollowings, scrollOutItemsForFollowings;
    private LinearLayoutManager mLayoutManagerForFollowings;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_followings_layout, container, false);
        mRecyclerViewForFollowings = (RecyclerView) view.findViewById(R.id.home_followings_recy);
        mProgressBarForFollowings=(ProgressBar)view.findViewById(R.id.home_following_scroll_progress_bar);
        mSwipeRefreshLayoutForFollowings=(SwipeRefreshLayout)view.findViewById(R.id.home_followings_swipe);

        mSwipeRefreshLayoutForFollowings.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG,"refreshing");
                getWholePost();


                mSwipeRefreshLayoutForFollowings.setRefreshing(false);
            }
        });
        mDatabaseReference=FirebaseDatabase.getInstance().getReference();
        getFollowingList();

        return view;

    }

    private void getFollowingList() {
        Log.d(TAG, "get FollowingList");
        Query query = FirebaseFirestore.getInstance().collection("followings")
                .whereEqualTo("follower_id", FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (final DocumentSnapshot snapshot : task.getResult()) {
                        FollowObject obj = snapshot.toObject(FollowObject.class);
                        Log.d(TAG, "following user id" + obj.getFollowed_id());

                        mFollowingList.add(obj.getFollowed_id());
                    }

                    initRecyclerView();
                    getWholePost();


                }
            }
        });


    }

    private void fetchData() {
        mProgressBarForFollowings.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Query query = FirebaseFirestore.getInstance().collection("postLocations")
                        .orderBy("timeStamp", Query.Direction.DESCENDING).startAfter(mLastQueriedDocumentForScrollForFollowings).limit(3);
                query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        int count = 0;

                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            PostLocations postLocation = doc.toObject(PostLocations.class);
                            String userId=postLocation.getUser().getUser_id();
                            Log.d(TAG,"following list size: "+mFollowingList.size());
                            if (mFollowingList.contains(userId)) {
                                count++;
                                Log.d(TAG, "POST " + count);

                                mPostListForFollowings.add(postLocation.getPost());

                            }

                        }
                        if (queryDocumentSnapshots.size() != 0) {

                            mLastQueriedDocumentForScrollForFollowings = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                            Log.d(TAG, "mLastQueryForScroll: " + mLastQueriedDocumentForScrollForFollowings.getId());

                        }
                        mHomeRecyclerViewAdapterForFollowings.notifyDataSetChanged();
                        mProgressBarForFollowings.setVisibility(View.GONE);


                    }
                });
            }
        }, 5000);
    }

    private void initRecyclerView() {
        Log.d(TAG, "init rec");

        if (mHomeRecyclerViewAdapterForFollowings == null) {
            Log.d(TAG, "init rec1");

            mHomeRecyclerViewAdapterForFollowings = new HomeRecyclerView((MainActivity) getContext(), mPostListForFollowings);

        }

        Log.d(TAG, "init rec3");


        mLayoutManagerForFollowings = new LinearLayoutManager(getActivity());
        mRecyclerViewForFollowings.setLayoutManager(mLayoutManagerForFollowings);
        mRecyclerViewForFollowings.setAdapter(mHomeRecyclerViewAdapterForFollowings);
        mRecyclerViewForFollowings.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    IS_SCROLLING_FOR_FOLLOWINGS = true;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                currentItemsForFollowings = mLayoutManagerForFollowings.getChildCount();
                totalItemsFollowings = mLayoutManagerForFollowings.getItemCount();
                scrollOutItemsForFollowings = mLayoutManagerForFollowings.findFirstVisibleItemPosition();
                if (IS_SCROLLING_FOR_FOLLOWINGS && (currentItemsForFollowings + scrollOutItemsForFollowings == totalItemsFollowings)) {
                    IS_SCROLLING_FOR_FOLLOWINGS = false;
                    fetchData();
                }
            }
        });


    }

    private void getWholePost() {
        Log.d(TAG, "in getwholepost");
        Query ref = null;
        if (mLastQueriedDocumentForFollowings != null) {
            PAGINATE_STATE_FOR_FOLLOWINGS = true;
            Log.d(TAG, "in getwholepost1");
            ref = FirebaseFirestore.getInstance()
                    .collection("postLocations")
                    .orderBy("timeStamp", Query.Direction.ASCENDING).startAfter(mLastQueriedDocumentForFollowings).limit(3);

        } else {
            Log.d(TAG, "in getwholepost2");
            ref = FirebaseFirestore.getInstance().collection("postLocations").orderBy("timeStamp", Query.Direction.DESCENDING).limit(10);
        }
        Log.d(TAG, "in getwholepost3");

        ref.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int count = 0;

                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    PostLocations postLocation = doc.toObject(PostLocations.class);
                    String userId=postLocation.getUser().getUser_id();
                    Log.d(TAG,"user id: "+userId);
                    Log.d(TAG,"following list size: "+mFollowingList.size());

                    if (mFollowingList.contains(userId)) {
                        count++;
                        Log.d(TAG, "the post of followings : " + count);
                        if (PAGINATE_STATE_FOR_FOLLOWINGS) {
                            mPostListForFollowings.add(0, postLocation.getPost());
                        } else {
                            mPostListForFollowings.add(postLocation.getPost());

                        }

                    }

                }
                if (queryDocumentSnapshots.size() != 0) {

                    mLastQueriedDocumentForFollowings = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - (queryDocumentSnapshots.size() - 1) - 1);
                    mLastQueriedDocumentForScrollForFollowings = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                    Log.d(TAG, "mLastQuery: " + mLastQueriedDocumentForFollowings.getId());

                }
                mHomeRecyclerViewAdapterForFollowings.notifyDataSetChanged();
            }
        });


    }
}
