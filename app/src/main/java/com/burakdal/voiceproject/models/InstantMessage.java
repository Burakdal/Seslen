package com.burakdal.voiceproject.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
@IgnoreExtraProperties
public class InstantMessage implements Parcelable {

    private String message;
    private String messageId;
    private String author;
    private String voiceUrl;


    protected InstantMessage(Parcel in) {
        message = in.readString();
        messageId = in.readString();
        author = in.readString();
        voiceUrl = in.readString();
    }

    public static final Creator<InstantMessage> CREATOR = new Creator<InstantMessage>() {
        @Override
        public InstantMessage createFromParcel(Parcel in) {
            return new InstantMessage(in);
        }

        @Override
        public InstantMessage[] newArray(int size) {
            return new InstantMessage[size];
        }
    };

    public String getVoiceUrl() {
        return voiceUrl;
    }

    public void setVoiceUrl(String voiceUrl) {
        this.voiceUrl = voiceUrl;
    }



    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }






    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    private @ServerTimestamp Date timestamp;

     public String getMessage() {
         return message;
     }

     public void setMessage(String message) {
         this.message = message;
     }

     public String getAuthor() {
         return author;
     }

     public void setAuthor(String author) {
         this.author = author;
     }



    public InstantMessage(String message, String author) {
        this.message = message;
        this.author = author;
    }

    public InstantMessage() {



    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(message);
        dest.writeString(messageId);
        dest.writeString(author);
        dest.writeString(voiceUrl);
    }
}
