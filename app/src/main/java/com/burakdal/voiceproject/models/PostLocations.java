package com.burakdal.voiceproject.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

@IgnoreExtraProperties
public class PostLocations implements Parcelable {
    private Post post;
    private User user;
    private @ServerTimestamp Date timeStamp;

    protected PostLocations(Parcel in) {
        post = in.readParcelable(Post.class.getClassLoader());
        user = in.readParcelable(User.class.getClassLoader());
        post_locations_id = in.readString();
    }

    public static final Creator<PostLocations> CREATOR = new Creator<PostLocations>() {
        @Override
        public PostLocations createFromParcel(Parcel in) {
            return new PostLocations(in);
        }

        @Override
        public PostLocations[] newArray(int size) {
            return new PostLocations[size];
        }
    };

    public String getPost_locations_id() {
        return post_locations_id;
    }

    public void setPost_locations_id(String post_locations_id) {
        this.post_locations_id = post_locations_id;
    }

    private GeoPoint location;
    private String post_locations_id;

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public PostLocations(Post post, User user, GeoPoint location, String id, Date date) {
        this.post = post;
        this.user = user;

        this.location = location;
        this.post_locations_id=id;
        this.timeStamp=date;
    }
    public PostLocations() {

    }





    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(post, flags);
        dest.writeParcelable(user, flags);
        dest.writeString(post_locations_id);
    }
}
