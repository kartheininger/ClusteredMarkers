package de.kartheininger.markerclustering;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import de.kartheininger.markerclustering.config.Constants;
import de.kartheininger.markerclustering.markercluster.ClusterObject;
import de.kartheininger.markerclustering.markercluster.MarkerObject;

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


public class MapsActivity extends AbstractMapActivity implements GoogleMap.OnMarkerClickListener {

    private static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap;
    private Context context;


    private HashSet<MarkerObject> setA = new HashSet<MarkerObject>();  // set of all annotations that may be displayed
    private HashSet<MarkerObject> setC = new HashSet<MarkerObject>();  // set of clusters to be displayed
    private HashSet<MarkerObject> setV = new HashSet<MarkerObject>();  // subset of A of those annotations that are visible within the bounds/span of the map window.

    private HashMap<String, LatLngBounds> markerReferenceList;

    private AsyncTask mTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_main);
        if (readyToGo()) {
            this.context = this;
            //Initiate a referenceList for later use to choose the right Bounds on MarkerClick
            this.markerReferenceList = new HashMap<String, LatLngBounds>();
            measureMap();
            setUpMapIfNeeded();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (readyToGo()) {
            setUpMapIfNeeded();
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not have been
     * completely destroyed during this process (it is likely that it would only be stopped or
     * paused), {@link #onCreate(Bundle)} may not be called again so we should call this method in
     * {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        // Just for the user's convenience place the map center to a place where the test data objects are located
        // and set some reasonable region.
        // The initial span may vary from iPhone to iPad
        //TODO: Smartphone Tablet Zoom wie Herrmann...

//        map.animateCamera(CameraUpdateFactory.zoomTo(zoom), 1500, null);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(48.697616, 9.165135), 7));
        mMap.setOnCameraChangeListener(getCameraChangeListener());
        mMap.setOnMarkerClickListener(this);

        // Read the sample data from json file and then add all to setA
        ArrayList<String[]> allLocations = readJson();

        // Create set A - with all annotations
        for (String[] location : allLocations) {
            String number, postcode, city, name, street;
            float lon, lat;
            //number      = location[0];
            postcode = location[1];
            city = location[2];
            name = location[3];
            lon = Float.parseFloat(location[4]);
            lat = Float.parseFloat(location[5]);
            //street      = location[6];

            MarkerObject markerObject = new MarkerObject(this);
            markerObject.addMarkerOptions(new MarkerOptions().position(new LatLng(lat, lon))
                    .title(name).snippet(postcode + " " + city));

            setA.add(markerObject);
            // We don´t want to add the marker here right now
        }
    }

    //Listener is called, when Cameraposition on the map changed.
    // Equivalent to regionDidChangeAnimated on iOS
    private GoogleMap.OnCameraChangeListener getCameraChangeListener() {
        return new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
//                if another asynctask has been started, cancel it, cause we don´t need his computation
//                anymore... cause we have a new cameraposition with new visible markers/clusters
//                and another problem could be that we can get a ConcurrentModificationException,
//                cause we change setC concurrent in more than one asynctask
                if (mTask != null){
                    mTask.cancel(true);
                }

                //Give the visible area to an async task and let them do almost the rest
                loadMarkersTask loader = new loadMarkersTask();
                mTask = loader.execute(mMap.getProjection().getVisibleRegion().latLngBounds);
            }
        };
    }

    //Here are finally our markers drawn onto the map
    //Get called from AsyncTask
    private void drawMapMarkers()
    {
        //Before adding any annotations remove the existing ones
        mMap.clear();

        //Clear our referenceList, so that we can fill it with the actual values
        markerReferenceList.clear();

        Marker markerReference;
        for (MarkerObject allObjects : setC) {
            markerReference = mMap.addMarker(allObjects.getMarkerOptions());
            markerReferenceList.put(markerReference.getId(), allObjects.getBounds());
        }
    }


    @Override
    public boolean onMarkerClick(final Marker marker) {
        if (marker.getTitle().equals(Constants.CLUSTERMARKER)){
            animateCameraTo(mMap, markerReferenceList.get(marker.getId()));
            return true;
        }
        //If clicked normal marker, do default-onMarkerClick
        return false;
    }


    private class loadMarkersTask extends AsyncTask<LatLngBounds, Void, Void>
    {
        @Override
        protected Void doInBackground(LatLngBounds... params) {
        LatLngBounds latLngBounds = params[0];
        visibleMarkers(latLngBounds);
        clusterMarkers(latLngBounds);
//            addAllMarkers();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            //All markers were read, our markeroptions are generated, and now lets finally draw them on the map :)
            drawMapMarkers();
        }


        // This method determines which markers of setA are visible within the current span of the mapView
        // It creates a new set with those markers and returns it.
        private void visibleMarkers(LatLngBounds mapBounds) {
            setV.clear();

            //Determine the bounding box which corresponds to the map views' currently spanned area.
//            LatLngBounds mapBounds = mMap.getProjection().getVisibleRegion().latLngBounds;

            int maxMarkersVisible;
            if (isTablet(context)) {
                maxMarkersVisible = Constants.kNTMaxMarkersInMapTablet;
            } else {
                maxMarkersVisible = Constants.kNTMaxMarkersInMapPhone;
            }

            for (MarkerObject allMarkerObjects : setA) {
                if (mapBounds.contains(allMarkerObjects.getPosition())) {
                    // Add the marker but check, whether the number of markers exceeds the maximum.
                    // In that case just empty the array/set and return.
                    if (setV.size() >= maxMarkersVisible) {
                        setV.clear();
                        return;
                    }
                    setV.add(allMarkerObjects);
                }
            }
        }


        private void clusterMarkers(LatLngBounds mapBounds) {
            // Empty the setC
            setC.clear();

            int maxMarkersInCluster;
            if (isTablet(context)) {
                maxMarkersInCluster = Constants.kNTMaxMarkersInClusterTablet;
            } else {
                maxMarkersInCluster = Constants.kNTMaxMarkersInClusterPhone;
            }

            for (MarkerObject marker : setV) {
                ClusterObject clusterFound = new ClusterObject(context);

                // Iterate over a copy of the set because we change the contents of
                // the set probably even several times per loop.
                HashSet<MarkerObject> copyOfSetC = new HashSet<MarkerObject>(setC);
                for (MarkerObject clusterMarker : copyOfSetC) {
                    if (geoTouchMarker(clusterMarker, marker, mapBounds)) {
                        // A match was found!
                        // If this is the first match then the marker just needs to be combined with the
                        // touching marker or cluster.
                        if (clusterFound.isEmpty()) {
                            // This is the first touch
                            // If the touching marker is a cluster then we just need to add the current
                            // marker. If it is a single marker then a new cluster must be created and added
                            // to setC while the single marker must be removed.
                            if (clusterMarker.isCluster()) {
                                // Remember the current cluster for potential unifications with more clusters or annotations
                                // that may be found touching marker
                                clusterFound = (ClusterObject) clusterMarker;

                                //Add the marker to the cluster
                                ((ClusterObject) clusterMarker).addMarkerOptions(marker.getMarkers());
                            } else {
                                // replace the single marker in setC by a new cluster.
                                ClusterObject newCluster = new ClusterObject(context);
                                newCluster.addMarkerOptions(clusterMarker.getMarkers());
                                newCluster.addMarkerOptions(marker.getMarkers());
                                clusterFound = newCluster;
                                setC.add(newCluster);
                                setC.remove(clusterMarker);
                                }
                        } else {
                            // This is a second (or more) touch.
                            // The touching marker or cluster needs to be combined with the cluster that
                            // has been found earlier and removed from setC.
                            clusterFound.addMarkerOptions(clusterMarker.getMarkers());
                            setC.remove(clusterMarker);
                        }
                    }   //touch was found
                }   //iteration over setC

                // if no touches were found then the marker needs to be added to setC as a single marker.
                if (clusterFound.isEmpty()) {
                    setC.add(marker);
                } else {
                    // If a cluster was found then the number of markers in that cluster was increased, regardless
                    // whether a single marker was added or a number of clusters and markers were unified.
                    // If now the maximum number of markers in a cluster is exeeded then no marker is to be
                    // shown at all.
                    // Therefore empty setV and return.
                    if (clusterFound.getMarkers().size() >= maxMarkersInCluster) {
                        setV.clear();
                        return;
                    }

                }   //iteration over setV
            }
        }

        private boolean geoTouchMarker(MarkerObject firstMarker, MarkerObject secondMarker, LatLngBounds mapBounds) {

            double latDiff = Math.abs(firstMarker.getPosition().latitude - secondMarker.getPosition().latitude);
            double lonDiff = Math.abs(firstMarker.getPosition().longitude - secondMarker.getPosition().longitude);

            float itemsVertical = mapsFragmentHeight / Constants.kNTAnnotationSizeY;
            float itemsHorizontal = mapsFragmentWidth / Constants.kNTAnnotationSizeX;

            double latitudeDelta = Math.abs(mapBounds.southwest.latitude - mapBounds.northeast.latitude);
            double longitudeDelta = Math.abs(mapBounds.southwest.longitude - mapBounds.northeast.longitude);

            boolean doesTouch = (latitudeDelta / itemsVertical > latDiff) && (longitudeDelta / itemsHorizontal > lonDiff);
            return doesTouch;
        }

    }



}




