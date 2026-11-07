package ca.gbc.comp3074.scavengerhuntgroup95project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import ca.gbc.comp3074.scavengerhuntgroup95project.data.HuntDatabase
import ca.gbc.comp3074.scavengerhuntgroup95project.data.HuntRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val repository by lazy {
        HuntRepository(HuntDatabase.getDatabase(applicationContext))
    }

    private var openingPicker = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Keep content away from system bars.
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

        val addButton = findViewById<Button>(
            R.id.btnOpenPointsForm
        )

        // Tap to add a new location.
        addButton.setOnClickListener {
            startActivity(
                Intent(this, PointsFormActivity::class.java)
            )
        }

        // Hold to choose a location for editing.
        addButton.setOnLongClickListener {
            showEditPicker()
            true
        }
    }

    private fun showEditPicker() {
        if (openingPicker) return
        openingPicker = true

        lifecycleScope.launch {
            try {
                val locations = repository.points.first()

                if (locations.isEmpty()) {
                    Toast.makeText(
                        this@MainActivity,
                        "Add a location first",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                val labels = locations.map {
                    "${it.point.name} — ${it.point.address}"
                }.toTypedArray()

                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Choose a location to edit")
                    .setItems(labels) { _, position ->
                        val pointId = locations[position].point.pointId

                        val intent = Intent(
                            this@MainActivity,
                            PointsFormActivity::class.java
                        ).apply {
                            putExtra("point_id", pointId)
                        }

                        startActivity(intent)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()

            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Could not load locations. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                openingPicker = false
            }
        }
    }
}