package com.burakdal.voiceproject.models;

import android.os.Parcel;
import android.os.Parcelable;

public class FollowingNotifications implements Parcelable {
    private String followerId;
    private String followedId;

    public FollowingNotifications(String followerId, String followedId) {
        this.followerId = followerId;
        this.followedId = followedId;
    }

    protected FollowingNotifications(Parcel in) {
        followerId = in.readString();
        followedId = in.readString();
    }

    public static final Creator<FollowingNotifications> CREATOR = new Creator<FollowingNotifications>() {
        @Override
        public FollowingNotifications createFromParcel(Parcel in) {
            return new FollowingNotifications(in);
        }

        @Override
        public FollowingNotifications[] newArray(int size) {
            return new FollowingNotifications[size];
        }
    };

    public String getFollowerId() {
        return followerId;
    }

    public String getFollowedId() {
        return followedId;
    }

    public void setFollowerId(String followerId) {
        this.followerId = followerId;
    }

    public void setFollowedId(String followedId) {
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
