package com.kotlintask

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_location.*
import android.content.Intent
import android.view.View
import android.content.Context
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.content.pm.PackageManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.net.ConnectivityManager
import android.widget.Toast
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.support.v4.content.LocalBroadcastManager


class LocationActivity : AppCompatActivity() {

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    private var mAlreadyStartedService = false
    private var userName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        if (intent != null) {
            userName = intent.getStringExtra("userName")
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(
                object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        val latitude = intent.getStringExtra(EXTRA_LATITUDE)
                        val longitude = intent.getStringExtra(EXTRA_LONGITUDE)

                        if (latitude != null && longitude != null) {
                            start_service_text_view.text = (getString(R.string.msg_location_service_started) + "\n Latitude : " + latitude + "\n Longitude: " + longitude + userName)
                        }
                    }
                }, IntentFilter(ACTION_LOCATION_BROADCAST)
        )
        start_button.setOnClickListener {
            getCurrentLocation()
        }
        stop_button.setOnClickListener {
            stopService(Intent(this, LocationBackgroundService::class.java))
            mAlreadyStartedService = false
        }
    }

    private fun getCurrentLocation() {
        if (isGooglePlayServicesAvailable()) {
            startStep2(null)

        } else {
            Toast.makeText(applicationContext, R.string.no_google_playservice_available, Toast.LENGTH_LONG).show()
        }
    }


    private fun requestPermissions() {

        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)

        val shouldProvideRationale2 = ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)

        if (shouldProvideRationale || shouldProvideRationale2) {
            Log.i("request permission", "Displaying permission rationale to provide additional context.")
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, View.OnClickListener {
                ActivityCompat.requestPermissions(this@LocationActivity,
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                        REQUEST_PERMISSIONS_REQUEST_CODE)
            })
        } else {
            Log.i("request permission", "Requesting permission")
            ActivityCompat.requestPermissions(this@LocationActivity,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    REQUEST_PERMISSIONS_REQUEST_CODE)
        }
    }

    private fun showSnackbar(mainTextStringId: Int, actionStringId: Int,
                             listener: View.OnClickListener) {
        Snackbar.make(
                findViewById<View>(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show()
    }

    private fun checkPermissions(): Boolean {
        val permissionState1 = ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)

        val permissionState2 = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)

        return permissionState1 == PackageManager.PERMISSION_GRANTED && permissionState2 == PackageManager.PERMISSION_GRANTED

    }

    private fun isGooglePlayServicesAvailable(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(this)
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(this, status, 2404).show()
            }
            return false
        }
        return true
    }

    private fun startStep3() {

        if (!mAlreadyStartedService) {

            start_service_text_view.text = getString(R.string.start_service)
            val intent = Intent(this, LocationBackgroundService::class.java)
            startService(intent)

            mAlreadyStartedService = true

        }
    }

    private fun promptInternetConnect() {
        val builder = AlertDialog.Builder(this@LocationActivity)
        builder.setTitle(R.string.title_alert_no_intenet)
        builder.setMessage(R.string.msg_alert_no_internet)

        val positiveText = getString(R.string.btn_label_refresh)
        builder.setPositiveButton(positiveText,
                DialogInterface.OnClickListener { dialog, which ->

                    if (startStep2(dialog)) {

                        if (checkPermissions()) {
                            startStep3()
                        } else if (!checkPermissions()) {
                            requestPermissions()
                        }
                    }
                })

        val dialog = builder.create()
        dialog.show()
    }

    private fun startStep2(dialog: DialogInterface?): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo

        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected) {
            promptInternetConnect()
            return false
        }


        dialog?.dismiss()

        if (checkPermissions()) {
            startStep3()
        } else {
            requestPermissions()
        }
        return true
    }


    public override fun onDestroy() {

        stopService(Intent(this, LocationBackgroundService::class.java))
        mAlreadyStartedService = false

        super.onDestroy()
    }
}
