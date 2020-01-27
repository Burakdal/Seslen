package com.burakdal.voiceproject;

import android.support.v4.app.Fragment;

import com.burakdal.voiceproject.models.Post;
import com.burakdal.voiceproject.models.User;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public interface IMainActivity {

    void inflateViewPostFragment(Post post);
    void inflateEditFragment(User user);
    void onBackPressed();
    void inflateViewProfile(User user);
    User getUser();
    void inflateMyProfileFragment();
    void inflateHomeFragment();
    void inflatePostFragment();
    void inflateNotificationFragment();
    void inflateSearchFragment();
    void inflateFollowingListFragment(String userId);
    void inflateFollowerListFragment(String userId);
    void inflateMainMessageFragment(User user);
    void inflateMessageThreads();
    void inflateCommentThread(String postId,User postUser);




}
