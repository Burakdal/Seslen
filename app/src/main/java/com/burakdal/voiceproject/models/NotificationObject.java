package com.burakdal.voiceproject.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

@IgnoreExtraProperties
public class NotificationObject implements Parcelable {
    private User sender;
    private User sended;
    private String message;
    private @ServerTimestamp Date timestamp;

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }



    public NotificationObject(User sender, User sended, String message) {
        this.sender = sender;
        this.sended = sended;
        this.message = message;
    }
    public NotificationObject(){

    }

    protected NotificationObject(Parcel in) {
        sender = in.readParcelable(User.class.getClassLoader());
        sended = in.readParcelable(User.class.getClassLoader());
        message = in.readString();
    }

    public static final Creator<NotificationObject> CREATOR = new Creator<NotificationObject>() {
        @Override
        public NotificationObject createFromParcel(Parcel in) {
            return new NotificationObject(in);
        }

        @Override
        public NotificationObject[] newArray(int size) {
            return new NotificationObject[size];
        }
    };

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getSended() {
        return sended;
    }

    public void setSended(User sended) {
        this.sended = sended;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(sender, flags);
        dest.writeParcelable(sended, flags);
        dest.writeString(message);
    }
}
