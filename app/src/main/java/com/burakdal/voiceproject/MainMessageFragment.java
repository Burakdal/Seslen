package com.burakdal.voiceproject;

import android.content.Context;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.burakdal.voiceproject.models.InstantMessage;
import com.burakdal.voiceproject.models.NotificationObject;
import com.burakdal.voiceproject.models.User;
import com.burakdal.voiceproject.utils.MainMessageListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainMessageFragment extends Fragment {

    private final static String TAG="MainMessageFragment";
    private EditText mInputText;
    private IMainActivity mInterface;
    private User mCurrentUser;
    private User mClientUser;
    private MainMessageListAdapter mAdapter;
    private android.support.v7.widget.Toolbar mToolbar;
    //RecordingVoice
    private Boolean RECORDING_STATE = false;
    private File mVoiceFolder;
    private String mPath;
    private String mFileRef;
    private MediaRecorder myAudioRecorder;




    private LinearLayout mLinearLayout;
    private boolean DOCUMENT_STATE=false;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mInterface=(IMainActivity)context;
    }

    private ImageButton mSendButton,mRecordBtn;
    private ListView mChatListView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view=inflater.inflate(R.layout.main_message_layout,container,false);
        getCurrentUser();
        createVoiceFolder();


        mToolbar=(android.support.v7.widget.Toolbar) view.findViewById(R.id.main_message_toolbar);
        mToolbar.setNavigationIcon(R.drawable.back_ic);
        mToolbar.setTitle(mClientUser.getName());
        mLinearLayout=(LinearLayout)view.findViewById(R.id.linearLayout2);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInterface.onBackPressed();
            }
        });
        mInputText = (EditText)view. findViewById(R.id.messageInput);
        mSendButton = (ImageButton)view. findViewById(R.id.sendButton);
        mRecordBtn=(ImageButton)view. findViewById(R.id.recordBtn_for_comment);
        mRecordBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction()==MotionEvent.ACTION_DOWN){
                    try {
                        createVoiceFileName();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setUpMediaRecorder();
                    try {
                        myAudioRecorder.prepare();

                        myAudioRecorder.start();
//                        mRecordBtn.setBackgroundColor(getResources().getColor(R.color.colorBusy));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            mRecordBtn.setBackground(getResources().getDrawable(R.drawable.shape_of_send_voice_busy));
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getActivity(),"Recording",Toast.LENGTH_SHORT).show();
                    v.setPressed(true);
                }else if (event.getAction()==MotionEvent.ACTION_CANCEL | event.getAction()==MotionEvent.ACTION_UP ){
                    myAudioRecorder.stop();
                    myAudioRecorder.release();
                    Toast.makeText(getActivity(),"Recording Stop",Toast.LENGTH_SHORT).show();
//                    mRecordBtn.setBackgroundColor(getResources().getColor(R.color.notBusy));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mRecordBtn.setBackground(getResources().getDrawable(R.drawable.shape_of_send_voice));
                    }
                    sendMessageWhole();

                    v.setPressed(false);
                }
                return true;
            }
        });


        mChatListView = (ListView)view. findViewById(R.id.chat_list_view);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        mInputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                sendMessageWhole();
                return true;
            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                sendMessage();
                sendMessageWhole();

            }
        });






        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter=new MainMessageListAdapter(getActivity(),mClientUser,mCurrentUser);
        mChatListView.setAdapter(mAdapter);
    }


    private void createVoiceFileName() throws IOException {
        String timestamp = new SimpleDateFormat("yyMMdd_HHmmss").format(new Date());
        mFileRef = mCurrentUser.getName()+"_RV_" + timestamp + ".3gp";
        mPath=mVoiceFolder.getAbsolutePath()+"/"+mFileRef;;

    }
    private void setUpMediaRecorder() {
        myAudioRecorder=new MediaRecorder();
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myAudioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        myAudioRecorder.setOutputFile(mPath);

    }
    private void createVoiceFolder(){
        File movieFile=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS);
        mVoiceFolder=new File(movieFile,"VoiceAppV");

        if(!mVoiceFolder.exists()){
            mVoiceFolder.mkdirs();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle=this.getArguments();
        if (bundle!=null){
            mClientUser=bundle.getParcelable("clientUser");

        }

    }

    private void getCurrentUser(){
        mCurrentUser=mInterface.getUser();
    }

    private void sendMessage() {

        Log.d("FlashChat", "I sent something");
        // TODO: Grab the text the user typed in and push the message to Firebase
        String input = mInputText.getText().toString();
        if (!input.equals("")) {


            String totalId=generateTotalId(mCurrentUser.getUser_id(),mClientUser.getUser_id());
            DocumentReference ref=FirebaseFirestore.getInstance().collection("messages").document(totalId).collection("prmessages").document();
            InstantMessage chat=new InstantMessage();
            chat.setMessage(input);
            chat.setAuthor(mCurrentUser.getName());
            chat.setMessageId(ref.getId());


            ref.set(chat).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(getActivity(),"successfully uploaded",Toast.LENGTH_SHORT).show();
                    mInputText.setText("");
                }
            });



        }

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
    private void sendMessageWhole(){
        String wholeId=generateTotalId(mCurrentUser.getUser_id(),mClientUser.getUser_id());

        final DocumentReference ref=FirebaseFirestore.getInstance().collection("messages").document(wholeId).collection("prmessages").document();
        final DocumentReference refNot=FirebaseFirestore.getInstance().collection("notifications").document(mClientUser.getUser_id()).collection("nots").document();
        DocumentReference refForThread=FirebaseFirestore.getInstance().collection("usersMessageThreads").document(mCurrentUser.getUser_id())
                .collection("listOfUsers").document(mClientUser.getUser_id());
        refForThread.set(mClientUser).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(getActivity(),"thread Added",Toast.LENGTH_SHORT).show();
            }
        });
        if(mPath!=null){
            Uri file=Uri.fromFile(new File(mPath));
            StorageReference refStorage=FirebaseStorage.getInstance().getReference().child("chatrooms/"+wholeId+"/"+mCurrentUser.getName()+"/"+ref.getId());
            refStorage.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uri.isComplete()) ;
                    Uri url = uri.getResult();


                    InstantMessage chat=new InstantMessage();
                    chat.setMessageId(ref.getId());
                    chat.setMessage("this is voice message");
                    chat.setVoiceUrl(url.toString());
                    chat.setAuthor(mCurrentUser.getName());
                    ref.set(chat).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(getActivity(),"voice message successfully send",Toast.LENGTH_SHORT).show();
                            mPath=null;
                        }
                    });
                    NotificationObject notObj=new NotificationObject();
                    notObj.setSender(mCurrentUser);
                    notObj.setSended(mClientUser);
                    notObj.setMessage("is sent you a message!!");
                    refNot.set(notObj).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG,"not is uploaded");
                        }
                    });


                }
            });
        }else if (mPath==null && mInputText.getText().toString()!=""){
            InstantMessage chat=new InstantMessage();
            chat.setAuthor(mCurrentUser.getName());
            chat.setMessage(mInputText.getText().toString());
            chat.setMessageId(ref.getId());
            ref.set(chat).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(getActivity(),"text message successfully send",Toast.LENGTH_SHORT).show();
                    mInputText.setText("");
                }
            });
            NotificationObject notObj=new NotificationObject();
            notObj.setSender(mCurrentUser);
            notObj.setSended(mClientUser);
            notObj.setMessage("is sent you a message!!");
            refNot.set(notObj).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.d(TAG,"not is uploaded");
                }
            });

        }




    }
}
