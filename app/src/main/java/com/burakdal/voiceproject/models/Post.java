package com.burakdal.voiceproject.models;

import android.os.Parcel;
import android.os.Parcelable;


import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

@IgnoreExtraProperties
public class Post  implements Parcelable{

    private String post_id;
    private String user_id;
    private String image;
    private String audio;
    private String title;
    private String description;
    private @ServerTimestamp Date timeStamp;
    private String placeId;

    protected Post(Parcel in) {
        post_id = in.readString();
        user_id = in.readString();
        image = in.readString();
        audio = in.readString();
        title = in.readString();
        description = in.readString();
        placeId=in.readString();

    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };


    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public Post(String post_id, String user_id, String image, Date date, String audio, String title, String description, String placeid) {
        this.post_id = post_id;
        this.user_id = user_id;
        this.image = image;
        this.audio = audio;
        this.title = title;
        this.description = description;
        this.timeStamp=date;
        this.placeId=placeid;



    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public Post() {

    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }



    @Override
    public String toString() {
        return "Post{" +
                "post_id='" + post_id + '\'' +
                ", user_id='" + user_id + '\'' +
                ", image='" + image + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", placeId='" + placeId + '\'' +






                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(post_id);
        dest.writeString(user_id);
        dest.writeString(image);
        dest.writeString(audio);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(placeId);

    }
}
