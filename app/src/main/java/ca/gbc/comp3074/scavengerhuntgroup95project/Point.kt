package ca.gbc.comp3074.scavengerhuntgroup95project

data class Point(
    val name: String,
    val address: String,
    val tags: List<String>,
    val rating: Int,
    val done: Boolean
)

// This is temporary sample data for the UI prototype
// we'll change it later

object SampleData {
    val points = listOf(
        Point("CN Tower", "290 Bremner Blvd, Toronto", listOf("photo", "easy"), 4, true),
        Point("Graffiti Alley", "Rush Lane, Toronto", listOf("photo", "hard"), 0, false),
        Point("St. Lawrence Market", "93 Front St E, Toronto", listOf("info"), 3, false)
    )
}