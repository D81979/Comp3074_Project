package ca.gbc.comp3074.scavengerhuntgroup95project.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

// Connect each location to its selected tags.
@Entity(
    tableName = "point_tag_cross_ref",
    primaryKeys = ["pointId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = PointOfInterest::class,
            parentColumns = ["pointId"],
            childColumns = ["pointId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["tagId"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["tagId"])]
)
data class PointTagCrossRef(
    val pointId: Long,
    val tagId: Long
)