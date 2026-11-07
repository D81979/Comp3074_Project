package ca.gbc.comp3074.scavengerhuntgroup95project

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.MaterialToolbar

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = "Map"
            setDisplayHomeAsUpEnabled(true)
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val toronto = LatLng(43.6532, -79.3832)
        mMap.addMarker(
            MarkerOptions()
                .position(toronto)
                .title("Toronto")
        )

        mMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(toronto, 12f)
        )
    }
}