package com.example.athleteiq.data

data class AthleteProfile(val name: String = "Thando Dlamini", val age: String = "29", val sport: String = "Running", val weeklyHours: String = "8", val weightKg: String = "", val heightCm: String = "")
data class RaceGoal(val name: String, val distanceKm: Double, val targetMinutes: Int)
data class TrainingSession(val day: String, val title: String, val details: String, val distanceKm: Double)

/** Local sample data; replace this boundary with a persistent data source when connecting accounts. */
class AthleteRepository {
    val profile = AthleteProfile()
    val goals = listOf(RaceGoal("Cape Town Half Marathon", 21.1, 110))
    val sessions = listOf(
        TrainingSession("MON", "Easy aerobic run", "45 min · 6:00 /km · Average HR 138 bpm", 7.5),
        TrainingSession("TUE", "Threshold intervals", "4 × 8 min · 5:05 /km · 2 min recovery", 10.0),
        TrainingSession("THU", "Recovery run", "30 min · 6:20 /km · Keep it conversational", 4.7),
        TrainingSession("SAT", "Long endurance run", "90 min · 6:10 /km · Easy effort", 14.6)
    )
}
