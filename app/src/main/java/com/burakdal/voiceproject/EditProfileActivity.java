package com.burakdal.voiceproject;

import android.Manifest;
import android.content.pm.PackageManager;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

public class EditProfileActivity extends AppCompatActivity implements SelectPhotoDialog1.OnPhotoSelectedListener1 {
    @Override
    public void getImagePath(Uri imagePath) {

    }

    @Override
    public void getImageBitmap(Bitmap bitmap) {

    }
    private final String TAG="EditProfileFragment";
    private final int REQUEST_PERMISSION_CODE=1000;
    private ImageButton mPlayBtn,mRecordBtn;
    private ImageView mProfilePic;
    private EditText mDescriptionText;
    private Button mApplyBtn;
    private Chronometer mChronometer;
    private TextView mTextView;
    private File mVoiceFolder;
    private String mDownloadUrlAudio;
    private String mDownloadUrlImage;
    private User mUser;
    private String mPath;
    private String mUsername;
    private String mFileRef;
    private double mProgress = 0;
    private IMainActivity mInterface;
    private MediaRecorder myAudioRecorder;
    private MediaPlayer mMediaPlayer;
    private Boolean RECORDING_STATE=false;
    private Boolean PLAYING_STATE=false;
    private int mCurrentPosition=0;
    private Bitmap mSelectedBitmap;
    private Uri mSelectedUri;
    private byte[] mUploadBytes;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editprofile_activity);

        if (!checkPermissionFromDevice()){
            requestPermission();
        }
        mProfilePic=(ImageView)findViewById(R.id.ed_profil_pic);
        mPlayBtn=(ImageButton)findViewById(R.id.editPlayBtn);
        mRecordBtn=(ImageButton)findViewById(R.id.editRecordBtn);
        mApplyBtn=(Button) findViewById(R.id.applyBtn);
        mDescriptionText=(EditText)findViewById(R.id.editDescription);
        mChronometer=(Chronometer)findViewById(R.id.mChr);
        mTextView=(TextView)findViewById(R.id.edit_profile_user_name);
        createVoiceFolder();
        setUserParams();

        mProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: opening dialog to choose new photo");
                SelectPhotoDialog1 dialog = new SelectPhotoDialog1();
                dialog.show(getSupportFragmentManager(), getString(R.string.dialog_select_photo1));


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
                    Toast.makeText(EditProfileActivity.this,"Recording",Toast.LENGTH_SHORT).show();

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
                                    Toast.makeText(EditProfileActivity.this,"Playing is finished", Toast.LENGTH_SHORT).show();

                                    mp.release();
                                    PLAYING_STATE=false;

                                    mPlayBtn.setImageResource(R.drawable.play_ic);


                                }
                            });

                            mPlayBtn.setImageResource(R.drawable.pause_ic);







                            Toast.makeText(EditProfileActivity.this,"Playing", Toast.LENGTH_SHORT).show();

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


    }

    private void loadNormal() {
        if (mPath != null) {
            Uri file = Uri.fromFile(new File(mPath));
            final StorageReference storageReferenceAudio = FirebaseStorage.getInstance().getReference()
                    .child("users/profiles" + FirebaseAuth.getInstance().getCurrentUser().getUid() +
                            "/description_audio");
            UploadTask uploadTaskAudio = storageReferenceAudio.putFile(file);
            uploadTaskAudio.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(EditProfileActivity.this, "Record is successfully uploaded", Toast.LENGTH_SHORT).show();
                    Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uri.isComplete()) ;
                    Uri url = uri.getResult();
                    mDownloadUrlAudio = url.toString();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditProfileActivity.this, "Fail in uploading Record.", Toast.LENGTH_SHORT).show();
                }
            });


            if (mDownloadUrlImage == null) {
                mDownloadUrlImage = " ";
            }
            if (mDownloadUrlAudio == null) {
                mDownloadUrlAudio = " ";
            }
            Log.d(TAG, "onSuccess: firebase download url: " + mDownloadUrlImage);
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            final DatabaseReference reference1 = reference.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            reference1.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Map<String, Object> postValues = new HashMap<String, Object>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            postValues.put(snapshot.getKey(), snapshot.getValue());
                        }
                        postValues.put("profileImage", mDownloadUrlImage);
                        postValues.put("description", mDescriptionText.getText().toString());
                        postValues.put("descriptionVoice", mDownloadUrlAudio);
                        reference1.updateChildren(postValues);
                    }
                    MyProfile fragment = new MyProfile();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
    }

    private boolean checkPermissionFromDevice(){
        int write_external_storage_result=ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result=ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO);
        int read_externat_storage=ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE);

        return write_external_storage_result==PackageManager.PERMISSION_GRANTED && record_audio_result==PackageManager.PERMISSION_GRANTED && read_externat_storage==PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermission() {
        ActivityCompat.requestPermissions(this,new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        },REQUEST_PERMISSION_CODE);
    }
    private void uploadNewPhoto(Bitmap bitmap){
        Log.d(TAG, "uploadNewPhoto: uploading a new image bitmap to storage");
        EditProfileActivity.BackgroundImageResize resize = new EditProfileActivity.BackgroundImageResize(bitmap);
        Uri uri = null;
        resize.execute(uri);
    }
    private void uploadNewPhoto(Uri imagePath){
        Log.d(TAG, "uploadNewPhoto: uploading a new image uri to storage.");
        EditProfileActivity.BackgroundImageResize resize = new EditProfileActivity.BackgroundImageResize(null);
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
            Toast.makeText(EditProfileActivity.this, "compressing image", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected byte[] doInBackground(Uri... params) {
            Log.d(TAG, "doInBackground: started.");

            if (mBitmap == null) {
                try {
                    RotateBitmap rotateBitmap = new RotateBitmap();
                    mBitmap = rotateBitmap.HandleSamplingAndRotationBitmap(EditProfileActivity.this, params[0]);
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

    private void executeUploadTask() {
        Toast.makeText(EditProfileActivity.this, "uploading image", Toast.LENGTH_SHORT).show();

        if (mUploadBytes != null) {
            final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                    .child("users/profiles" + FirebaseAuth.getInstance().getCurrentUser().getUid() +
                            "/profile_image");
            UploadTask uploadTask = storageReference.putBytes(mUploadBytes);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(EditProfileActivity.this, "Post Success", Toast.LENGTH_SHORT).show();
                    Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uri.isComplete()) ;
                    Uri url = uri.getResult();
                    mDownloadUrlImage = url.toString();
                    //insert the download url into the firebase database

                    loadNormal();


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditProfileActivity.this, "could not upload photo", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double currentProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    if (currentProgress > (mProgress + 15)) {
                        mProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        Log.d(TAG, "onProgress: upload is " + mProgress + "& done");
                        Toast.makeText(EditProfileActivity.this, mProgress + "%", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_PERMISSION_CODE:
            {
                if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(EditProfileActivity.this,"Permission Granted",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(EditProfileActivity.this,"Permission Denied",Toast.LENGTH_SHORT).show();
                }
            }
            break;
        }

    }
    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality,stream);
        return stream.toByteArray();
    }
    private void createVoiceFolder(){
        File movieFile=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS);
        mVoiceFolder=new File(movieFile,"VoiceAppV");

        if(!mVoiceFolder.exists()){
            mVoiceFolder.mkdirs();
        }
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
    private void setUserParams(){
        if (mUser!=null){
            if(mUser.getProfileImage()!=null){
                Log.d(TAG,"setting profile img url: "+mUser.getProfileImage());
                RequestOptions requestOptions=new RequestOptions().placeholder(R.drawable.my_profile);
                Glide.with(EditProfileActivity.this).load(mUser.getProfileImage()).apply(requestOptions).into(mProfilePic);
            }
            Log.d(TAG,"user description: "+mUser.getDescription());
            mDescriptionText.setText(mUser.getDescription());
            mDownloadUrlImage=mUser.getProfileImage();
            mDownloadUrlAudio=mUser.getDescriptionVoice();
            if (mUser.getDescriptionVoice()!=null){
                mPath=mUser.getDescriptionVoice();
                mPlayBtn.setBackgroundColor(getResources().getColor(R.color.lightGreen));
            }

        }else{
            Toast.makeText(EditProfileActivity.this,"user is null",Toast.LENGTH_SHORT).show();
        }
    }
    private void setUpMediaRecorder() {
        myAudioRecorder=new MediaRecorder();
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myAudioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        myAudioRecorder.setOutputFile(mPath);

    }
}
