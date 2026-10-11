package ca.gbc.comp3074.scavengerhuntgroup95project

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        setupBackToolbar(R.string.title_details)

        findViewById<TextView>(R.id.txtName).text = intent.getStringExtra("name")
        findViewById<TextView>(R.id.txtAddress).text = intent.getStringExtra("address")
        findViewById<TextView>(R.id.txtTags).text = intent.getStringExtra("tags")
    }
}