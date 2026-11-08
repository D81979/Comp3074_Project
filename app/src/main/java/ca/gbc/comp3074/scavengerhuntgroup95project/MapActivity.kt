package ca.gbc.comp3074.scavengerhuntgroup95project

import android.Manifest
import android.os.Bundle
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.widget.Button
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import ca.gbc.comp3074.scavengerhuntgroup95project.data.HuntDatabase
import ca.gbc.comp3074.scavengerhuntgroup95project.data.PointOfInterest
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST = 100
    private var selectedPoint: PointOfInterest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        val pointId = intent.getLongExtra(
            "point_id",
            0L
        )
        val myplace = findViewById<FloatingActionButton>(R.id.float_btn_myplace)

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        }

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)

        // Toolbar - back button
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Map"
            setDisplayHomeAsUpEnabled(true)
        }

        // Get location from db
        lifecycleScope.launch {
            val pointWithTags =
                HuntDatabase
                    .getDatabase(this@MapActivity)
                    .huntDao()
                    .getPointWithTags(pointId)

            selectedPoint = pointWithTags?.point

            mapFragment.getMapAsync(this@MapActivity)
        }

        // Get directions from current location to selected destination
        mapFragment.getMapAsync(this)
        findViewById<Button>(R.id.btnDirections)
            .setOnClickListener {

                selectedPoint?.let { point ->
                    val destination = getLatLngFromAddress(point.address)
                    if (destination != null) {
                        val uri = "google.navigation:q=${destination.latitude},${destination.longitude}".toUri()
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            uri
                        )
                        intent.setPackage("com.google.android.apps.maps")
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this,
                            "Invalid location",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        // Go back to current user's location
        myplace.setOnClickListener {
            if (
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val currentLocation = LatLng(it.latitude, it.longitude)
                        mMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                currentLocation,
                                13f
                            )
                        )
                    }
                }
            }
        }

    }

    // Go back to previous Activity
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    // Display current location and destination AND get permissions
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            getCurrentLocation()
        }

        selectedPoint?.let { point ->
            val destination = getLatLngFromAddress(point.address)
            if (destination != null) {
                mMap.addMarker(
                    MarkerOptions()
                        .position(destination)
                        .title(point.name)
                )
                mMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(destination, 13f)
                )
            } else {
                Toast.makeText(
                    this,
                    "Invalid location",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @RequiresPermission(
        anyOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ]
    )
    private fun getCurrentLocation() {

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val currentLocation = LatLng(
                        location.latitude,
                        location.longitude
                    )
                    mMap.addMarker(
                    MarkerOptions()
                        .position(currentLocation)
                        .title("You")
                        .icon(
                            BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_BLUE
                            )
                        )
                    )
                }
            }
    }

    private fun getLatLngFromAddress(address: String): LatLng? {
        return try {
            val geocoder = Geocoder(this)

            val locations = geocoder.getFromLocationName(address, 1)

            if (!locations.isNullOrEmpty()) {
                LatLng(
                    locations[0].latitude,
                    locations[0].longitude
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}