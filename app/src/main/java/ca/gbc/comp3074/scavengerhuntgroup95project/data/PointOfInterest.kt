package ca.gbc.comp3074.scavengerhuntgroup95project.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Defines the table for hunt locations.
@Entity(tableName = "points")
data class PointOfInterest(

    // Room gives each new location a unique ID.
    @PrimaryKey(autoGenerate = true)
    val pointId: Long = 0,

    // Information entered in the Points Form.
    val name: String,
    val address: String,
    val task: String,

    // Coordinates can be added when the address is located.
    val latitude: Double? = null,
    val longitude: Double? = null,

    // No rating until the player chooses one.
    val rating: Int? = null,

    // New tasks start as incomplete.
    val isCompleted: Boolean = false,

    // Completion time stays empty until the task is done.
    val completedAt: Long? = null
)