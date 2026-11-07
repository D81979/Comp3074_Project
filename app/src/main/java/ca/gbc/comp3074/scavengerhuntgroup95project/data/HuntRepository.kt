package ca.gbc.comp3074.scavengerhuntgroup95project.data

import android.util.Patterns
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

class HuntRepository(private val database: HuntDatabase) {

    private val dao = database.huntDao()

    // Observe saved locations and their tags.
    val points: Flow<List<PointWithTags>> =
        dao.observePointsWithTags()

    // Observe all available tags.
    val tags: Flow<List<Tag>> = dao.observeTags()

    // Observe members so the list can refresh automatically.
    val members: Flow<List<TeamMember>> = dao.observeMembers()

    // Load a location when opening the Edit form.
    suspend fun getPoint(pointId: Long): PointWithTags? {
        return dao.getPointWithTags(pointId)
    }

    // Use pointId = 0 for a new location.
    suspend fun savePoint(
        name: String,
        address: String,
        task: String,
        tagNames: List<String>,
        pointId: Long = 0
    ): Long {

        // Remove spaces around the entered text.
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
        require(pointId >= 0) {
            "Invalid location ID"
        }

        // Ignore blank tags and repeated identical names.
        val cleanTags = tagNames
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()

        // Save everything together, or undo it if something fails.
        return database.withTransaction {

            val savedPointId: Long

            if (pointId == 0L) {

                // Create a new location.
                val newPoint = PointOfInterest(
                    name = cleanName,
                    address = cleanAddress,
                    task = cleanTask
                )

                savedPointId = dao.insertPoint(newPoint)

            } else {

                // Read the latest record before editing.
                val existing = dao.getPointWithTags(pointId)?.point
                    ?: error("This location no longer exists")

                val addressChanged =
                    existing.address != cleanAddress

                // Keep ratings and completion information.
                val updatedPoint = existing.copy(
                    name = cleanName,
                    address = cleanAddress,
                    task = cleanTask,
                    latitude = if (addressChanged) null
                    else existing.latitude,
                    longitude = if (addressChanged) null
                    else existing.longitude
                )

                check(dao.updatePoint(updatedPoint) == 1) {
                    "The location could not be updated"
                }

                savedPointId = pointId
            }

            // Replace this location's old tag selections.
            dao.deletePointTagLinks(savedPointId)

            val links = mutableListOf<PointTagCrossRef>()

            for (tagName in cleanTags) {

                // Reuse a tag if its name already exists.
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

            // Store each location-to-tag connection once.
            dao.insertPointTagLinks(links.distinct())

            savedPointId
        }
    }

    // Delete a location while keeping shared tags.
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

    // Load a member when opening the Edit form.
    suspend fun getMember(memberId: Long): TeamMember? {
        require(memberId > 0L) {
            "Invalid member ID"
        }

        return dao.getMember(memberId)
    }

    // Use memberId = 0 for a new member.
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

        // Email is optional, but check it when provided.
        require(
            cleanEmail.isEmpty() ||
                    Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()
        ) {
            "Enter a valid email address"
        }

        // Keep phone numbers as text to preserve + and leading zeros.
        return database.withTransaction {

            if (memberId == 0L) {

                val newMember = TeamMember(
                    name = cleanName,
                    email = cleanEmail,
                    phone = cleanPhone
                )

                dao.insertMember(newMember)

            } else {

                // Update the existing member without changing its ID.
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

    // Return false if the member was already removed.
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