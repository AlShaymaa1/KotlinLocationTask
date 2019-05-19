package com.kotlintask

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import android.support.v4.content.LocalBroadcastManager
import android.util.Log


const val ACTION_LOCATION_BROADCAST = "LocationBroadcast"
const val EXTRA_LATITUDE = "extra_latitude"
const val EXTRA_LONGITUDE = "extra_longitude"

class LocationBackgroundService : Service(),
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private val TAG = LocationBackgroundService::class.java.simpleName
    private val mLocationClient by lazy {
        GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
    }
    private val mLocationRequest by lazy {
        LocationRequest()
    }

    private val LOCATION_INTERVAL = 6000000000;
    private val FASTEST_LOCATION_INTERVAL = 90000000000;


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        mLocationRequest.interval = LOCATION_INTERVAL;
        mLocationRequest.fastestInterval = FASTEST_LOCATION_INTERVAL;
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
        mLocationClient.connect();
        return START_STICKY;

    }

    override fun onBind(intent: Intent): IBinder? {
        return null;
    }

    override fun onConnected(p0: Bundle?) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG, "== Error On onConnected() Permission not granted");

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, this);

        Log.d(TAG, "Connected to Google API");
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.d(TAG, "Connection suspended");
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.d(TAG, "Failed to connect to Google API");
    }

    override fun onLocationChanged(location: Location?) {
        sendMessageToUI((location?.latitude.toString()), (location?.longitude.toString()));
    }


    private fun sendMessageToUI(lat: String, lng: String) {

        Log.d(TAG, "Sending info...")

        val intent = Intent(ACTION_LOCATION_BROADCAST)
        intent.putExtra(EXTRA_LATITUDE, lat)
        intent.putExtra(EXTRA_LONGITUDE, lng)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

    }

}