package ca.gbc.comp3074.scavengerhuntgroup95project.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Store each tag name only once.
@Entity(
    tableName = "tags",
    indices = [Index(value = ["name"], unique = true)]
)
data class Tag(

    // Room creates a unique ID for each tag.
    @PrimaryKey(autoGenerate = true)
    val tagId: Long = 0,

    // Treat names like "Photo" and "photo" as the same tag.
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val name: String
)