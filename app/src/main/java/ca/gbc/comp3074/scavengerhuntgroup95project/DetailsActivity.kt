package ca.gbc.comp3074.scavengerhuntgroup95project

import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class DetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        setupBackToolbar(R.string.title_details)

        val index = intent.getIntExtra("index", -1)
        if (index !in SampleData.points.indices) {
            finish()
            return
        }
        val point = SampleData.points[index]

        findViewById<TextView>(R.id.txtName).text = point.name
        findViewById<TextView>(R.id.txtAddress).text = point.address
        findViewById<TextView>(R.id.txtTags).text = point.tags.joinToString("  |  ")

        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        ratingBar.rating = point.rating.toFloat()
        ratingBar.setOnRatingBarChangeListener { _, rating, fromUser ->
            if (fromUser) {
                SampleData.points[index] = SampleData.points[index].copy(rating = rating.toInt())
            }
        }

        val chkDone = findViewById<CheckBox>(R.id.chkDone)
        chkDone.isChecked = point.done
        chkDone.setOnCheckedChangeListener { _, isChecked ->
            SampleData.points[index] = SampleData.points[index].copy(done = isChecked)
        }

        // Map button opens the full-screen map (MapActivity)
        findViewById<MaterialButton>(R.id.btnMap).setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }
    }
}