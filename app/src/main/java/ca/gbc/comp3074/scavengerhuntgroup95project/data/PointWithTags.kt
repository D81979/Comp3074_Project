package ca.gbc.comp3074.scavengerhuntgroup95project.data

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

// Hold one location and its related tags.
data class PointWithTags(

    // Include all fields from the location.
    @Embedded
    val point: PointOfInterest,

    // Find its tags through the linking table.
    @Relation(
        parentColumn = "pointId",
        entityColumn = "tagId",
        associateBy = Junction(PointTagCrossRef::class)
    )
    val tags: List<Tag>
)