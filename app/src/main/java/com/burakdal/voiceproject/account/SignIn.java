package com.burakdal.voiceproject.account;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.burakdal.voiceproject.MainActivity;
import com.burakdal.voiceproject.R;
import com.burakdal.voiceproject.UniversalImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

public class SignIn extends AppCompatActivity {
    private final static String TAG="LogIn";
    private FirebaseAuth.AuthStateListener mAuthListener;
    private EditText mPassword;
    private AutoCompleteTextView mEmail;
    private Button mSignInBtn,mSignUpBtn;
    private FirebaseAuth mAuth;
    private ProgressBar mProgressBar;
    private ImageView mLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);





        mEmail = (AutoCompleteTextView) findViewById(R.id.emailView);
        mPassword = (EditText) findViewById(R.id.passwordView);
        mSignInBtn = (Button) findViewById(R.id.signIn);
        mSignUpBtn = (Button) findViewById(R.id.signUp);
        mProgressBar=(ProgressBar) findViewById(R.id.proBar);
        mLogo=(ImageView)findViewById(R.id.login_logo);




        mAuth = FirebaseAuth.getInstance();

        initImageLoader();

        setUpFireBaseAuth();




        mPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == R.integer.login || actionId == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }

                return false;
            }
        });

        UniversalImageLoader.setImage("assets://login_logo.png", mLogo);
        hideSoftKeyboard();


    }

    private void setUpFireBaseAuth() {
        Log.d("VoiceProject", "setupFirebaseAuth: started");

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    //check if email is verified

                    Log.d("VoiceProject", "onAuthStateChanged: signed_in: " + user.getUid());
                    Toast.makeText(SignIn.this, "Authenticated with: " + user.getEmail(), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(SignIn.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();



                } else {
                    // User is signed out
                    Log.d("VoiceProject", "onAuthStateChanged: signed_out");
                }
                // ...
            }
        };
    }

    private void attemptLogin() {

            String email=mEmail.getText().toString();
            String password=mPassword.getText().toString();

            if (email.equals("") || password.equals("")) return;

            Toast.makeText(SignIn.this, "Login Processing", Toast.LENGTH_SHORT).show();

            showProgressBar();

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()) {

                        createErrorDialog();
                        hideProgressBar();
                    } else {
                        addDeviceToken();
                        hideProgressBar();
                        Intent intent = new Intent(SignIn.this, MainActivity.class);
                        finish();
                        startActivity(intent);


                    }

                }
            });
        }
    public void loginExistingUser(View v) {


        attemptLogin();
    }

    public void register(View v){
        Intent intent = new Intent(SignIn.this, RegisterActivity.class);
        finish();
        startActivity(intent);
    }
    private void addDeviceToken(){
        if (FirebaseAuth.getInstance().getCurrentUser()!=null){
            Log.d(TAG,"current user is not null ");
            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                @Override
                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                    if (task.isSuccessful()){
                        Log.d(TAG,"token task is succesfull");
                        String token=task.getResult().getToken();
                        Log.d(TAG,"TOKEN :"+token);
                        SharedPreferences pref=getSharedPreferences("login",0);
                        pref.edit().putString("token",token).apply();
                        FirebaseMessaging.getInstance().subscribeToTopic("voice").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(SignIn.this, "subscribed to topic", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }
            });
        }



    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        }
    }

    private void showProgressBar(){
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){
        if (mProgressBar.getVisibility()==View.VISIBLE){
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void initImageLoader(){
        UniversalImageLoader imageLoader = new UniversalImageLoader(SignIn.this);
        ImageLoader.getInstance().init(imageLoader.getConfig());
    }

    private void createErrorDialog() {
        AlertDialog.Builder builder=new AlertDialog.Builder(SignIn.this);
        builder.setTitle("Ooops");
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setMessage("There is a problem in connection");
        builder.setPositiveButton(android.R.string.ok,null);
        builder.show();


    }
    private void saveUsernamePassword(){
        String email=mEmail.getText().toString();
        String password=mPassword.getText().toString();

        SharedPreferences prefer=getSharedPreferences("LoginPrefs",0);
        prefer.edit().putString("email",email).putString("password",password).apply();
    }
}
