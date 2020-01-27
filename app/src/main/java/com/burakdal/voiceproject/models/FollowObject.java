package com.burakdal.voiceproject.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;


@IgnoreExtraProperties
public class FollowObject implements Parcelable {
    private String follower_id;
    private String followed_id;
    private @ServerTimestamp Date timeStamp;

    public FollowObject(String follower_id, String followed_id, Date timeStamp) {
        this.follower_id = follower_id;
        this.followed_id = followed_id;
        this.timeStamp = timeStamp;
    }

    protected FollowObject(Parcel in) {
        follower_id = in.readString();
        followed_id = in.readString();
    }

    public static final Creator<FollowObject> CREATOR = new Creator<FollowObject>() {
        @Override
        public FollowObject createFromParcel(Parcel in) {
            return new FollowObject(in);
        }

        @Override
        public FollowObject[] newArray(int size) {
            return new FollowObject[size];
        }
    };

    public String getFollower_id() {
        return follower_id;
    }

    public void setFollower_id(String follower_id) {
        this.follower_id = follower_id;
    }

    public String getFollowed_id() {
        return followed_id;
    }

    public void setFollowed_id(String followed_id) {
        this.followed_id = followed_id;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public FollowObject(){

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(follower_id);
        dest.writeString(followed_id);
    }
}
