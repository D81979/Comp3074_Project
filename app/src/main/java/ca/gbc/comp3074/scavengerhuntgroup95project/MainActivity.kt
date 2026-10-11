package ca.gbc.comp3074.scavengerhuntgroup95project

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))

        val recycler = findViewById<RecyclerView>(R.id.recyclerPoints)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = PointAdapter(SampleData.points) { point ->
            val intent = Intent(this, DetailsActivity::class.java)
            intent.putExtra("name", point.name)
            intent.putExtra("address", point.address)
            intent.putExtra("tags", point.tags.joinToString("  |  "))
            startActivity(intent)
        }

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            startActivity(Intent(this, PointFormActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_team -> startActivity(Intent(this, TeamActivity::class.java))
            R.id.menu_about -> startActivity(Intent(this, AboutActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }
}