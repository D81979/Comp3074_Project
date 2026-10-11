package ca.gbc.comp3074.scavengerhuntgroup95project

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class PointsFormActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var addressInput: EditText
    private lateinit var taskInput: EditText
    private lateinit var tagGroup: ChipGroup

    private val customTags = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_points_form)

        // Keep the form clear of system bars and the keyboard.
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

        // Connect Kotlin to the XML controls.
        nameInput = findViewById(R.id.etPointName)
        addressInput = findViewById(R.id.etAddress)
        taskInput = findViewById(R.id.etTask)
        tagGroup = findViewById(R.id.chipGroupTags)

        // Style the four existing tags.
        for (chip in allChips()) {
            styleTag(chip)
        }

        // Restore custom tags after a screen rotation.
        savedInstanceState?.getStringArrayList("custom_tags")
            ?.forEach { addCustomTag(it) }

        val selectedTags =
            savedInstanceState?.getStringArrayList("selected_tags")

        if (selectedTags != null) {
            for (chip in allChips()) {
                chip.isChecked = chip.text.toString() in selectedTags
            }
        }

        // Leave the form without saving.
        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btnCancel).setOnClickListener {
            finish()
        }

        // Open the custom-tag dialog.
        findViewById<View>(R.id.btnAddTag).setOnClickListener {
            showAddTagDialog()
        }

        // Check the required fields.
        findViewById<View>(R.id.btnSavePoint).setOnClickListener {
            validateForm()
        }
    }

    private fun allChips(): List<Chip> {
        // Get every tag inside the group.
        return (0 until tagGroup.childCount).mapNotNull {
            tagGroup.getChildAt(it) as? Chip
        }
    }

    private fun styleTag(chip: Chip) {
        chip.isCheckable = true
        chip.isCheckedIconVisible = false

        // Tag selections are restored by their names.
        chip.isSaveEnabled = false

        chip.shapeAppearanceModel = chip.shapeAppearanceModel
            .toBuilder()
            .setAllCornerSizes(0f)
            .build()

        chip.chipStrokeWidth = 0f
        chip.chipMinHeight = 28f * resources.displayMetrics.density
        chip.textSize = 12f

        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf()
        )

        // Selected = dark; unselected = light grey.
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

        // Keep the dialog open if the tag is invalid.
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
                            addCustomTag(tagName, selected = true)
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
        // Clear old error messages.
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

        // Temporary feedback until we connect Room.
        Toast.makeText(
            this,
            "Form is valid. Database saving is not connected yet.",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Keep custom tags and selections during rotation.
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