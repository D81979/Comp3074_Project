package ca.gbc.comp3074.scavengerhuntgroup95project

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.gbc.comp3074.scavengerhuntgroup95project.data.HuntDatabase
import ca.gbc.comp3074.scavengerhuntgroup95project.data.HuntRepository
import ca.gbc.comp3074.scavengerhuntgroup95project.data.PointWithTags
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val repository by lazy {
        HuntRepository(
            HuntDatabase.getDatabase(applicationContext)
        )
    }

    private lateinit var adapter: PointAdapter
    private lateinit var txtEmpty: TextView
    private lateinit var edtSearch: EditText
    private lateinit var chipGroup: ChipGroup

    private var savedPoints: List<PointWithTags> = emptyList()
    private var loadFailed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Keep content clear of system bars.
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                bars.left,
                bars.top,
                bars.right,
                bars.bottom
            )

            insets
        }

        ViewCompat.requestApplyInsets(findViewById(R.id.main))

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        txtEmpty = findViewById(R.id.txtEmpty)
        edtSearch = findViewById(R.id.edtSearch)
        chipGroup = findViewById(R.id.chipGroup)

        val recycler = findViewById<RecyclerView>(
            R.id.recyclerPoints
        )

        recycler.layoutManager = LinearLayoutManager(this)

        // Use the database ID for each action.
        adapter = PointAdapter(
            items = emptyList(),
            onClick = { item ->
                val intent = Intent(
                    this,
                    DetailsActivity::class.java
                )

                intent.putExtra(
                    "point_id",
                    item.point.pointId
                )

                startActivity(intent)
            },
            onEdit = { item ->
                openPointForm(item.point.pointId)
            },
            onDelete = { item ->
                confirmDelete(item)
            }
        )

        recycler.adapter = adapter

        edtSearch.doOnTextChanged { _, _, _, _ ->
            applyFilters()
        }

        chipGroup.setOnCheckedStateChangeListener { _, _ ->
            applyFilters()
        }

        findViewById<FloatingActionButton>(
            R.id.fabAdd
        ).setOnClickListener {
            openPointForm()
        }

        observePoints()
    }

    private fun openPointForm(pointId: Long = 0L) {
        val intent = Intent(
            this,
            PointsFormActivity::class.java
        )

        intent.putExtra("point_id", pointId)
        startActivity(intent)
    }

    // Refresh the list when database data changes.
    private fun observePoints() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    repository.points.collect { points ->
                        savedPoints = points
                        loadFailed = false
                        applyFilters()
                    }
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    loadFailed = true
                    applyFilters()

                    Toast.makeText(
                        this@MainActivity,
                        "Could not load locations",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun applyFilters() {
        val query = edtSearch.text.toString().trim()

        val checkedId = chipGroup.checkedChipId

        val selectedTag = if (checkedId == View.NO_ID) {
            "All"
        } else {
            findViewById<Chip>(checkedId)
                ?.text
                ?.toString()
                ?: "All"
        }

        val filteredPoints = savedPoints.filter { item ->
            val point = item.point

            val matchesSearch =
                query.isEmpty() ||
                        point.name.contains(
                            query,
                            ignoreCase = true
                        ) ||
                        point.address.contains(
                            query,
                            ignoreCase = true
                        ) ||
                        item.tags.any { tag ->
                            tag.name.contains(
                                query,
                                ignoreCase = true
                            )
                        }

            val matchesTag =
                selectedTag == "All" ||
                        item.tags.any { tag ->
                            tag.name.equals(
                                selectedTag,
                                ignoreCase = true
                            )
                        }

            matchesSearch && matchesTag
        }

        adapter.updateList(filteredPoints)

        txtEmpty.visibility = if (filteredPoints.isEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }

        txtEmpty.text = when {
            loadFailed ->
                "Could not load locations. Reopen this screen to retry."

            savedPoints.isEmpty() ->
                "No locations yet. Tap + to add your first point."

            else ->
                getString(R.string.no_results)
        }
    }

    private fun confirmDelete(item: PointWithTags) {
        AlertDialog.Builder(this)
            .setTitle("Delete location?")
            .setMessage(
                "Delete \"${item.point.name}\"?"
            )
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                deletePoint(item.point.pointId)
            }
            .show()
    }

    private fun deletePoint(pointId: Long) {
        lifecycleScope.launch {
            try {
                val deleted = repository.deletePoint(pointId)

                val message = if (deleted) {
                    "Location deleted"
                } else {
                    "This location no longer exists"
                }

                Toast.makeText(
                    this@MainActivity,
                    message,
                    Toast.LENGTH_SHORT
                ).show()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Could not delete the location",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(
        item: MenuItem
    ): Boolean {
        return when (item.itemId) {
            R.id.menu_team -> {
                startActivity(
                    Intent(this, TeamActivity::class.java)
                )
                true
            }

            R.id.menu_about -> {
                startActivity(
                    Intent(this, AboutActivity::class.java)
                )
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}