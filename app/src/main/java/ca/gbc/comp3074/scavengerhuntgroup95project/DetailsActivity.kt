package ca.gbc.comp3074.scavengerhuntgroup95project

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class DetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        setupBackToolbar(R.string.title_details)

        findViewById<TextView>(R.id.txtName).text = intent.getStringExtra("name")
        findViewById<TextView>(R.id.txtAddress).text = intent.getStringExtra("address")
        findViewById<TextView>(R.id.txtTags).text = intent.getStringExtra("tags")

        // Map button opens the full-screen map (MapActivity)
        findViewById<MaterialButton>(R.id.btnMap).setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }
    }
}