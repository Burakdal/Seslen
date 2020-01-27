package com.burakdal.voiceproject;

import android.Manifest;
import android.content.Context;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.burakdal.voiceproject.models.Post;
import com.burakdal.voiceproject.models.PostLocations;
import com.burakdal.voiceproject.utils.RotateBitmap;
import com.burakdal.voiceproject.utils.SelectLocationAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class PostFragment extends Fragment implements SelectPhotoDialog1.OnPhotoSelectedListener1,SelectLocationAdapter.ISelectLocation {
    private ImageButton mRecordBtn;

    private static final String TAG = "PostFragment";
    private String mPlaceId;

    private SelectPhotoDialog mPhotoDialog;
    private ImageButton mPlayBtn;
    private SeekBar mSeekBar;
    private TextView mTimerTextView;
    private TextView mTimeLengthView;
    private ImageView mImageView;
    private EditText mTitleView;
    private EditText mDescriptionView;
    private Button mAddLocationPlace;
    private IMainActivity mInterface;

    private int mMediaFileLength;
    private int mRealTimeLength;
    private int mCurrentPosition = 0;
    private String mDownloadUrlAudio;
    private String mDownloadUrlImage;

    static final String AUDIO_PREFS = "Audio_Prefs";
    static final String AUDIO_PATH_KEY = "audio_path";
    private static final int PICKFILE_REQUEST_CODE = 66770;
    private static final int CAMERA_REQUEST_CODE = 69857;
    final Handler mHandler = new Handler();


    private Button mUpload;
    private Button mDeleteBtn;
    private android.support.v7.widget.Toolbar mToolbar;

    private ImageView mPostImage;

    private Bitmap mSelectedBitmap;
    private Uri mSelectedUri;
    private byte[] mUploadBytes;
    private double mProgress = 0;

    private MediaRecorder myAudioRecorder;
    private MediaPlayer mMediaPlayer;
    private String mPath;
    private File mVoiceFolder;
    private String mUsername = "burak.dal.09";
    private Boolean RECORDING_STATE = false;
    private Boolean PLAYING_STATE = false;
    private GeoPoint mUserGeoPoint;
    private PostLocations mPostLocations;
    private PlacesClient mPlacesClient;

    private String mFileRef;
    private Chronometer mChronometer;

    private final int REQUEST_PERMISSION_CODE = 1000;
    private final int PLACE_PICKER_REQUEST = 1;
    private FirebaseStorage mStorage;
    private FusedLocationProviderClient mFusedLocationProviderClient;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mInterface=(IMainActivity)context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.post_fragment,container,false);
        if (!checkPermissionFromDevice()) {
            requestPermission();
        }
        Places.initialize(getActivity(), getString(R.string.google_maps_key));
        mPlacesClient = Places.createClient(getActivity());
        mRecordBtn = (ImageButton)view. findViewById(R.id.recordBtn_for_comment);
        mPlayBtn = (ImageButton)view. findViewById(R.id.playBtn);
        mUpload = (Button)view. findViewById(R.id.uploadBtn);
        mSeekBar = (SeekBar) view.findViewById(R.id.seekBar);
        mAddLocationPlace = (Button) view.findViewById(R.id.recording_voice_add_location_place);
        mAddLocationPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mInterface.inflateSelectLocationFragment();
                SelectLocationFragment dialog=new SelectLocationFragment();
                dialog.setTargetFragment(PostFragment.this,1);
                dialog.show(getFragmentManager(),getString(R.string.tag_selectlocation_fragment));


            }
        });
        mDeleteBtn=(Button)view.findViewById(R.id.deleteBtn);
        mTitleView=(EditText)view.findViewById(R.id.postTitle);
        mDescriptionView=(EditText)view.findViewById(R.id.postDes);
        mPostImage=(ImageView)view. findViewById(R.id.user_image);

        mToolbar=(android.support.v7.widget.Toolbar)view.findViewById(R.id.post_fragment_layout);

        initToolbar();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        getLastKnownLocation();

        mSeekBar.setMax(99);
        mSeekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mMediaPlayer.isPlaying()){
                    SeekBar seekBar=(SeekBar)v;

                    int playPosition=(mMediaFileLength/100)*seekBar.getProgress();
                    mRealTimeLength=playPosition;
                    mMediaPlayer.seekTo(playPosition);
                }
                return false;
            }



        });
        mChronometer=(Chronometer)view.findViewById(R.id.chorView);
        mTimerTextView=(TextView)view.findViewById(R.id.timmerView);
        mTimeLengthView=(TextView)view.findViewById(R.id.lengthView);
        mStorage=FirebaseStorage.getInstance();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        createVoiceFolder();


        mPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: opening dialog to choose new photo");
                SelectPhotoDialog1 dialog = new SelectPhotoDialog1();

                dialog.setTargetFragment(PostFragment.this,1);
                dialog.show(getFragmentManager(), getString(R.string.dialog_select_photo1));



            }
        });







        mRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!RECORDING_STATE){
                    try {
                        createVoiceFileName();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setUpMediaRecorder();
                    try {
                        myAudioRecorder.prepare();

                        myAudioRecorder.start();
                        mRecordBtn.setBackgroundColor(getResources().getColor(R.color.colorBusy));
                        mChronometer.setBase(SystemClock.elapsedRealtime());
                        mChronometer.setVisibility(View.VISIBLE);
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


                    mChronometer.setVisibility(View.INVISIBLE);
                    mRecordBtn.setVisibility(View.INVISIBLE);
                    mPlayBtn.setVisibility(View.VISIBLE);
                    mSeekBar.setVisibility(View.VISIBLE);
                    mUpload.setVisibility(View.VISIBLE);
                    mDeleteBtn.setVisibility(View.VISIBLE);

                    RECORDING_STATE=false;

                }
//                mPath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+UUID.randomUUID().toString()+"audio_record.3gp";




            }

        });

        mMediaPlayer= new MediaPlayer();

        mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                mSeekBar.setSecondaryProgress(percent);
                mRealTimeLength=mCurrentPosition;

            }
        });
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mCurrentPosition=0;
                Toast.makeText(getActivity(),"Playing is finished", Toast.LENGTH_SHORT).show();
                mRealTimeLength=mRealTimeLength;
//                PLAYING_STATE=false;
                mPlayBtn.setImageResource(R.drawable.play_ic);
                mUpload.setVisibility(View.VISIBLE);
                mDeleteBtn.setVisibility(View.VISIBLE);


            }
        });



        mPlayBtn.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {


                AsyncTask<String,String,String> audioPlayer=new AsyncTask<String, String, String>() {


                    @Override
                    protected String doInBackground(String... strings) {
                        try {

                            if (!PLAYING_STATE){

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
                        mMediaFileLength=mMediaPlayer.getDuration();
                        mTimeLengthView.setText(String.format("%d:%d",TimeUnit.MILLISECONDS.toMinutes(mMediaFileLength),TimeUnit.MILLISECONDS.toSeconds(mMediaFileLength)-TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mMediaFileLength)*60000)));


                        mRealTimeLength=mMediaPlayer.getCurrentPosition();
                        if (!mMediaPlayer.isPlaying()){
                            if (!PLAYING_STATE){

                                mMediaPlayer.start();
                                PLAYING_STATE=true;
                            }else{
                                mRealTimeLength=mCurrentPosition;

                                mMediaPlayer.seekTo(mCurrentPosition);
                                mMediaPlayer.start();
                            }








                            mUpload.setVisibility(View.INVISIBLE);
                            mDeleteBtn.setVisibility(View.INVISIBLE);
                            mTimerTextView.setVisibility(View.VISIBLE);
                            mTimeLengthView.setVisibility(View.VISIBLE);
                            mPlayBtn.setImageResource(R.drawable.pause_ic);
                            Toast.makeText(getActivity(),"Playing", Toast.LENGTH_SHORT).show();

                        }else{
                            mMediaPlayer.pause();
                            mCurrentPosition=mMediaPlayer.getCurrentPosition();
//                            mRealTimeLength=mMediaFileLength-mCurrentPosition;


                            mPlayBtn.setImageResource(R.drawable.play_ic);
                        }
                        updateSeekBar();

                    }
                };
                audioPlayer.execute(mPath);
            }
        });
        mUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to post...");
                if(!isEmpty(mTitleView.getText().toString())
                        && !isEmpty(mDescriptionView.getText().toString())){

                    //we have a bitmap and no Uri
                    if(mSelectedBitmap != null && mSelectedUri == null){
                        uploadNewPhoto(mSelectedBitmap);
                    }
                    //we have no bitmap and a uri
                    else if(mSelectedBitmap == null && mSelectedUri != null){
                        uploadNewPhoto(mSelectedUri);
                    }
                }else{
                    Toast.makeText(getActivity(), "You must fill out all the fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;

    }
    private void initToolbar(){

        mToolbar.setTitle("Post");
        mToolbar.inflateMenu(R.menu.post_fragment_layout_menu);
        MenuItem menuItem=mToolbar.getMenu().findItem(R.id.close_icon_post_fragment_toolbar);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mInterface.inflateHomeFragment();
                return false;

            }
        });
    }
    private void updateSeekBar() {
        mSeekBar.setProgress((int)(((float)mMediaPlayer.getCurrentPosition()/mMediaFileLength)*100));
        if (mMediaPlayer.isPlaying()){
            Runnable updater=new Runnable() {
                @Override
                public void run() {
                    updateSeekBar();

                    mTimerTextView.setText(String.format("%d:%d",TimeUnit.MILLISECONDS.toMinutes(mRealTimeLength),TimeUnit.MILLISECONDS.toSeconds(mRealTimeLength)-TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mRealTimeLength)*60000)));
                    mRealTimeLength+=1000;
                }

            };
            mHandler.postDelayed(updater,1000);
        }
    }
    private void uploadNewPhoto(Bitmap bitmap){
        Log.d(TAG, "uploadNewPhoto: uploading a new image bitmap to storage");
        PostFragment.BackgroundImageResize resize = new PostFragment.BackgroundImageResize(bitmap);
        Uri uri = null;
        resize.execute(uri);
    }
    private void uploadNewPhoto(Uri imagePath){
        Log.d(TAG, "uploadNewPhoto: uploading a new image uri to storage.");
        PostFragment.BackgroundImageResize resize = new PostFragment.BackgroundImageResize(null);
        resize.execute(imagePath);
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
                mUserGeoPoint =new GeoPoint(location.getLatitude(),location.getLongitude());
            }
        });


    }

    @Override
    public void getPlace(Place place) {
        mPlaceId=place.getId();
        Log.d(TAG,"in get place -place id: "+place.getId());
        mAddLocationPlace.setText(place.getName());
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

    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality,stream);
        return stream.toByteArray();
    }
    private void resetFields(){
        UniversalImageLoader.setImage("", mPostImage);
        mTitleView.setText("");
        mDescriptionView.setText("");

    }

    private void setUpMediaRecorder() {
        myAudioRecorder=new MediaRecorder();
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myAudioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        myAudioRecorder.setOutputFile(mPath);

    }
    private boolean isEmpty(String string){
        return string.equals("");
    }

    private void executeUploadTask(){
        Toast.makeText(getActivity(), "uploading image", Toast.LENGTH_SHORT).show();
        Uri file=Uri.fromFile(new File(mPath));
//        final String postId = FirebaseDatabase.getInstance().getReference().push().getKey();
        FirebaseFirestore db=FirebaseFirestore.getInstance();
        final DocumentReference postRef=db.collection("posts").document();

        final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("posts/users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() +
                        "/" + postRef.getId() + "/post_image");
        final StorageReference storageReferenceAudio = FirebaseStorage.getInstance().getReference()
                .child("posts/users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() +
                        "/" + postRef.getId()  + "/post_audio");


        UploadTask uploadTaskAudio=storageReferenceAudio.putFile(file);
        uploadTaskAudio.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getActivity(),"Record is successfully uploaded",Toast.LENGTH_SHORT).show();
                Task<Uri> uri=taskSnapshot.getStorage().getDownloadUrl();
                while(!uri.isComplete());
                Uri url=uri.getResult();
                mDownloadUrlAudio=url.toString();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(),"Fail in uploading Record.",Toast.LENGTH_SHORT).show();
            }
        });
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


                Log.d(TAG, "onSuccess: firebase download url: " + mDownloadUrlImage);
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();



                Post post = new Post();

                post.setImage(mDownloadUrlImage);
                Log.d(TAG,"latitude: "+mUserGeoPoint.getLatitude());
                if (mPlaceId!=null){
                    post.setPlaceId(mPlaceId);

                }else {
                    Log.d(TAG,"placeId:null ");
                }
                post.setDescription(mDescriptionView.getText().toString());
                post.setPost_id(postRef.getId() );

                post.setAudio(mDownloadUrlAudio);

                post.setTitle(mTitleView.getText().toString());
                post.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

                mRecordBtn.setVisibility(View.VISIBLE);
                mUpload.setVisibility(View.INVISIBLE);
                mDeleteBtn.setVisibility(View.INVISIBLE);
                mSeekBar.setVisibility(View.INVISIBLE);
                mTimerTextView.setVisibility(View.INVISIBLE);
                mTimeLengthView.setVisibility(View.INVISIBLE);
                mPlayBtn.setVisibility(View.INVISIBLE);

                //postLocation upload
                DocumentReference locationRef=FirebaseFirestore.getInstance()
                        .collection("postLocations").document(postRef.getId() );


                mPostLocations=new PostLocations(post,mInterface.getUser(),mUserGeoPoint,locationRef.getId(),null);

                locationRef.set(mPostLocations).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG,"successfully uploaded postlocations");
                    }
                });


                //post upload

                postRef.set(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG,"succesfully uploaded");
                    }
                });
                mInterface.inflateHomeFragment();



            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "could not upload photo", Toast.LENGTH_SHORT).show();
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
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_PERMISSION_CODE:
            {
                if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(getActivity(),"Permission Granted",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getActivity(),"Permission Denied",Toast.LENGTH_SHORT).show();
                }
            }
            break;
        }

    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(getActivity(),new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        },REQUEST_PERMISSION_CODE);
    }
    private boolean checkPermissionFromDevice(){
        int write_external_storage_result=ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result=ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.RECORD_AUDIO);
        int read_externat_storage=ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.READ_EXTERNAL_STORAGE);

        return write_external_storage_result==PackageManager.PERMISSION_GRANTED && record_audio_result==PackageManager.PERMISSION_GRANTED && read_externat_storage==PackageManager.PERMISSION_GRANTED;
    }
    private void createVoiceFileName() throws IOException {
        String timestamp = new SimpleDateFormat("yyMMdd_HHmmss").format(new Date());
        mFileRef = mUsername+"_RV_" + timestamp + ".3gp";
        mPath=mVoiceFolder.getAbsolutePath()+"/"+mFileRef;;

    }
    private void createVoiceFolder(){
        File movieFile=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS);
        mVoiceFolder=new File(movieFile,"VoiceAppV");

        if(!mVoiceFolder.exists()){
            mVoiceFolder.mkdirs();
        }
    }
    public void deleteAudio(View v){
        File currentAudio=new File(mPath);
        Boolean isDeleted=currentAudio.delete();
        if (isDeleted){
            Toast.makeText(getActivity(),"Record is deleted!!",Toast.LENGTH_SHORT).show();
            mRecordBtn.setVisibility(View.VISIBLE);
            mRecordBtn.setBackgroundColor(getResources().getColor(R.color.notBusy));
            mTimerTextView.setVisibility(View.INVISIBLE);
            mTimeLengthView.setVisibility(View.INVISIBLE);

            mUpload.setVisibility(View.INVISIBLE);
            mDeleteBtn.setVisibility(View.INVISIBLE);
            mSeekBar.setVisibility(View.INVISIBLE);

            mPlayBtn.setVisibility(View.INVISIBLE);

        }else{
            Toast.makeText(getActivity(),"Record can not be  deleted!!",Toast.LENGTH_SHORT).show();

        }
    }
    @Override
    public void getImagePath(Uri imagePath) {
        Log.d(TAG, "getImagePath: setting the image to imageview");
        UniversalImageLoader.setImage(imagePath.toString(), mPostImage);
        //assign to global variable
        mSelectedBitmap = null;
        mSelectedUri = imagePath;
    }
    @Override
    public void getImageBitmap(Bitmap bitmap) {
        Log.d(TAG, "getImageBitmap: setting the image to imageview");
        mPostImage.setImageBitmap(bitmap);
        //assign to a global variable
        mSelectedUri = null;
        mSelectedBitmap = bitmap;
    }
}
