package com.burakdal.voiceproject.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.burakdal.voiceproject.R;
import com.burakdal.voiceproject.UniversalImageLoader;
import com.burakdal.voiceproject.models.ClusterMarker;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class ClusterManagerRenderer extends DefaultClusterRenderer<ClusterMarker>  {
    private final String TAG="ClusterManagerRenderer";
    private final IconGenerator icongenerator;
    private final ImageView imageView;
    private final int markerWidth;
    private final int markerHeight;
    private final Context context;




    private DisplayImageOptions options;
    private ImageLoader mImageLoader;
    private GoogleMap mMap;

    private MarkerOptions marketoption;




    public ClusterManagerRenderer(Context context, GoogleMap map, ClusterManager<ClusterMarker> clusterManager) {
        super(context, map, clusterManager);

        this.context=context;
        this.mMap=map;

        icongenerator=new IconGenerator(context.getApplicationContext());
        imageView=new ImageView(context.getApplicationContext());
        markerWidth=(int)context.getResources().getDimension(R.dimen.custom_marker_image);
        markerHeight=(int)context.getResources().getDimension(R.dimen.custom_marker_image);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(markerWidth,markerHeight));
        int padding=(int)context.getResources().getDimension(R.dimen.custom_marker_padding);
        imageView.setPadding(padding,padding,padding,padding);
        icongenerator.setContentView(imageView);
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.music_holder)
                .showImageForEmptyUri(R.drawable.music_holder)
                .showImageOnFail(R.drawable.music_holder)
                .cacheInMemory(false)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .build();
    }




    @Override
    protected void onBeforeClusterItemRendered(ClusterMarker item, final MarkerOptions markerOptions) {


        Log.d(TAG,",img url :"+item.getImageurl());
        Picasso.get().load(item.getImageurl())
                .placeholder(R.drawable.music_holder)
                .resize(markerWidth,markerHeight)
                .into(imageView);





        Bitmap icon=icongenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createBitmap(icon))).title(item.getTitle());


    }


}
