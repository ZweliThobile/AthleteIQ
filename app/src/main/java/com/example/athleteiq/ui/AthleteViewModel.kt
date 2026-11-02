package com.example.athleteiq.ui

import androidx.lifecycle.ViewModel
import com.example.athleteiq.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AthleteUiState(
    val signedIn: Boolean = false,
    val profile: AthleteProfile = AthleteProfile(),
    val goals: List<RaceGoal> = emptyList(),
    val sessions: List<TrainingSession> = emptyList()
)

object InputValidation {
    fun login(email: String, password: String): String? = when {
        !Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(email.trim()) -> "Enter a valid email address."
        password.length < 8 -> "Use a password with at least 8 characters."
        else -> null
    }
    fun profile(profile: AthleteProfile): String? = when {
        profile.name.trim().length < 2 -> "Enter your full name."
        profile.age.toIntOrNull() !in 16..100 -> "Age must be between 16 and 100."
        profile.weeklyHours.toIntOrNull() !in 1..40 -> "Weekly training must be between 1 and 40 hours."
        profile.weightKg.isNotBlank() && profile.weightKg.toDoubleOrNull()?.let { it.isFinite() && it in 20.0..500.0 } != true -> "Enter a weight between 20 and 500 kg, or leave it blank."
        profile.heightCm.isNotBlank() && profile.heightCm.toDoubleOrNull()?.let { it.isFinite() && it in 80.0..260.0 } != true -> "Enter a height between 80 and 260 cm, or leave it blank."
        else -> null
    }
    fun goal(name: String, distance: String, minutes: String): String? = when {
        name.trim().length < 3 -> "Enter a race name with at least 3 characters."
        distance.toDoubleOrNull()?.let { it.isFinite() && it in 1.0..300.0 } != true -> "Distance must be between 1 and 300 km."
        minutes.toIntOrNull() !in 5..6000 -> "Target time must be between 5 and 6000 minutes."
        else -> null
    }
}

class AthleteViewModel(private val accounts: AccountStore = MemoryAccountStore()) : ViewModel() {
    private var activeAccount: LocalAccount? = null
    private val repository = AthleteRepository()
    private val mutableState = MutableStateFlow(AthleteUiState(profile = repository.profile, goals = repository.goals, sessions = repository.sessions))
    val state = mutableState.asStateFlow()
    fun login(email: String, password: String): String? {
        InputValidation.login(email, password)?.let { return it }
        return try {
            val account = accounts.find(AccountPasswords.normalize(email))
            if (account == null || !AccountPasswords.matches(password, account)) {
                "Email or password is incorrect. Sign up if you do not have an account."
            } else {
                activeAccount = account
                mutableState.update { it.copy(signedIn = true, profile = account.profile, goals = repository.goals) }
                null
            }
        } catch (_: Exception) { "Unable to read your account. Please try again." }
    }
    fun signUp(email: String, password: String, confirmPassword: String, profile: AthleteProfile): String? {
        InputValidation.login(email, password)?.let { return it }
        if (password != confirmPassword) return "Passwords do not match."
        InputValidation.profile(profile)?.let { return it }
        return try {
            val normalizedEmail = AccountPasswords.normalize(email)
            if (accounts.find(normalizedEmail) != null) return "An account with this email already exists. Please sign in."
            val salt = AccountPasswords.salt()
            val account = LocalAccount(normalizedEmail, salt, AccountPasswords.hash(password, salt), profile.copy(name = profile.name.trim()))
            if (!accounts.save(account)) return "Your account could not be saved. Please try again."
            activeAccount = account
            mutableState.update { it.copy(signedIn = true, profile = account.profile, goals = repository.goals) }
            null
        } catch (_: Exception) { "Your account could not be saved. Please try again." }
    }
    fun saveProfile(profile: AthleteProfile): String? {
        InputValidation.profile(profile)?.let { return it }
        val account = activeAccount ?: return "Please sign in before saving your profile."
        val updated = account.copy(profile = profile.copy(name = profile.name.trim()))
        return try {
            if (!accounts.save(updated)) return "Your profile could not be saved. Please try again."
            activeAccount = updated
            mutableState.update { it.copy(profile = updated.profile) }
            null
        } catch (_: Exception) { "Your profile could not be saved. Please try again." }
    }
    fun addGoal(name: String, distance: String, minutes: String): String? {
        val error = InputValidation.goal(name, distance, minutes)
        if (error == null) mutableState.update { it.copy(goals = it.goals + RaceGoal(name.trim(), distance.toDouble(), minutes.toInt())) }
        return error
    }
    fun removeGoal(goal: RaceGoal) = mutableState.update { it.copy(goals = it.goals - goal) }
    fun logout() {
        activeAccount = null
        mutableState.update { AthleteUiState(sessions = repository.sessions) }
    }
}
