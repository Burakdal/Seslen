package com.burakdal.voiceproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.burakdal.voiceproject.models.User;
import com.burakdal.voiceproject.utils.RotateBitmap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment implements SelectPhotoDialog1.OnPhotoSelectedListener1{
    private ImageButton mPlayBtn,mRecordBtn;
    private ImageView mProfilePic;
    private EditText mDescriptionText;
    private Button mApplyBtn;
    private final String TAG="EditProfileFragment";
    private String mPath;
    private String mDownloadUrlAudio;
    private String mDownloadUrlImage;
    private String mUsername;
    private String mFileRef;
    private File mVoiceFolder;
    private double mProgress = 0;
    private IMainActivity mInterface;
    private Context mContext;
    private MediaRecorder myAudioRecorder;
    private MediaPlayer mMediaPlayer;
    private Boolean RECORDING_STATE=false;
    private Boolean PLAYING_STATE=false;
    private Chronometer mChronometer;
    private TextView mName;
    private int mMediaFileLength;
    private int mRealTimeLength;
    private int mCurrentPosition=0;

    private Bitmap mSelectedBitmap;
    private Uri mSelectedUri;
    private byte[] mUploadBytes;
    private User mUser;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle=this.getArguments();
        if (bundle!=null){
            mUser=bundle.getParcelable("editedUser");
            mUsername=mUser.getName();
            Log.d(TAG,"username: "+mUsername);
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.edit_profile,container,false);
        mProfilePic=(ImageView)view.findViewById(R.id.ed_profil_pic);
        mPlayBtn=(ImageButton) view.findViewById(R.id.editPlayBtn);
        mRecordBtn=(ImageButton) view.findViewById(R.id.editRecordBtn);
        mApplyBtn=(Button) view.findViewById(R.id.applyBtn);
        mDescriptionText=(EditText)view.findViewById(R.id.editDescription);
        mChronometer=(Chronometer)view.findViewById(R.id.mChr);
        mName=(TextView)view.findViewById(R.id.edit_profile_user_name);

        mContext=(MainActivity)getContext();
        createVoiceFolder();
        setUserParams();


        mProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: opening dialog to choose new photo");
                SelectPhotoDialog1 dialog = new SelectPhotoDialog1();
                if (getFragmentManager()!=null){
                    Log.d(TAG,"getfragmentmanager is not null");
                }else {
                    Log.d(TAG,"getfragmentmanager is  null");

                }
                dialog.setTargetFragment(EditProfileFragment.this,1);
                dialog.show(getFragmentManager(), getString(R.string.dialog_select_photo1));


            }
        });

        mRecordBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG,"Record Button is clicked");
                if (!RECORDING_STATE){
                    try {
                        createVoiceFileName();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setUpMediaRecorder();
                    try {
                        Log.d(TAG,"media recorder is preparing");
                        myAudioRecorder.prepare();

                        myAudioRecorder.start();
                        mRecordBtn.setBackgroundColor(getResources().getColor(R.color.colorBusy));
                        mChronometer.setBase(SystemClock.elapsedRealtime());

                        mChronometer.start();


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getActivity(),"Recording",Toast.LENGTH_SHORT).show();

                    RECORDING_STATE=true;

                }
                else{
                    myAudioRecorder.stop();
                    myAudioRecorder.release();
                    mChronometer.stop();






                    RECORDING_STATE=false;

                }





            }

        });


        mPlayBtn.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {

                Log.d(TAG,"mPATH in playbtn: "+mPath);
                AsyncTask<String,String,String> audioPlayer=new AsyncTask<String, String, String>() {


                    @Override
                    protected String doInBackground(String... strings) {
                        try {





                            if (!PLAYING_STATE){
                                mMediaPlayer= new MediaPlayer();

                                mMediaPlayer.setDataSource(strings[0]);
                                mMediaPlayer.prepare();

                            }





                            ;

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(String s) {




                        if (!mMediaPlayer.isPlaying()){
                            if (!PLAYING_STATE){

                                mMediaPlayer.start();
                                PLAYING_STATE=true;
                            }else{


                                mMediaPlayer.seekTo(mCurrentPosition);
                                mMediaPlayer.start();

                            }
                            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    mCurrentPosition=0;
                                    Toast.makeText(mContext,"Playing is finished", Toast.LENGTH_SHORT).show();

                                    mp.release();
                                    PLAYING_STATE=false;

                                    mPlayBtn.setImageResource(R.drawable.play_ic);


                                }
                            });

                            mPlayBtn.setImageResource(R.drawable.pause_ic);







                            Toast.makeText(mContext,"Playing", Toast.LENGTH_SHORT).show();

                        }else{
                            mMediaPlayer.pause();
                            mCurrentPosition=mMediaPlayer.getCurrentPosition();
//                            mRealTimeLength=mMediaFileLength-mCurrentPosition;


                            mPlayBtn.setImageResource(R.drawable.play_ic);
                        }


                    }
                };
                audioPlayer.execute(mPath);
            }
        });

        mApplyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to post...");


                //we have a bitmap and no Uri
                if(mSelectedBitmap != null && mSelectedUri == null){
                    uploadNewPhoto(mSelectedBitmap);
                }
                //we have no bitmap and a uri
                else if(mSelectedBitmap == null && mSelectedUri != null){
                    uploadNewPhoto(mSelectedUri);
                }else{
                    loadNormal();

                }

            }
        });




        return view;
    }
    private void init(View view){

    }
    private void uploadNewPhoto(Bitmap bitmap){
        Log.d(TAG, "uploadNewPhoto: uploading a new image bitmap to storage");
        BackgroundImageResize resize = new BackgroundImageResize(bitmap);
        Uri uri = null;
        resize.execute(uri);
    }
    private void setUpMediaRecorder() {
        myAudioRecorder=new MediaRecorder();
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myAudioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        myAudioRecorder.setOutputFile(mPath);

    }

    private void uploadNewPhoto(Uri imagePath){
        Log.d(TAG, "uploadNewPhoto: uploading a new image uri to storage.");
        BackgroundImageResize resize = new BackgroundImageResize(null);
        resize.execute(imagePath);
    }
    public class BackgroundImageResize extends AsyncTask<Uri, Integer, byte[]> {

        Bitmap mBitmap;

        public BackgroundImageResize(Bitmap bitmap) {
            if (bitmap != null) {
                this.mBitmap = bitmap;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getActivity(), "compressing image", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected byte[] doInBackground(Uri... params) {
            Log.d(TAG, "doInBackground: started.");

            if (mBitmap == null) {
                try {
                    RotateBitmap rotateBitmap = new RotateBitmap();
                    mBitmap = rotateBitmap.HandleSamplingAndRotationBitmap(getActivity(), params[0]);
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: IOException: " + e.getMessage());
                }
            }
            byte[] bytes = null;
            Log.d(TAG, "doInBackground: megabytes before compression: " + mBitmap.getByteCount() / 1000000);
            bytes = getBytesFromBitmap(mBitmap, 100);
            Log.d(TAG, "doInBackground: megabytes before compression: " + bytes.length / 1000000);
            return bytes;
        }
        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            mUploadBytes = bytes;
            //execute the upload task
            executeUploadTask();
        }

    }
    private void setUserParams(){
        if (mUser!=null){
            if(mUser.getProfileImage()!=null){
                Log.d(TAG,"setting profile img url: "+mUser.getProfileImage());
                RequestOptions requestOptions=new RequestOptions().placeholder(R.drawable.my_profile);
                Glide.with(mContext).load(mUser.getProfileImage()).apply(requestOptions).into(mProfilePic);
            }
            Log.d(TAG,"user description: "+mUser.getDescription());
            mDescriptionText.setText(mUser.getDescription());
            mName.setText(mUser.getName());
            mDownloadUrlImage=mUser.getProfileImage();
            mDownloadUrlAudio=mUser.getDescriptionVoice();
            if (mUser.getDescriptionVoice()!=null){
                mPath=mUser.getDescriptionVoice();
                mPlayBtn.setBackgroundColor(getResources().getColor(R.color.lightGreen));
            }

        }else{
            Toast.makeText(getActivity(),"user is null",Toast.LENGTH_SHORT).show();
        }
    }


    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality,stream);
        return stream.toByteArray();
    }

    @Override
    public void getImagePath(Uri imagePath) {
        Log.d(TAG, "getImagePath: setting the image to imageview");
        UniversalImageLoader.setImage(imagePath.toString(), mProfilePic);
        //assign to global variable
        mSelectedBitmap = null;
        mSelectedUri = imagePath;
    }

    @Override
    public void getImageBitmap(Bitmap bitmap) {
        Log.d(TAG, "getImageBitmap: setting the image to imageview");
        mProfilePic.setImageBitmap(bitmap);
        //assign to a global variable
        mSelectedUri = null;
        mSelectedBitmap = bitmap;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mInterface=(IMainActivity)context;
    }

    private void executeUploadTask(){
        Toast.makeText(getActivity(), "uploading image", Toast.LENGTH_SHORT).show();
        if (mDownloadUrlImage == null) {
            mDownloadUrlImage = mUser.getProfileImage();
        }
        if (mUploadBytes!=null){
            final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                    .child("users/profiles" + FirebaseAuth.getInstance().getCurrentUser().getUid() +
                            "/profile_image");
            UploadTask uploadTask = storageReference.putBytes(mUploadBytes);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getActivity(), "Post Success", Toast.LENGTH_SHORT).show();
                    Task<Uri> uri=taskSnapshot.getStorage().getDownloadUrl();
                    while(!uri.isComplete());
                    Uri url=uri.getResult();
                    mDownloadUrlImage=url.toString();
                    //insert the download url into the firebase database









                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double currentProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    if( currentProgress > (mProgress + 15)){
                        mProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        Log.d(TAG, "onProgress: upload is " + mProgress + "& done");
                        Toast.makeText(getActivity(), mProgress + "%", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    loadNormal();
                }
            });


        }else{
            loadNormal();
        }

        Log.d(TAG,"outside the class image url: "+mDownloadUrlImage);











    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(getActivity(),"you are in editprofile",Toast.LENGTH_SHORT).show();

    }

    private void createVoiceFileName() throws IOException {
        String timestamp = new SimpleDateFormat("yyMMdd_HHmmss").format(new Date());
        mFileRef = mUsername+"_RV_" + timestamp + ".3gp";
        mPath=mVoiceFolder.getAbsolutePath()+"/"+mFileRef;
        Log.d(TAG,"mPath: "+mPath);
//        File videoFile = File.createTempFile(preppend, ".3gp", mVoiceFolder);
//        m
//        mOutputFile = videoFile.getAbsolutePath();

    }
    private void createVoiceFolder(){
        File movieFile=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS);
        mVoiceFolder=new File(movieFile,"VoiceAppV");

        if(!mVoiceFolder.exists()){
            mVoiceFolder.mkdirs();
        }
    }
    private void loadNormal() {
        if (mDownloadUrlAudio == null) {
            mDownloadUrlAudio = mUser.getDescriptionVoice();
            Log.d(TAG,"mDownload audio url: stage1 "+mDownloadUrlAudio);
        }
        if (mPath != null) {
            Uri file = Uri.fromFile(new File(mPath));
            if (file!=null){
                Log.d(TAG,"file is not null");
                StorageReference storageReferenceAudio = FirebaseStorage.getInstance().getReference()
                        .child("users/profiles" + FirebaseAuth.getInstance().getCurrentUser().getUid() +
                                "/description_audio");
                UploadTask uploadTaskAudio = storageReferenceAudio.putFile(file);
                uploadTaskAudio.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getActivity(), "Record is successfully uploaded", Toast.LENGTH_SHORT).show();
                        Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                        Log.d(TAG,"uri is not complete");
                        while (!uri.isComplete()) ;
                        Log.d(TAG,"uri is complete");
                        Uri url = uri.getResult();
                        mDownloadUrlAudio = url.toString();



                    }
                }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        DocumentReference docRef=FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        User user=new User();
                        user.setName(mName.getText().toString());
                        user.setDescription(mDescriptionText.getText().toString());
                        user.setDescriptionVoice(mDownloadUrlAudio);
                        user.setProfileImage(mDownloadUrlImage);
                        user.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        docRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Log.d(TAG,"successfully uploaded");
                                    mInterface.inflateMyProfileFragment();
                                }else{
                                    Log.d(TAG,"there is a problem");
                                }
                            }
                        });
                    }
                });
            }

        }else{
            DocumentReference docRef=FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
            User user=new User();
            user.setName(mName.getText().toString());
            user.setDescription(mDescriptionText.getText().toString());
            user.setDescriptionVoice(mDownloadUrlAudio);
            user.setProfileImage(mDownloadUrlImage);
            user.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
            docRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Log.d(TAG,"successfully uploaded");
                        mInterface.inflateMyProfileFragment();
                    }else{
                        Log.d(TAG,"there is a problem");
                    }
                }
            });
        }
        Log.d(TAG,"mDownload audio url: stage2 "+mDownloadUrlAudio);


        Log.d(TAG, "onSuccess: firebase download url: " + mDownloadUrlImage);

    }
}


