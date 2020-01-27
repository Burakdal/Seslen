package com.burakdal.voiceproject.models;

import android.os.Parcel;
import android.os.Parcelable;

public class FollowingConnections implements Parcelable {
    private String followerId;
    private String followedId;

    protected FollowingConnections(Parcel in) {
        followerId = in.readString();
        followedId = in.readString();
    }

    public static final Creator<FollowingConnections> CREATOR = new Creator<FollowingConnections>() {
        @Override
        public FollowingConnections createFromParcel(Parcel in) {
            return new FollowingConnections(in);
        }

        @Override
        public FollowingConnections[] newArray(int size) {
            return new FollowingConnections[size];
        }
    };

    public void setFollowerId(String followerId) {
        this.followerId = followerId;
    }

    public void setFollowedId(String followedId) {
        this.followedId = followedId;
    }

    public String getFollowerId() {
        return followerId;
    }

    public String getFollowedId() {
        return followedId;
    }

    public FollowingConnections(String followerId, String followedId) {
        this.followerId = followerId;
        this.followedId = followedId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(followerId);
        dest.writeString(followedId);
    }
}
