package ca.gbc.comp3074.scavengerhuntgroup95project

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import ca.gbc.comp3074.scavengerhuntgroup95project.data.HuntDatabase
import ca.gbc.comp3074.scavengerhuntgroup95project.data.HuntRepository
import ca.gbc.comp3074.scavengerhuntgroup95project.data.PointWithTags
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailsActivity : AppCompatActivity() {

    private lateinit var viewModel: PointDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        setupBackToolbar(R.string.title_details)

        // MainActivity sends the database ID.
        val pointId = intent.getLongExtra("point_id", 0L)

        if (pointId <= 0L) {
            Toast.makeText(
                this,
                "Invalid location",
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        viewModel = ViewModelProvider(this)[
            PointDetailsViewModel::class.java
        ]

        val txtName = findViewById<TextView>(R.id.txtName)
        val txtAddress = findViewById<TextView>(R.id.txtAddress)
        val txtTags = findViewById<TextView>(R.id.txtTags)
        val txtTask = findViewById<TextView>(R.id.txtTask)
        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val chkDone = findViewById<CheckBox>(R.id.chkDone)
        val btnMap = findViewById<MaterialButton>(R.id.btnMap)

        // Wait until the saved point is loaded.
        txtTask.text = ""
        ratingBar.isEnabled = false
        chkDone.isEnabled = false
        btnMap.isEnabled = false

        ratingBar.stepSize = 1f
        ratingBar.numStars = 5

        ratingBar.setOnRatingBarChangeListener { _, rating, fromUser ->
            if (fromUser) {
                viewModel.saveRating(rating.toInt())
            }
        }

        btnMap.setOnClickListener {
            val mapIntent = Intent(
                this,
                MapActivity::class.java
            )

            mapIntent.putExtra("point_id", pointId)
            startActivity(mapIntent)
        }

        viewModel.loadPoint(pointId)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->

                    if (state.missing) {
                        Toast.makeText(
                            this@DetailsActivity,
                            "This location no longer exists",
                            Toast.LENGTH_SHORT
                        ).show()

                        finish()
                        return@collect
                    }

                    val item = state.item
                    val canEdit =
                        item != null && !state.loading && !state.saving

                    ratingBar.isEnabled = canEdit
                    chkDone.isEnabled = canEdit
                    btnMap.isEnabled = item != null

                    if (item != null) {
                        val point = item.point

                        txtName.text = point.name
                        txtAddress.text = point.address
                        txtTask.text = point.task

                        txtTags.text = item.tags.joinToString(
                            separator = "  |  "
                        ) { tag ->
                            tag.name
                        }

                        ratingBar.rating =
                            (point.rating ?: 0).toFloat()

                        // Updating the screen should not trigger a save.
                        chkDone.setOnCheckedChangeListener(null)
                        chkDone.isChecked = point.isCompleted

                        chkDone.setOnCheckedChangeListener { _, checked ->
                            viewModel.saveCompletion(checked)
                        }
                    }

                    state.error?.let { message ->
                        Toast.makeText(
                            this@DetailsActivity,
                            message,
                            Toast.LENGTH_LONG
                        ).show()

                        viewModel.clearError()
                    }
                }
            }
        }
    }
}

// Values shown by the Details screen.
data class PointDetailsState(
    val item: PointWithTags? = null,
    val loading: Boolean = true,
    val saving: Boolean = false,
    val missing: Boolean = false,
    val error: String? = null
)

// Keeps database work alive during rotation.
class PointDetailsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = HuntRepository(
        HuntDatabase.getDatabase(application)
    )

    private val mutableState = MutableStateFlow(
        PointDetailsState()
    )

    val state = mutableState.asStateFlow()

    private var currentPointId = 0L

    fun loadPoint(pointId: Long) {
        // Do not start another observer after rotation.
        if (currentPointId == pointId) {
            return
        }

        currentPointId = pointId

        viewModelScope.launch {
            try {
                repository.observePoint(pointId).collect { item ->
                    mutableState.update {
                        it.copy(
                            item = item,
                            loading = false,
                            missing = item == null
                        )
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                mutableState.update {
                    it.copy(
                        loading = false,
                        error = "Could not load this location"
                    )
                }
            }
        }
    }

    fun saveRating(rating: Int) {
        saveChange {
            repository.updatePointRating(
                pointId = currentPointId,
                rating = rating
            )
        }
    }

    fun saveCompletion(completed: Boolean) {
        saveChange {
            repository.updatePointCompletion(
                pointId = currentPointId,
                completed = completed
            )
        }
    }

    private fun saveChange(action: suspend () -> Unit) {
        val current = mutableState.value

        if (
            current.item == null ||
            current.loading ||
            current.saving ||
            current.missing
        ) {
            return
        }

        mutableState.update {
            it.copy(saving = true, error = null)
        }

        viewModelScope.launch {
            try {
                action()

                // Read back the saved values.
                val savedItem = repository.getPoint(currentPointId)

                mutableState.update {
                    it.copy(
                        item = savedItem,
                        missing = savedItem == null
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                mutableState.update {
                    it.copy(
                        error = "Could not save the change. Please try again."
                    )
                }
            } finally {
                mutableState.update {
                    it.copy(saving = false)
                }
            }
        }
    }

    fun clearError() {
        mutableState.update {
            it.copy(error = null)
        }
    }
}