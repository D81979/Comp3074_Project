package ca.gbc.comp3074.scavengerhuntgroup95project.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
@Dao
interface HuntDao {

    // Save a new location and return its generated ID.
    @Insert
    suspend fun insertPoint(point: PointOfInterest): Long

    // Update an existing location using its ID.
    @Update
    suspend fun updatePoint(point: PointOfInterest): Int

    // Delete a location; its tag links are removed automatically.
    @Delete
    suspend fun deletePoint(point: PointOfInterest): Int

    // Observe all locations together with their tags.
    @Transaction
    @Query("SELECT * FROM points ORDER BY pointId DESC")
    fun observePointsWithTags(): Flow<List<PointWithTags>>

    // Load one location and its tags for editing.
    @Transaction
    @Query("SELECT * FROM points WHERE pointId = :pointId")
    suspend fun getPointWithTags(pointId: Long): PointWithTags?

    // Add a tag; return -1 if its name already exists.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: Tag): Long

    // Find an existing tag by name.
    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): Tag?

    // Observe the available tags in alphabetical order.
    @Query("SELECT * FROM tags ORDER BY name")
    fun observeTags(): Flow<List<Tag>>

    // Rename an existing tag.
    @Update
    suspend fun updateTag(tag: Tag): Int

    // Delete a tag and automatically remove its location links.
    @Delete
    suspend fun deleteTag(tag: Tag): Int

    // Connect a location to its selected tags.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPointTagLinks(links: List<PointTagCrossRef>)

    // Clear old selections before assigning updated tags.
    @Query("DELETE FROM point_tag_cross_ref WHERE pointId = :pointId")
    suspend fun deletePointTagLinks(pointId: Long)

    // Add a new member.
    @Insert
    suspend fun insertMember(member: TeamMember): Long

    // Update an existing member.
    @Update
    suspend fun updateMember(member: TeamMember): Int

    // Remove a member.
    @Delete
    suspend fun deleteMember(member: TeamMember): Int

    // Load one member for editing.
    @Query("SELECT * FROM team_members WHERE memberId = :memberId")
    suspend fun getMember(memberId: Long): TeamMember?

    // Keep the members list updated.
    @Query(
        "SELECT * FROM team_members " +
                "ORDER BY name COLLATE NOCASE, memberId"
    )
    fun observeMembers(): Flow<List<TeamMember>>
}