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

    // Add a location and return its ID.
    @Insert
    suspend fun insertPoint(point: PointOfInterest): Long

    // Update an existing location.
    @Update
    suspend fun updatePoint(point: PointOfInterest): Int

    // Delete a location and its tag links.
    @Delete
    suspend fun deletePoint(point: PointOfInterest): Int

    // Keep the main list updated.
    @Transaction
    @Query("SELECT * FROM points ORDER BY pointId DESC")
    fun observePointsWithTags(): Flow<List<PointWithTags>>

    // Load a location for editing.
    @Transaction
    @Query("SELECT * FROM points WHERE pointId = :pointId")
    suspend fun getPointWithTags(pointId: Long): PointWithTags?

    // Keep the Details screen updated.
    @Transaction
    @Query("SELECT * FROM points WHERE pointId = :pointId")
    fun observePointWithTags(pointId: Long): Flow<PointWithTags?>

    // Change only the rating.
    @Query(
        "UPDATE points SET rating = :rating " +
                "WHERE pointId = :pointId"
    )
    suspend fun updatePointRating(pointId: Long, rating: Int): Int

    // Change completion without overwriting other fields.
    @Query(
        """
        UPDATE points
        SET isCompleted = :completed,
            completedAt = CASE
                WHEN :completed = 0 THEN NULL
                WHEN isCompleted = 1 THEN COALESCE(completedAt, :timestamp)
                ELSE :timestamp
            END
        WHERE pointId = :pointId
        """
    )
    suspend fun updatePointCompletion(
        pointId: Long,
        completed: Boolean,
        timestamp: Long
    ): Int

    // Reuse tags that already exist.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: Tag): Long

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): Tag?

    // Show available tags alphabetically.
    @Query("SELECT * FROM tags ORDER BY name")
    fun observeTags(): Flow<List<Tag>>

    @Update
    suspend fun updateTag(tag: Tag): Int

    @Delete
    suspend fun deleteTag(tag: Tag): Int

    // Connect locations to their tags.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPointTagLinks(links: List<PointTagCrossRef>)

    // Clear old selections before saving new ones.
    @Query("DELETE FROM point_tag_cross_ref WHERE pointId = :pointId")
    suspend fun deletePointTagLinks(pointId: Long)

    // Add a member.
    @Insert
    suspend fun insertMember(member: TeamMember): Long

    // Update a member.
    @Update
    suspend fun updateMember(member: TeamMember): Int

    // Delete a member.
    @Delete
    suspend fun deleteMember(member: TeamMember): Int

    // Load a member for editing.
    @Query("SELECT * FROM team_members WHERE memberId = :memberId")
    suspend fun getMember(memberId: Long): TeamMember?

    // Keep the members list updated.
    @Query(
        "SELECT * FROM team_members " +
                "ORDER BY name COLLATE NOCASE, memberId"
    )
    fun observeMembers(): Flow<List<TeamMember>>
}