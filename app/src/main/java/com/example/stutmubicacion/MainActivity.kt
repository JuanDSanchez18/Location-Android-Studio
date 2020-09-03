package com.example.stutmubicacion

import android.Manifest
import android.annotation.SuppressLint
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task

class MainActivity : AppCompatActivity() {

    private val tag = "MainActivity"
    private lateinit var latitudeText: TextView
    private lateinit var longitudeText: TextView

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    //private lateinit var builder: LocationSettingsRequest.Builder

    private lateinit var currentLocation: Location

    private val runningQOrLater =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        latitudeText = findViewById(R.id.latitude)
        longitudeText = findViewById(R.id.longitude)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (checkPermissions()){
            createLocationRequestAndcheckSettings()
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    // Update UI with location data
                    // ...
                    currentLocation = location
                    latitudeText.text = currentLocation.latitude.toString()
                    longitudeText.text = currentLocation.longitude.toString()
                }
            }
        }

    }

    private fun checkPermissions(): Boolean {
        if (runningQOrLater ) {
            ActivityCompat.requestPermissions(this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                21
            )
            val permissionAccessFineLocationApproved = ActivityCompat
                .checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
            val backgroundLocationPermissionApproved = ActivityCompat
                .checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED

            return permissionAccessFineLocationApproved && backgroundLocationPermissionApproved

        }else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                22
            )

            return ActivityCompat
                .checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
        }
        
    }


    private fun createLocationRequestAndcheckSettings() {
        locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }!!
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        //val builder = LocationSettingsRequest.Builder()
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnFailureListener { exception ->

            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(this@MainActivity,
                        29)//REQUEST_CHECK_SETTINGS
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                    sendEx.printStackTrace()
                    Log.d(tag, "Error getting location settings resolution: " + sendEx.message)
                }
            }
        }
        task.addOnSuccessListener { // locationSettingsResponse ->
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
            getLastLocation()
            //startLocationUpdates()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { Location : Location? ->
                // Got last known location. In some rare situations this can be null.
                if (Location!= null) {
                    // use your location object
                    currentLocation = Location
                    latitudeText.text = currentLocation.latitude.toString()
                    longitudeText.text = currentLocation.longitude.toString()

                }else{
                    latitudeText.text = getString(R.string.nulo)
                    longitudeText.text = getString(R.string.nulo)

                }
            }
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {

        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())

    }

    //You can comment this part if you don't want to stop in background
    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

}