package de.kartheininger.markerclustering.config;

/**
 * Created by johannes on 21.08.13.
 */
public class Constants {
    // Set here the maximum values for phone and tablet for the markers in a cluster or in the map.
    // If the actual number of markers is exeeded then no markers will be displayed any more.
    // Above a certain threshold it simply does not make sence any more. Here you define the threshold.
    public static final int kNTMaxMarkersInClusterPhone = 80;
    public static final int kNTMaxMarkersInClusterTablet = 100;
    public static final int kNTMaxMarkersInMapPhone = 700;
    public static final int kNTMaxMarkersInMapTablet = 999;

    // This is the X and Y size of the bounding box around your marker view.
    public static final float kNTAnnotationSizeX = 100.0f;
    public static final float kNTAnnotationSizeY = 100.0f;

    // This is just to read in the test data in JSON format.
    // In your final implementation you won't need these keys any more.
    public static final String kAnnotationsKey = "annotations";
    public static final String kNameKey = "name";
    public static final String kPostcodeKey = "postcode";
    public static final String kCityKey = "city";
    public static final String kStreetKey = "street";
    public static final String kNumberKey = "number";
    public static final String kLatitudeKey = "lat";
    public static final String kLongitudeKey = "lon";


    //    To differentiate between normal marker and a clustermarker.
    //    Google Maps Marker doesn´t provide any extrafields to add to marker, so
    //    we use this "hack" to differentiate
    public static final String CLUSTERMARKER = "CLUSTERMARKER";


    // The following is a padding value.
    // It is used to add some padding when the user touches a cluster and the cluster is expanded.
    public static final int kNTMapPaddingValue = 200;


}
