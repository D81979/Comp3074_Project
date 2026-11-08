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
import androidx.lifecycle.lifecycleScope
import ca.gbc.comp3074.scavengerhuntgroup95project.data.HuntDatabase
import ca.gbc.comp3074.scavengerhuntgroup95project.data.PointOfInterest
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
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
        }

        // Get directions from current location to selected destination
        mapFragment.getMapAsync(this)
        findViewById<Button>(R.id.btnDirections)
            .setOnClickListener {

                //----------- (Uncomment in final implementation)
//                val point = selectedPoint
//
//                if (
//                    point?.latitude != null &&
//                    point.longitude != null
//                ) {
//
//                    val destinationLat = point.latitude
//                    val destinationLng = point.longitude

                    val destinationLat = 43.6532
                    val destinationLng = -79.3832

                    val uri = "google.navigation:q=$destinationLat,$destinationLng".toUri()

                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        uri
                    )

                    intent.setPackage("com.google.android.apps.maps")
                    startActivity(intent)
                //}
            }

        // Go back to current user's location
        myplace.setOnClickListener {
            val currentLocation = LatLng(
                43.6756, -79.4106
            )

            mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    currentLocation,
                    13f
                )
            )
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

        //--------- Get point's location (Uncomment in final implementation)
//        val point = selectedPoint
//        if (
//            point?.latitude != null &&
//            point.longitude != null
//        ) {
//            // Get points location
//            val destination = LatLng(
//                point.latitude, point.longitude
//            )

        val destination = LatLng(
            43.6532, -79.3832
        )

        mMap.addMarker(
            MarkerOptions()
                .position(destination)
                .title("Destination")
        )

        val currentLocation = LatLng(
            43.6756, -79.4106 // Temporarily hardcoded
        )

        mMap.addMarker(
            MarkerOptions()
                .position(currentLocation)
                .title("You")
        )

        mMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(currentLocation, 13f)
        )

            // ----------- Get current user's location (Uncomment in final implementation)
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
        //}
    }
}