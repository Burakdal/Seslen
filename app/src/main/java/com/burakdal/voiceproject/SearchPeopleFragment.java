package com.burakdal.voiceproject;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.PluralsRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.burakdal.voiceproject.models.HitsList;
import com.burakdal.voiceproject.models.HitsObject;
import com.burakdal.voiceproject.models.Post;
import com.burakdal.voiceproject.models.User;
import com.burakdal.voiceproject.utils.ElasticSearchAPI;
import com.burakdal.voiceproject.utils.HomeRecyclerView;
import com.burakdal.voiceproject.utils.SearchRecyclerViewAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import io.appbase.client.AppbaseClient;
import okhttp3.Call;
import okhttp3.Credentials;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchPeopleFragment extends Fragment {
    private final String TAG="PEOPLE";
    private final static String BASE_URL="http://35.193.249.129//elasticsearch/users/user/";
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private DocumentSnapshot mLastQueriedDocument;
    private SearchRecyclerViewAdapter mRecyclerViewAdapter;
    private ArrayList<User> mUsers=new ArrayList<>();
    private SearchView mSearchView;
    private String mElasticSearchPassword;
    private ArrayList<User> mUsersForSearch;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.search_people_layout,container,false);
        getElasticSearchPassword();
        mRecyclerView=(RecyclerView) view.findViewById(R.id.search_people_rec);
        initRecyclerView();
        getWholeUsers();
        mSearchView=(SearchView)view.findViewById(R.id.search_view_people);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                mUsers=new ArrayList<>();
                Retrofit retrofit=new Retrofit.Builder().baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create()).build();
                ElasticSearchAPI searchAPI=retrofit.create(ElasticSearchAPI.class);
                HashMap<String,String> headerMap=new HashMap<String,String>();
                headerMap.put("Authorization",Credentials.basic("user",mElasticSearchPassword));


                String searchString="";
                if (!s.equals("")){
                    searchString=searchString+s;

                }


                retrofit2.Call<HitsObject> call=searchAPI.search(headerMap,"AND",searchString+"*");

                call.enqueue(new Callback<HitsObject>() {
                    @Override
                    public void onResponse(retrofit2.Call<HitsObject> call, Response<HitsObject> response) {
                        HitsList hitsList=new HitsList();
                        String jsonResponse="";
                        try{
                            Log.d(TAG, "onResponse: server response: " + response.toString());

                            if(response.isSuccessful()){
                                hitsList = response.body().getHits();
                            }else{
                                jsonResponse = response.errorBody().string();
                            }

                            Log.d(TAG, "onResponse: hits: " + hitsList);

                            for(int i = 0; i < hitsList.getUserIndex().size(); i++){
                                Log.d(TAG, "onResponse: data: " + hitsList.getUserIndex().get(i).getUser().toString());
                                mUsers.add(hitsList.getUserIndex().get(i).getUser());
                            }
                            mRecyclerViewAdapter.setFilter(mUsers);


                            Log.d(TAG, "onResponse: size: " + mUsers.size());

                            //setup the list of posts

                        }catch (NullPointerException e){
                            Log.e(TAG, "onResponse: NullPointerException: " + e.getMessage() );
                        }
                        catch (IndexOutOfBoundsException e){
                            Log.e(TAG, "onResponse: IndexOutOfBoundsException: " + e.getMessage() );
                        }
                        catch (IOException e){
                            Log.e(TAG, "onResponse: IOException: " + e.getMessage() );
                        }


                    }

                    @Override
                    public void onFailure(retrofit2.Call<HitsObject> call, Throwable t) {

                    }
                });


                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });





        return view;
    }
    private void initRecyclerView() {
        if (mRecyclerViewAdapter==null){
            mRecyclerViewAdapter=new SearchRecyclerViewAdapter((MainActivity)getContext(),mUsers);
        }


        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mRecyclerViewAdapter);

    }
    private void getWholeUsers(){

        Query ref=null;
        if (mLastQueriedDocument!=null){
            ref=FirebaseFirestore.getInstance()
                    .collection("users")
                    .orderBy("user_id",Query.Direction.DESCENDING)
                    .startAfter(mLastQueriedDocument);
        }else {
            ref=FirebaseFirestore.getInstance().collection("users").orderBy("user_id",Query.Direction.DESCENDING);
        }
        ref.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for(QueryDocumentSnapshot doc:task.getResult()){
                        User user=doc.toObject(User.class);
                        mUsers.add(user);
                    }
                    if (task.getResult().size()!=0){
                        mLastQueriedDocument=task.getResult().getDocuments().get(task.getResult().size()-1);

                    }


                    mRecyclerViewAdapter.notifyDataSetChanged();

                }
            }
        });
    }
    private void getElasticSearchPassword(){
        CollectionReference query=FirebaseFirestore.getInstance().collection("elasticsearch");
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                for (QueryDocumentSnapshot snapshot:queryDocumentSnapshots){
                    mElasticSearchPassword=snapshot.get("password").toString();
                    Log.d(TAG,"PASSWORD: "+mElasticSearchPassword);
                }

            }
        });

    }
}
