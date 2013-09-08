package de.kartheininger.markerclustering;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

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

public class AbstractMapActivity extends FragmentActivity {

    protected static final String TAG_ERROR_DIALOG_FRAGMENT="errorDialog";

    private static final String TAG = AbstractMapActivity.class.getSimpleName() ;
    private static final int IO_BUFFER_SIZE = 4 * 1024;

    int mapsFragmentWidth;
    int mapsFragmentHeight;

    protected boolean readyToGo() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (status == ConnectionResult.SUCCESS) {
            if (getVersionFromPackageManager(this) >= 2) {
                return(true);
            }
            else {
                Toast.makeText(this, R.string.activity_maps_noMaps, Toast.LENGTH_LONG).show();
                finish();
            }
        }
        else if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
            ErrorDialogFragment.newInstance(status)
                    .show(getSupportFragmentManager(),
                            TAG_ERROR_DIALOG_FRAGMENT);
        }
        else {
            Toast.makeText(this, R.string.activity_maps_noMaps, Toast.LENGTH_LONG).show();
            finish();
        }

        return(false);
    }

    public static class ErrorDialogFragment extends DialogFragment {
        static final String ARG_STATUS="status";

        static ErrorDialogFragment newInstance(int status) {
            Bundle args=new Bundle();

            args.putInt(ARG_STATUS, status);

            ErrorDialogFragment result=new ErrorDialogFragment();

            result.setArguments(args);

            return(result);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle args=getArguments();

            return GooglePlayServicesUtil.getErrorDialog(args.getInt(ARG_STATUS),
                    getActivity(), 0);
        }

        @Override
        public void onDismiss(DialogInterface dlg) {
            if (getActivity() != null) {
                getActivity().finish();
            }
        }
    }

    // following from
    // https://android.googlesource.com/platform/cts/+/master/tests/tests/graphics/src/android/opengl/cts/OpenGlEsVersionTest.java

  /*
   * Copyright (C) 2010 The Android Open Source Project
   *
   * Licensed under the Apache License, Version 2.0 (the
   * "License"); you may not use this file except in
   * compliance with the License. You may obtain a copy of
   * the License at
   *
   * http://www.apache.org/licenses/LICENSE-2.0
   *
   * Unless required by applicable law or agreed to in
   * writing, software distributed under the License is
   * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
   * CONDITIONS OF ANY KIND, either express or implied. See
   * the License for the specific language governing
   * permissions and limitations under the License.
   */

    private static int getVersionFromPackageManager(Context context) {
        PackageManager packageManager = context.getPackageManager();
        FeatureInfo[] featureInfos  = packageManager.getSystemAvailableFeatures();
        if (featureInfos != null && featureInfos.length > 0) {
            for (FeatureInfo featureInfo : featureInfos) {
                // Null feature name means this feature is the open
                // gl es version feature.
                if (featureInfo.name == null) {
                    if (featureInfo.reqGlEsVersion != FeatureInfo.GL_ES_VERSION_UNDEFINED) {
                        return getMajorVersion(featureInfo.reqGlEsVersion);
                    }
                    else {
                        return 1; // Lack of property means OpenGL ES
                        // version 1
                    }
                }
            }
        }
        return 1;
    }

    /** @see FeatureInfo#getGlEsVersion() */
    private static int getMajorVersion(int glEsVersion) {
        return((glEsVersion & 0xffff0000) >> 16);
    }



    // Read the sample data from json file into dictionary and array.
    //Get Data From Text Resource File Contains Json Data.

    protected ArrayList<String[]> readJson() {
        ArrayList<String[]> data = new ArrayList<String[]>();

        BufferedInputStream inputStream = new BufferedInputStream( getResources().openRawResource(R.raw.annotationlocationsjson), IO_BUFFER_SIZE);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            copy (inputStream, outputStream);
            outputStream.flush();
        } catch (IOException e) {
            Log.e(TAG, "Could not load json from resources ");
        } finally {
            closeStream(inputStream);
        }

        try {
            // Parse the data into jsonobject to get original data in form of json.
            // You could also use some library like gson for mapping json to objects
            JSONObject jObject = new JSONObject(outputStream.toString());
            JSONArray jArray = jObject.getJSONArray(Constants.kAnnotationsKey);

            String number, postcode, city, name, lon, lat, street;

            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jsonTmp = jArray.getJSONObject(i);
                number      = jsonTmp.getString(Constants.kNumberKey);
                postcode    = jsonTmp.getString(Constants.kPostcodeKey);
                city        = jsonTmp.getString(Constants.kCityKey);
                name        = jsonTmp.getString(Constants.kNameKey);
                lat         = jsonTmp.getString(Constants.kLatitudeKey);
                lon         = jsonTmp.getString(Constants.kLongitudeKey);
                street      = jsonTmp.getString(Constants.kStreetKey);
                data.add(new String[] {number, postcode, city, name, lon, lat, street});
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeStream(outputStream);
        }
        return data;
    }


    /**
     * Closes the specified stream.
     *
     * @param stream The stream to close.
     */
    private static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                android.util.Log.e(TAG, "Could not close stream", e);
            }
        }
    }

    /**
     * Copy the content of the input stream into the output stream, using a
     * temporary byte array buffer whose size is defined by
     * {@link #IO_BUFFER_SIZE}.
     *
     * @param in The input stream to copy from.
     * @param out The output stream to copy to.
     * @throws IOException If any error occurs during the copy.
     */
    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[IO_BUFFER_SIZE];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }



//    "Workaround" for how to determine if the app is on a tablet or a phone
protected boolean isTablet(Context context) {
    return (context.getResources().getConfiguration().screenLayout
            & Configuration.SCREENLAYOUT_SIZE_MASK)
            >= Configuration.SCREENLAYOUT_SIZE_LARGE;
       /* return getResources().getBoolean(R.bool.isTablet);*/
    }




    //    Cannot be null

    void measureMap() {
        findViewById(R.id.map).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final int fragmentWidth = findViewById(R.id.map).getWidth();
                if (fragmentWidth != 0){
                    mapsFragmentWidth =  findViewById(R.id.map).getWidth();
                    mapsFragmentHeight =  findViewById(R.id.map).getHeight();
                }
            }
        });
    }



    protected void animateCameraTo(GoogleMap ourMap, LatLngBounds latLngBounds ) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, Constants.kNTMapPaddingValue);
        ourMap.animateCamera(cameraUpdate);
    }

}
