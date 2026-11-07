package ca.gbc.comp3074.scavengerhuntgroup95project.data

import android.util.Patterns
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

class HuntRepository(private val database: HuntDatabase) {

    private val dao = database.huntDao()

    val points: Flow<List<PointWithTags>> =
        dao.observePointsWithTags()

    val tags: Flow<List<Tag>> =
        dao.observeTags()

    val members: Flow<List<TeamMember>> =
        dao.observeMembers()

    suspend fun getPoint(pointId: Long): PointWithTags? {
        return dao.getPointWithTags(pointId)
    }

    // Watch one point for changes.
    fun observePoint(pointId: Long): Flow<PointWithTags?> {
        require(pointId > 0L) {
            "Invalid location ID"
        }

        return dao.observePointWithTags(pointId)
    }

    // Save a rating from 0 to 5.
    suspend fun updatePointRating(pointId: Long, rating: Int) {
        require(pointId > 0L) {
            "Invalid location ID"
        }

        require(rating in 0..5) {
            "Rating must be between 0 and 5"
        }

        check(dao.updatePointRating(pointId, rating) == 1) {
            "This location no longer exists"
        }
    }

    // Save completion and its timestamp.
    suspend fun updatePointCompletion(
        pointId: Long,
        completed: Boolean
    ) {
        require(pointId > 0L) {
            "Invalid location ID"
        }

        val updatedRows = dao.updatePointCompletion(
            pointId = pointId,
            completed = completed,
            timestamp = System.currentTimeMillis()
        )

        check(updatedRows == 1) {
            "This location no longer exists"
        }
    }

    suspend fun savePoint(
        name: String,
        address: String,
        task: String,
        tagNames: List<String>,
        pointId: Long = 0L
    ): Long {
        val cleanName = name.trim()
        val cleanAddress = address.trim()
        val cleanTask = task.trim()

        require(cleanName.isNotEmpty()) {
            "Enter a location name"
        }

        require(cleanAddress.isNotEmpty()) {
            "Enter an address"
        }

        require(cleanTask.isNotEmpty()) {
            "Enter task instructions"
        }

        require(pointId >= 0L) {
            "Invalid location ID"
        }

        val cleanTags = tagNames
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()

        return database.withTransaction {
            val savedPointId: Long

            if (pointId == 0L) {
                val newPoint = PointOfInterest(
                    name = cleanName,
                    address = cleanAddress,
                    task = cleanTask
                )

                savedPointId = dao.insertPoint(newPoint)
            } else {
                val existing = dao.getPointWithTags(pointId)?.point
                    ?: error("This location no longer exists")

                val addressChanged = existing.address != cleanAddress

                // Keep rating and completion when editing.
                val updatedPoint = existing.copy(
                    name = cleanName,
                    address = cleanAddress,
                    task = cleanTask,
                    latitude = if (addressChanged) null else existing.latitude,
                    longitude = if (addressChanged) null else existing.longitude
                )

                check(dao.updatePoint(updatedPoint) == 1) {
                    "The location could not be updated"
                }

                savedPointId = pointId
            }

            // Replace this point's tag links.
            dao.deletePointTagLinks(savedPointId)

            val links = mutableListOf<PointTagCrossRef>()

            for (tagName in cleanTags) {
                val insertedId = dao.insertTag(
                    Tag(name = tagName)
                )

                val tagId = if (insertedId != -1L) {
                    insertedId
                } else {
                    dao.getTagByName(tagName)?.tagId
                        ?: error("The tag could not be found")
                }

                links.add(
                    PointTagCrossRef(
                        pointId = savedPointId,
                        tagId = tagId
                    )
                )
            }

            dao.insertPointTagLinks(links.distinct())

            savedPointId
        }
    }

    suspend fun deletePoint(pointId: Long): Boolean {
        return database.withTransaction {
            val existing = dao.getPointWithTags(pointId)

            if (existing == null) {
                false
            } else {
                dao.deletePoint(existing.point) == 1
            }
        }
    }

    suspend fun getMember(memberId: Long): TeamMember? {
        require(memberId > 0L) {
            "Invalid member ID"
        }

        return dao.getMember(memberId)
    }

    suspend fun saveMember(
        name: String,
        email: String = "",
        phone: String = "",
        memberId: Long = 0L
    ): Long {
        val cleanName = name.trim()
        val cleanEmail = email.trim()
        val cleanPhone = phone.trim()

        require(cleanName.isNotEmpty()) {
            "Enter a member name"
        }

        require(memberId >= 0L) {
            "Invalid member ID"
        }

        require(
            cleanEmail.isEmpty() ||
                    Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()
        ) {
            "Enter a valid email address"
        }

        return database.withTransaction {
            if (memberId == 0L) {
                dao.insertMember(
                    TeamMember(
                        name = cleanName,
                        email = cleanEmail,
                        phone = cleanPhone
                    )
                )
            } else {
                val existing = dao.getMember(memberId)
                    ?: error("This member no longer exists")

                val updatedMember = existing.copy(
                    name = cleanName,
                    email = cleanEmail,
                    phone = cleanPhone
                )

                check(dao.updateMember(updatedMember) == 1) {
                    "The member could not be updated"
                }

                memberId
            }
        }
    }

    suspend fun deleteMember(memberId: Long): Boolean {
        require(memberId > 0L) {
            "Invalid member ID"
        }

        return database.withTransaction {
            val existing = dao.getMember(memberId)

            if (existing == null) {
                false
            } else {
                dao.deleteMember(existing) == 1
            }
        }
    }
}