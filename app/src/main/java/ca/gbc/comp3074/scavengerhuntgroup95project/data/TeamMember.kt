package ca.gbc.comp3074.scavengerhuntgroup95project.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Store game participants separately from hunt locations.
@Entity(tableName = "team_members")
data class TeamMember(
    @PrimaryKey(autoGenerate = true)
    val memberId: Long = 0L,

    val name: String,

    // Contact details are optional.
    val email: String = "",
    val phone: String = ""
)