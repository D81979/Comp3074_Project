package ca.gbc.comp3074.scavengerhuntgroup95project

import android.app.Application
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import ca.gbc.comp3074.scavengerhuntgroup95project.data.HuntDatabase
import ca.gbc.comp3074.scavengerhuntgroup95project.data.HuntRepository
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class PointsFormActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var addressInput: EditText
    private lateinit var taskInput: EditText
    private lateinit var tagGroup: ChipGroup

    private val saveModel: PointsFormSaveViewModel by viewModels()
    private val customTags = arrayListOf<String>()

    // Zero means Add mode.
    private var editingPointId = 0L

    // Wait for saved values before allowing edits.
    private var editReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_points_form)

        editingPointId = intent.getLongExtra("point_id", 0L)

        if (editingPointId < 0L) {
            Toast.makeText(
                this,
                "Invalid location ID",
                Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }

        // Show the title for adding or editing.
        findViewById<TextView>(R.id.tvFormTitle).text =
            if (editingPointId == 0L) "Add Point" else "Edit Point"

        editReady = editingPointId == 0L ||
                savedInstanceState?.getBoolean(
                    "edit_ready",
                    false
                ) == true

        // Keep the form clear of bars and the keyboard.
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { view, insets ->
            val spacing = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                        WindowInsetsCompat.Type.ime()
            )

            view.setPadding(
                spacing.left,
                spacing.top,
                spacing.right,
                spacing.bottom
            )

            insets
        }

        // Connect the XML controls.
        nameInput = findViewById(R.id.etPointName)
        addressInput = findViewById(R.id.etAddress)
        taskInput = findViewById(R.id.etTask)
        tagGroup = findViewById(R.id.chipGroupTags)

        // Style the existing tags.
        allChips().forEach { styleTag(it) }

        // Restore custom tags after rotation.
        savedInstanceState
            ?.getStringArrayList("custom_tags")
            ?.forEach { addCustomTag(it) }

        val selectedTags = savedInstanceState
            ?.getStringArrayList("selected_tags")

        if (selectedTags != null) {
            allChips().forEach { chip ->
                chip.isChecked =
                    chip.text.toString() in selectedTags
            }
        }

        findViewById<View>(R.id.btnBack)
            .setOnClickListener {
                closeForm()
            }

        findViewById<View>(R.id.btnCancel)
            .setOnClickListener {
                closeForm()
            }

        findViewById<View>(R.id.btnAddTag)
            .setOnClickListener {
                showAddTagDialog()
            }

        findViewById<View>(R.id.btnSavePoint)
            .setOnClickListener {
                validateForm()
            }

        // Handle the phone's Back button.
        onBackPressedDispatcher.addCallback(this) {
            closeForm()
        }

        // Set the initial button state.
        updateFormControls()

        // Watch the save operation.
        saveModel.state.observe(this) { state ->
            updateFormControls()

            if (state.savedPointId != null) {
                setResult(
                    RESULT_OK,
                    Intent().putExtra(
                        "point_id",
                        state.savedPointId
                    )
                )

                val message = if (editingPointId == 0L) {
                    "Point saved successfully"
                } else {
                    "Point updated successfully"
                }

                Toast.makeText(
                    this,
                    message,
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            } else if (state.error != null) {
                Toast.makeText(
                    this,
                    state.error,
                    Toast.LENGTH_LONG
                ).show()

                saveModel.clearError()
            }
        }

        // Load existing values in Edit mode.
        if (!editReady) {
            loadPointForEditing()
        }
    }

    private fun loadPointForEditing() {
        updateFormControls()

        lifecycleScope.launch {
            try {
                val repository = HuntRepository(
                    HuntDatabase.getDatabase(applicationContext)
                )

                val saved = repository.getPoint(editingPointId)
                    ?: error("This location no longer exists")

                // Fill the form with saved details.
                nameInput.setText(saved.point.name)
                addressInput.setText(saved.point.address)
                taskInput.setText(saved.point.task)

                allChips().forEach {
                    it.isChecked = false
                }

                // Restore the location's selected tags.
                for (tag in saved.tags) {
                    val existingChip = allChips().firstOrNull {
                        it.text.toString().equals(
                            tag.name,
                            ignoreCase = true
                        )
                    }

                    if (existingChip != null) {
                        existingChip.isChecked = true
                    } else {
                        addCustomTag(
                            tagName = tag.name,
                            selected = true
                        )
                    }
                }

                editReady = true
                updateFormControls()

            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Log.e(
                    "PointsForm",
                    "Unable to load location",
                    error
                )

                Toast.makeText(
                    this@PointsFormActivity,
                    "Could not load this location. Please reopen it.",
                    Toast.LENGTH_LONG
                ).show()

                finish()
            }
        }
    }

    private fun allChips(): List<Chip> {
        // Get all tag chips.
        return (0 until tagGroup.childCount).mapNotNull {
            tagGroup.getChildAt(it) as? Chip
        }
    }

    private fun styleTag(chip: Chip) {
        chip.isCheckable = true
        chip.isCheckedIconVisible = false

        // Restore selections by tag name.
        chip.isSaveEnabled = false

        chip.shapeAppearanceModel = chip.shapeAppearanceModel
            .toBuilder()
            .setAllCornerSizes(0f)
            .build()

        chip.chipStrokeWidth = 0f
        chip.chipMinHeight =
            28f * resources.displayMetrics.density
        chip.textSize = 12f

        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf()
        )

        // Dark when selected; light grey otherwise.
        chip.chipBackgroundColor = ColorStateList(
            states,
            intArrayOf(
                Color.parseColor("#444444"),
                Color.parseColor("#DDDDDD")
            )
        )

        chip.setTextColor(
            ColorStateList(
                states,
                intArrayOf(
                    Color.WHITE,
                    Color.parseColor("#444444")
                )
            )
        )
    }

    private fun showAddTagDialog() {
        val input = EditText(this).apply {
            hint = "Enter a tag"
            inputType = InputType.TYPE_CLASS_TEXT
            setSingleLine(true)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add new tag")
            .setView(input)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()

        // Keep the dialog open for invalid input.
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    val tagName = input.text.toString().trim()

                    val alreadyExists = allChips().any {
                        it.text.toString().equals(
                            tagName,
                            ignoreCase = true
                        )
                    }

                    when {
                        tagName.isBlank() -> {
                            input.error = "Enter a tag name"
                        }

                        alreadyExists -> {
                            input.error = "This tag already exists"
                        }

                        else -> {
                            addCustomTag(
                                tagName,
                                selected = true
                            )
                            dialog.dismiss()
                        }
                    }
                }
        }

        dialog.show()
    }

    private fun addCustomTag(
        tagName: String,
        selected: Boolean = false
    ) {
        // Avoid adding the same chip twice.
        val existingChip = allChips().firstOrNull {
            it.text.toString().equals(
                tagName,
                ignoreCase = true
            )
        }

        if (existingChip != null) {
            if (selected) {
                existingChip.isChecked = true
            }
            return
        }

        val chip = Chip(this).apply {
            id = View.generateViewId()
            text = tagName
        }

        styleTag(chip)
        tagGroup.addView(chip)
        chip.isChecked = selected
        customTags.add(tagName)
    }

    private fun validateForm() {
        if (!editReady) return

        // Ignore repeated Save taps.
        if (saveModel.state.value?.saving == true ||
            saveModel.state.value?.savedPointId != null
        ) return

        nameInput.error = null
        addressInput.error = null
        taskInput.error = null

        var firstInvalidField: EditText? = null

        val requiredFields = listOf(
            nameInput to "Enter a location name",
            addressInput to "Enter an address",
            taskInput to "Enter task instructions"
        )

        for ((field, message) in requiredFields) {
            if (field.text.toString().isBlank()) {
                field.error = message

                if (firstInvalidField == null) {
                    firstInvalidField = field
                }
            }
        }

        if (firstInvalidField != null) {
            firstInvalidField.requestFocus()
            return
        }

        val selectedTags = allChips()
            .filter { it.isChecked }
            .map { it.text.toString() }

        // Pass the ID to update an existing location.
        saveModel.save(
            name = nameInput.text.toString(),
            address = addressInput.text.toString(),
            task = taskInput.text.toString(),
            tags = selectedTags,
            pointId = editingPointId
        )
    }

    private fun closeForm() {
        if (saveModel.state.value?.saving == true) {
            Toast.makeText(
                this,
                "Please wait while saving",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            finish()
        }
    }

    private fun updateFormControls() {
        val state = saveModel.state.value
        val saving = state?.saving == true
        val saved = state?.savedPointId != null
        val enabled = editReady && !saving && !saved

        // Lock input while loading or saving.
        nameInput.isEnabled = enabled
        addressInput.isEnabled = enabled
        taskInput.isEnabled = enabled

        allChips().forEach {
            it.isEnabled = enabled
        }

        findViewById<View>(R.id.btnAddTag).isEnabled = enabled

        // Allow cancellation while loading.
        findViewById<View>(R.id.btnBack).isEnabled =
            !saving && !saved
        findViewById<View>(R.id.btnCancel).isEnabled =
            !saving && !saved

        findViewById<Button>(R.id.btnSavePoint).apply {
            isEnabled = enabled

            text = when {
                !editReady -> "Loading..."
                saving -> "Saving..."
                editingPointId != 0L -> "Save Changes"
                else -> "Save Point"
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("edit_ready", editReady)

        // Keep custom tags during rotation.
        outState.putStringArrayList(
            "custom_tags",
            ArrayList(customTags)
        )

        val selectedTags = allChips()
            .filter { it.isChecked }
            .map { it.text.toString() }

        outState.putStringArrayList(
            "selected_tags",
            ArrayList(selectedTags)
        )

        super.onSaveInstanceState(outState)
    }
}

// Hold the save progress and result.
data class PointsFormSaveState(
    val saving: Boolean = false,
    val savedPointId: Long? = null,
    val error: String? = null
)

// Keep the save running during screen rotation.
class PointsFormSaveViewModel(application: Application) :
    AndroidViewModel(application) {

    private val repository by lazy {
        HuntRepository(
            HuntDatabase.getDatabase(
                getApplication<Application>()
            )
        )
    }

    private val mutableState = MutableLiveData(
        PointsFormSaveState()
    )

    val state: LiveData<PointsFormSaveState> = mutableState

    fun save(
        name: String,
        address: String,
        task: String,
        tags: List<String>,
        pointId: Long = 0L
    ) {
        if (mutableState.value?.saving == true ||
            mutableState.value?.savedPointId != null
        ) return

        mutableState.value = PointsFormSaveState(
            saving = true
        )

        viewModelScope.launch {
            try {
                // Zero creates; an existing ID updates.
                val id = repository.savePoint(
                    name = name,
                    address = address,
                    task = task,
                    tagNames = tags,
                    pointId = pointId
                )

                mutableState.value = PointsFormSaveState(
                    savedPointId = id
                )

            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Log.e(
                    "PointsForm",
                    "Unable to save location",
                    error
                )

                mutableState.value = PointsFormSaveState(
                    error = "Could not save the point. Your entries are still here. Please try again."
                )
            }
        }
    }

    fun clearError() {
        mutableState.value = mutableState.value?.copy(
            error = null
        )
    }
}