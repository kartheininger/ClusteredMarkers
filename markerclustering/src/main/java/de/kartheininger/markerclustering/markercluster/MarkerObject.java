package de.kartheininger.markerclustering.markercluster;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashSet;

import de.kartheininger.markerclustering.R;

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


public class MarkerObject {
    private static final String TAG = MarkerObject.class.getSimpleName();
    private final Context context;

    private LatLng position;
    private String title;
    private String snippet;


    public MarkerObject(Context context) {
        this.context = context;
    }

    public void addMarkerOptions(MarkerOptions markerOptions) {
        this.position = markerOptions.getPosition();
        this.title = markerOptions.getTitle();
        this.snippet = markerOptions.getSnippet();
    }

    public LatLngBounds getBounds() {
        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        bounds.include(position);
        return bounds.build();
    }

    public boolean isCluster() {
        return false;
    }


    public LatLng getPosition() {
        return position;
    }

    public HashSet<MarkerObject> getMarkers() {

        HashSet<MarkerObject> singleSet = new HashSet<MarkerObject>();
        singleSet.add(this);
        return singleSet;
    }


    public MarkerOptions getMarkerOptions() {
        return new MarkerOptions().title(title).snippet(snippet).position(position).icon(BitmapDescriptorFactory.fromBitmap(createDrawable(this.context)));
    }

    private Bitmap createDrawable(Context context) {
        View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.maps_custom_marker, null);
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
