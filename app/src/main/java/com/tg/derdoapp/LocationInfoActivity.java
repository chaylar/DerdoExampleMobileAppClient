package com.tg.derdoapp;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.gson.Gson;
import com.tg.globals.AppGlobals;
import com.tg.helper.RequestHelper;
import com.tg.requestManager.HttpMethods;
import com.tg.requestManager.ServiceResponseVO;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class LocationInfoActivity extends BaseActivity implements android.location.LocationListener {

    private static final int PERMISSION_REQUEST_CODE = 1001;

    private static final int REQUEST_CHECK_SETTINGS = 1002;
    private static final String TAG = "LOC_ST";

    private double latitude = 0;
    private double longitude = 0;

    private LocationManager locationManager;

    public LocationInfoActivity() {
        super(R.layout.activity_location_info);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.w("LOC_OR", "onCreate");

        Boolean isGranted = PermissionsGranted();
        if(!isGranted) {
            RequestPermissions();
            return;
        }

        statusCheck();
    }

    private void RequestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
    }

    private void RedirectToShowCase() {
        android.content.Intent redirect = new android.content.Intent(getBaseContext(), ShowCaseActivity.class);
        startActivity(redirect);
    }

    private void RedirectToMain() {
        android.content.Intent redirect = new android.content.Intent(getBaseContext(), MainActivity.class);
        startActivity(redirect);
    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
        else {
            GetLocationAndSave();
        }
    }

    private boolean getLocationInfo() throws Exception {

        if (androidx.core.app.ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && androidx.core.app.ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                return false;
            }

            Location location = null;
            List<String> providers = locationManager.getAllProviders();
            for (String provider : providers) {
                locationManager.requestLocationUpdates(provider, 10000L, 5F, this);
                Location l = locationManager.getLastKnownLocation(provider);
                if (l == null) {
                    continue;
                }
                if (location == null || l.getAccuracy() < location.getAccuracy()) {
                    // Found best last known location: %s", l);
                    location = l;
                }
            }

            if(location == null) {
                //throw new Exception("LOCATION IS NULL");
                Log.w("LOC_OR", "LOCATION IS NULL");
                return false;
            }

            try {
                latitude = location.getLatitude();
                longitude = location.getLongitude();

            } catch (NullPointerException e) {
                throw e;
            }

            Log.d("Location.lat", String.valueOf(latitude));
            Log.d("Location.lon", String.valueOf(longitude));

            return true;
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.w("LOC_SET", "displayLocationSettingsRequest");
                    buildAlertMessageNoGps();
                }
                break;
            }
        }
    }

    private ServiceResponseVO saveLocationInfo(double lat, double lon) throws Exception {
        HashMap<String, String> params = new HashMap<>();
        params.put("lat", String.valueOf(lat));
        params.put("lon", String.valueOf(lon));

        RequestHelper rh = new RequestHelper(getApplicationContext());
        ServiceResponseVO resultVO = rh.sendRequest("/login/savelocation", params, HttpMethods.POST);

        return resultVO;
    }

    private void GetLocationAndSave() {
        try {
            boolean gotLocationInfo = getLocationInfo();
            Log.w("gotLocationInfo", String.valueOf(gotLocationInfo));
            if(gotLocationInfo) {
                ServiceResponseVO srvo = saveLocationInfo(latitude, longitude);
                if(srvo != null && srvo.success) {
                    AppGlobals.hasLocationInfo = true;
                    RedirectToShowCase();
                }
            }
            else {
                buildAlertMessageNoGps();
                //TODO : COULDN'T GET LAT LON
            }
        } catch (Exception ex) {
            Log.e("LOC_OR", ex.getMessage() != null ? ex.getMessage() : "MESSAGE_WAS_NULL");
            ShowErrorToast(ex);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.w("LOC_OR", "onActivityResult");

        if(requestCode == REQUEST_CHECK_SETTINGS) {
            //RedirectToMain();
            statusCheck();
        }
        //statusCheck();
    }

    private Boolean PermissionsGranted() {
        if (androidx.core.app.ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && androidx.core.app.ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        return false;
    }

    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(LocationInfoActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    private void buildAlertMessageNoGps() {

        displayLocationSettingsRequest(this);

        /*final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Your GPS seems to be disabled, do you want to enable it?
        builder.setMessage(getResources().getString(R.string.location_info_popup_warning))
                .setCancelable(false)
                .setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        ShowErrorToast(getResources().getString(R.string.location_info_popup_warning));
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();*/
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            ServiceResponseVO srvo = saveLocationInfo(location.getLatitude(), location.getLongitude());
            if(srvo != null && srvo.success) {
                AppGlobals.hasLocationInfo = true;

                locationManager.removeUpdates(this);

                RedirectToShowCase();
            }
        }
        catch (Exception e) {
            Log.w("LOC_OR", e.getMessage() != null ? e.getMessage() : "onLocationChanged.E");
        }

        Log.i("LOC_OR: ","Location changed, " + location.getAccuracy() + " , " + location.getLatitude()+ "," + location.getLongitude());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.i("LOC_OR", "onStatusChanged");
        statusCheck();
    }

    @Override
    public void onProviderEnabled(String s) {
        Log.i("LOC_OR", "onProviderEnabled");
        statusCheck();
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.i("LOC_OR", "onProviderDisabled");
        statusCheck();
    }
}
