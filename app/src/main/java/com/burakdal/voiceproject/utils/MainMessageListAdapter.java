package com.burakdal.voiceproject.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.burakdal.voiceproject.R;
import com.burakdal.voiceproject.models.InstantMessage;
import com.burakdal.voiceproject.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SnapshotMetadata;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class MainMessageListAdapter extends BaseAdapter {
    private final static String TAG="MainMessageListAdapter";
    private Activity mActivity;
    private Query mDatabaseReference;
    private String mDisplayName;
    private ArrayList<QueryDocumentSnapshot> mSnapshotList;
    private EventListener<QuerySnapshot> mListener=new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
            for(DocumentChange snapshot:queryDocumentSnapshots.getDocumentChanges()){

                if (snapshot.getType()==DocumentChange.Type.ADDED){
                    mSnapshotList.add(snapshot.getDocument());
                }



            }
            notifyDataSetChanged();
        }
    };



    public MainMessageListAdapter(Activity activity, User clientUser,User currentUser) {

        mActivity = activity;
        mDisplayName = currentUser.getName();
        String totalId=generateTotalId(currentUser.getUser_id(),clientUser.getUser_id());
        // common error: typo in the db location. Needs to match what's in MainChatActivity.
        Log.d(TAG,"in costructor");


        mDatabaseReference =FirebaseFirestore.getInstance().collection("messages").document(totalId).collection("prmessages").orderBy("timestamp",Query.Direction.ASCENDING);
        Log.d(TAG,"in costructor2");


        mDatabaseReference.addSnapshotListener(mListener);

        mSnapshotList = new ArrayList<>();
    }

    private static class ViewHolderForMessage{
//        TextView authorName;
        TextView body;
        LinearLayout.LayoutParams params;
    }
    private static class ViewHolderForVoiceMessage{
        //        TextView authorName;
        ImageButton playbutton;
        LinearLayout.LayoutParams params;
        LinearLayout layout;
        SeekBar seekbar;
    }

    @Override
    public int getCount() {
        return mSnapshotList.size();
    }

    @Override
    public InstantMessage getItem(int position) {

        DocumentSnapshot snapshot = mSnapshotList.get(position);
        return snapshot.toObject(InstantMessage.class);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final InstantMessage message = getItem(position);
        if (message.getVoiceUrl()==null){

            LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.chat_msg_row, parent, false);

            final ViewHolderForMessage holder = new ViewHolderForMessage();
//            holder.authorName = (TextView) convertView.findViewById(R.id.author);
            holder.body = (TextView) convertView.findViewById(R.id.message);
            holder.params = (LinearLayout.LayoutParams) holder.body.getLayoutParams();
            convertView.setTag(holder);






            boolean isMe = message.getAuthor().equals(mDisplayName);
            setChatRowAppearanceForMessage(isMe, holder);

//        String author = message.getAuthor();
//        holder.authorName.setText(author);

            String msg = message.getMessage();
            holder.body.setText(msg);


            return convertView;
        }else if(message.getVoiceUrl()!=null){

            LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.card_message_voice, parent, false);
            final ViewHolderForVoiceMessage holder = new ViewHolderForVoiceMessage();

            holder.playbutton=(ImageButton)convertView.findViewById(R.id.play_record_main_message_card);
            holder.seekbar=(SeekBar)convertView.findViewById(R.id.seekbar_main_message_card);
            holder.layout=(LinearLayout)convertView.findViewById(R.id.layout_of_voice_card);
            holder.params = (LinearLayout.LayoutParams) holder.layout.getLayoutParams();
            convertView.setTag(holder);




            boolean isMe = message.getAuthor().equals(mDisplayName);

            setChatRowAppearanceForVoiceMessage(isMe,holder);
            holder.playbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mActivity,"Playing",Toast.LENGTH_SHORT).show();
                }
            });

            return convertView;

        }else {
            return null;
        }



    }

    private void setChatRowAppearanceForMessage(boolean isItMe, ViewHolderForMessage holder) {

        if (isItMe) {


            holder.params.gravity = Gravity.END;
//            holder.authorName.setTextColor(Color.GREEN);

            // If you want to use colours from colors.xml
            // int colourAsARGB = ContextCompat.getColor(mActivity.getApplicationContext(), R.color.yellow);
            // holder.authorName.setTextColor(colourAsARGB);

            holder.body.setBackgroundResource(R.drawable.bubble2);
        } else {
            holder.params.gravity = Gravity.START;
//            holder.authorName.setTextColor(Color.BLUE);
            holder.body.setBackgroundResource(R.drawable.bubble1);
        }

//        holder.authorName.setLayoutParams(holder.params);
        holder.body.setLayoutParams(holder.params);

    }
    private void setChatRowAppearanceForVoiceMessage(boolean isItMe, ViewHolderForVoiceMessage holder) {

        if (isItMe) {

            Log.d(TAG,"gravity end ");
//            holder.params.gravity = Gravity.END;
            holder.params.gravity = Gravity.END;
            holder.layout.setGravity(Gravity.END);
//            holder.authorName.setTextColor(Color.GREEN);

            // If you want to use colours from colors.xml
            // int colourAsARGB = ContextCompat.getColor(mActivity.getApplicationContext(), R.color.yellow);
            // holder.authorName.setTextColor(colourAsARGB);

//            holder.layout.setBackgroundResource(R.drawable.rounded_rectangle_shape);
        } else {
            Log.d(TAG,"gravity start ");
            holder.layout.setGravity(Gravity.START);
            holder.params.gravity = Gravity.START;

//            holder.params.gravity = Gravity.START;
//            holder.authorName.setTextColor(Color.BLUE);
//            holder.layout.setBackgroundResource(R.drawable.rounded_rectangle_shape);
        }
        holder.layout.setLayoutParams(holder.params);



//        holder.authorName.setLayoutParams(holder.params);
//
//        holder.seekbar.setLayoutParams(holder.params);
//        holder.playbutton.setLayoutParams(holder.params);

    }
    private String generateTotalId(String currentUserId,String clientUserId){
        int first=currentUserId.compareTo(clientUserId);
        int second=clientUserId.compareTo(currentUserId);
        if (first>second){
            return clientUserId+currentUserId;
        }else{
            return currentUserId+clientUserId;
        }

    }



}
