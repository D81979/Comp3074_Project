package ca.gbc.comp3074.scavengerhuntgroup95project

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: PointAdapter
    private lateinit var txtEmpty: TextView

    private var currentQuery = ""
    private var selectedTag = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))

        txtEmpty = findViewById(R.id.txtEmpty)

        val recycler = findViewById<RecyclerView>(R.id.recyclerPoints)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = PointAdapter(SampleData.points) { point ->
            val intent = Intent(this, DetailsActivity::class.java)
            intent.putExtra("name", point.name)
            intent.putExtra("address", point.address)
            intent.putExtra("tags", point.tags.joinToString("  |  "))
            startActivity(intent)
        }
        recycler.adapter = adapter

        findViewById<EditText>(R.id.edtSearch).doOnTextChanged { text, _, _, _ ->
            currentQuery = text.toString()
            applyFilters()
        }

        findViewById<ChipGroup>(R.id.chipGroup).setOnCheckedStateChangeListener { group, checkedIds ->
            val chipId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            selectedTag = group.findViewById<Chip>(chipId).text.toString()
            applyFilters()
        }

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            startActivity(Intent(this, PointFormActivity::class.java))
        }
    }

    private fun applyFilters() {
        val result = SampleData.points.filter { point ->
            val matchesQuery = point.name.contains(currentQuery, ignoreCase = true) ||
                    point.tags.any { it.contains(currentQuery, ignoreCase = true) }
            val matchesTag = selectedTag == "All" || point.tags.contains(selectedTag)
            matchesQuery && matchesTag
        }
        adapter.updateList(result)
        txtEmpty.visibility = if (result.isEmpty()) View.VISIBLE else View.GONE
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