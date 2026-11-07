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

        // Show a message after saving or deleting a member.
        supportFragmentManager.setFragmentResultListener(
            MemberFormDialogFragment.RESULT_KEY,
            this
        ) { _, result ->
            val deleted = result.getBoolean("deleted")

            Toast.makeText(
                this,
                if (deleted) "Member deleted" else "Member saved",
                Toast.LENGTH_SHORT
            ).show()
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

        // Hold to open our temporary test menu.
        addButton.setOnLongClickListener {
            showTestMenu()
            true
        }
    }

    private fun showTestMenu() {
        if (openingPicker) return

        val options = arrayOf(
            "Edit location",
            "Add member",
            "Edit member"
        )

        AlertDialog.Builder(this)
            .setTitle("Test forms")
            .setItems(options) { _, position ->
                when (position) {
                    0 -> showEditPicker()
                    1 -> openMemberForm()
                    2 -> showMemberPicker()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openMemberForm(memberId: Long = 0L) {
        val manager = supportFragmentManager

        // Avoid opening the same dialog twice.
        if (
            manager.isStateSaved ||
            manager.findFragmentByTag(
                MemberFormDialogFragment.TAG
            ) != null
        ) {
            return
        }

        MemberFormDialogFragment.newInstance(memberId)
            .showNow(manager, MemberFormDialogFragment.TAG)
    }

    private fun showEditPicker() {
        if (openingPicker) return
        openingPicker = true

        lifecycleScope.launch {
            try {
                val locations = repository.points.first()

                if (isFinishing || isDestroyed) return@launch

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

    private fun showMemberPicker() {
        if (openingPicker) return
        openingPicker = true

        lifecycleScope.launch {
            try {
                // Read the latest saved members.
                val members = repository.members.first()

                if (isFinishing || isDestroyed) return@launch

                if (members.isEmpty()) {
                    Toast.makeText(
                        this@MainActivity,
                        "Add a member first",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                val labels = members.map {
                    "${it.name} — ID ${it.memberId}"
                }.toTypedArray()

                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Choose a member to edit")
                    .setItems(labels) { _, position ->
                        openMemberForm(members[position].memberId)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()

            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Could not load members. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                openingPicker = false
            }
        }
    }
}