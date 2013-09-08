package de.kartheininger.markerclustering.markercluster;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashSet;

import de.kartheininger.markerclustering.R;
import de.kartheininger.markerclustering.config.Constants;

/*
*   Copyright (c) 2013 Kartheininger Johannes
*   Permission is hereby granted, free of charge, to any person obtaining a copy of this software
*   and associated documentation files (the "Software"), to deal in the Software without
*   restriction, including without limitation the rights to use, copy, modify, merge, publish,
*   distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
*   Software is furnished to do so, subject to the following conditions:
*   The above copyright notice and this permission notice shall be included in all copies or
*   substantial portions of the Software.
*
*   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
*   BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
*   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
*   DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
*   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
*/

public class ClusterObject extends MarkerObject{
    private static final String TAG = ClusterObject.class.getSimpleName();
    private final Context context;

    private HashSet<MarkerObject> clusteredMarkers;

    public ClusterObject(Context context) {
        super(context);
        this.clusteredMarkers = new HashSet<MarkerObject>();
        this.context = context;
    }

    public void addMarkerOptions(HashSet<MarkerObject> mark){
        for (MarkerObject markerObj : mark){
            clusteredMarkers.add(markerObj);
        }
    }

    public boolean isEmpty() {
        return clusteredMarkers.isEmpty();
    }

 @Override
    public LatLngBounds getBounds() {
        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        for (MarkerObject markerInCluster : clusteredMarkers) {
            bounds.include(markerInCluster.getPosition());
        }
        return bounds.build();
    }

    @Override
    public boolean isCluster(){
        return true;
    }

    @Override
    public LatLng getPosition() {
        float latitude = 0;
        float longitude = 0;

        for (MarkerObject allMarkerInCluster : clusteredMarkers) {
            latitude += allMarkerInCluster.getPosition().latitude;
            longitude += allMarkerInCluster.getPosition().longitude;
        }
        return new LatLng(latitude / clusteredMarkers.size(), longitude / clusteredMarkers.size());
    }

    @Override
    public HashSet<MarkerObject> getMarkers(){
        return clusteredMarkers;
    }

    @Override
    public MarkerOptions getMarkerOptions() {
        return new MarkerOptions().position(getPosition()).title(Constants.CLUSTERMARKER).icon(BitmapDescriptorFactory.fromBitmap(createDrawable(this.context)));
    }


    private Bitmap createDrawable(Context context) {
        View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.maps_custom_cluster, null);
        TextView numTxt = (TextView) view.findViewById(R.id.cluster_count_txt);
        numTxt.setText(String.valueOf(this.clusteredMarkers.size()));

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();

        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }
}
