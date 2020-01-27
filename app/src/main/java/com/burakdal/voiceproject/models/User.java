package com.burakdal.voiceproject.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.ArrayList;

@IgnoreExtraProperties
public class User implements Parcelable {

    private String user_id;
    private String name;
    private String email;

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };





    private String descriptionVoice;






    public void setEmail(String email) {
        this.email = email;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    private String description;
    private String profileImage;

    protected User(Parcel in) {
        user_id = in.readString();
        name = in.readString();
        email = in.readString();
        descriptionVoice = in.readString();
        profileImage = in.readString();
    }


    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void setDescriptionVoice(String descriptionVoice) {
        this.descriptionVoice = descriptionVoice;
    }

    public String getDescriptionVoice() {
        return descriptionVoice;
    }

    public User(String user_id, String name) {
        this.user_id = user_id;
        this.name = name;
    }

    public User() {

    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user_id);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(descriptionVoice);
        dest.writeString(description);
        dest.writeString(profileImage);
    }
}
