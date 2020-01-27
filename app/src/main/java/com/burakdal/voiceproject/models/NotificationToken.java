package com.burakdal.voiceproject.models;

import android.os.Parcel;
import android.os.Parcelable;

public class NotificationToken implements Parcelable {
    private String token;

    public NotificationToken(){

    }

    protected NotificationToken(Parcel in) {
        token = in.readString();
    }

    public static final Creator<NotificationToken> CREATOR = new Creator<NotificationToken>() {
        @Override
        public NotificationToken createFromParcel(Parcel in) {
            return new NotificationToken(in);
        }

        @Override
        public NotificationToken[] newArray(int size) {
            return new NotificationToken[size];
        }
    };

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public NotificationToken(String token) {
        this.token = token;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(token);
    }
}
