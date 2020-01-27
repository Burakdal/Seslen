package com.burakdal.voiceproject.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Liking implements Parcelable {
    private User liker;
    private Post post;
    private @ServerTimestamp Date timestamp;
    private String likingId;

    public String getLikingId() {
        return likingId;
    }

    public void setLikingId(String likingId) {
        this.likingId = likingId;
    }

    public Liking(User liker, Post post, Date timestamp) {
        this.liker = liker;
        this.post = post;
        this.timestamp = timestamp;
    }

    public Liking(){

    }

    protected Liking(Parcel in) {
        liker = in.readParcelable(User.class.getClassLoader());
        post = in.readParcelable(Post.class.getClassLoader());
    }

    public static final Creator<Liking> CREATOR = new Creator<Liking>() {
        @Override
        public Liking createFromParcel(Parcel in) {
            return new Liking(in);
        }

        @Override
        public Liking[] newArray(int size) {
            return new Liking[size];
        }
    };

    public User getLiker() {
        return liker;
    }

    public void setLiker(User liker) {
        this.liker = liker;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(liker, flags);
        dest.writeParcelable(post, flags);
    }
}
