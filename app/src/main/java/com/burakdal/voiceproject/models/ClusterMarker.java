package com.burakdal.voiceproject.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class ClusterMarker implements ClusterItem {
    private String snipped;



    public void setSnipped(String snipped) {
        this.snipped = snipped;
    }

    private LatLng position;
    private String title;
    private String imageurl;
    private Post post;
    private User user;

    public ClusterMarker(LatLng position, String title,String snipped, String imageUrl, Post post,User user) {
        this.position = position;
        this.title = title;
        this.imageurl = imageUrl;
        this.snipped=snipped;
        this.post = post;
        this.user=user;
    }
    public ClusterMarker() {

    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snipped;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }
}
