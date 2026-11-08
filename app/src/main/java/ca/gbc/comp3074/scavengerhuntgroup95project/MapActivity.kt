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
import android.widget.Button
import androidx.core.net.toUri

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = "Map"
            setDisplayHomeAsUpEnabled(true)
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)

        findViewById<Button>(R.id.btnDirections)
            .setOnClickListener {

                val destinationLat = 43.6532
                val destinationLng = -79.3832

                val uri = "google.navigation:q=$destinationLat,$destinationLng".toUri()

                val intent = Intent(
                    Intent.ACTION_VIEW,
                    uri
                )

                intent.setPackage(
                    "com.google.android.apps.maps"
                )

                startActivity(intent)
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val toronto = LatLng(43.6532, -79.3832)
        mMap.addMarker(
            MarkerOptions()
                .position(toronto)
                .title("Toronto")
        )

        var currentLocation = LatLng(
            43.6756, -79.4106
        )

        mMap.addMarker(
            MarkerOptions()
                .position(currentLocation)
                .title("You")
        )

        mMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(currentLocation, 13f)
        )

//        fusedLocationClient.lastLocation
//            .addOnSuccessListener { location ->
//                var currentLocation = LatLng(
//                    43.6756, -79.4106
//                )
//
//                if (location != null) {
//                        currentLocation = LatLng(
//                        location.latitude, location.longitude
//                    )
//
//                    mMap.addMarker(
//                        MarkerOptions()
//                            .position(currentLocation)
//                            .title("You")
//                    )
//
//                    mMap.moveCamera(
//                        CameraUpdateFactory.newLatLngZoom(currentLocation, 5f)
//                    )
//                }
//
//            }
    }
}