package com.burakdal.voiceproject;

import android.content.Context;
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

import com.burakdal.voiceproject.models.NotificationObject;
import com.burakdal.voiceproject.models.PostLocations;
import com.burakdal.voiceproject.utils.HomeRecyclerView;
import com.burakdal.voiceproject.utils.NotificationRecAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class Notifications extends Fragment {
    private final static String TAG="Notifications";


    private RecyclerView mNotListView;

    private IMainActivity mInterface;
    private DocumentSnapshot mLastQueriedDocument;
    private DocumentSnapshot mLastQueriedDocumentForScroll;
    private boolean PAGINATE_STATE=false;
    private boolean IS_SCROLLING=false;
    private int currentItems,totalItems,scrollOutItems;
    private LinearLayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private NotificationRecAdapter mNotificationRecAdapter;
    private ArrayList<NotificationObject> mNots=new ArrayList<>();
    private ProgressBar mProgressBar;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mInterface=(IMainActivity)context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.notifications_fragment,container,false);
        mNotListView=(RecyclerView) view.findViewById(R.id.not_rec);
        mSwipeRefreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.not_swipe);
        mProgressBar=(ProgressBar)view.findViewById(R.id.not_scroll_progress);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG,"refreshing");
                getWholePost();


                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        initRecyclerView();

        getWholePost();
        return view;
    }

    private void getWholePost(){
        Log.d(TAG,"in getwholepost");
        Query ref=null;
        if (mLastQueriedDocument!=null){
            PAGINATE_STATE=true;
            Log.d(TAG,"in getwholepost1");
            ref= FirebaseFirestore.getInstance()
                    .collection("notifications")
                    .document(mInterface.getUser().getUser_id())
                    .collection("nots")
                    .orderBy("timestamp",Query.Direction.ASCENDING).startAfter(mLastQueriedDocument).limit(3);

        }else {
            Log.d(TAG,"in getwholepost2");
            Log.d(TAG,"user id:"+mInterface.getUser().getUser_id());
            ref=FirebaseFirestore.getInstance().collection("notifications").document(mInterface.getUser().getUser_id()).collection("nots").orderBy("timestamp",Query.Direction.DESCENDING).limit(10);
        }
        Log.d(TAG,"in getwholepost3");


        ref.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Log.d(TAG,"size:"+queryDocumentSnapshots.size());
                for (DocumentSnapshot doc:queryDocumentSnapshots){

                    NotificationObject notObj=doc.toObject(NotificationObject.class);
                    Log.d(TAG,"POST");

                    if (PAGINATE_STATE){
                        mNots.add(0,notObj);
                    }else{
                        mNots.add(notObj);

                    }

                }
                if (queryDocumentSnapshots.size()!=0){

                    mLastQueriedDocument=queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size()-(queryDocumentSnapshots.size()-1)-1);
                    mLastQueriedDocumentForScroll=queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size()-1);
                    Log.d(TAG,"mLastQuery: "+mLastQueriedDocument.getId());

                }
                mNotificationRecAdapter.notifyDataSetChanged();
            }
        });


    }


    private void initRecyclerView() {
        Log.d(TAG,"init rec");

        if (mNotificationRecAdapter==null){
            Log.d(TAG,"init rec1");

            mNotificationRecAdapter=new NotificationRecAdapter((MainActivity)getContext(),mNots);

        }

        Log.d(TAG,"init rec3");


        mLayoutManager=new LinearLayoutManager(getActivity());
        mNotListView.setLayoutManager(mLayoutManager);
        mNotListView.setAdapter(mNotificationRecAdapter);
        mNotListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState== AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
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
                Query query=FirebaseFirestore.getInstance().collection("notifications")
                        .document(mInterface.getUser().getUser_id())
                        .collection("nots")
                        .orderBy("timestamp",Query.Direction.DESCENDING).startAfter(mLastQueriedDocumentForScroll).limit(3);
                query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for (DocumentSnapshot doc:queryDocumentSnapshots){
                            NotificationObject notObj=doc.toObject(NotificationObject.class);



                            mNots.add(notObj);



                        }
                        if (queryDocumentSnapshots.size()!=0){

                            mLastQueriedDocumentForScroll=queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size()-1);
                            Log.d(TAG,"mLastQueryForScroll: "+mLastQueriedDocumentForScroll.getId());

                        }
                        mNotificationRecAdapter.notifyDataSetChanged();
                        mProgressBar.setVisibility(View.GONE);


                    }
                });
            }
        }, 5000);
    }


}
