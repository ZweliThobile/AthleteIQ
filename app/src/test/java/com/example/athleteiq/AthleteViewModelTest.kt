package com.example.athleteiq

import com.example.athleteiq.ui.AthleteViewModel
import com.example.athleteiq.ui.InputValidation
import com.example.athleteiq.data.AthleteProfile
import com.example.athleteiq.data.MemoryAccountStore
import org.junit.Assert.*
import org.junit.Test

class AthleteViewModelTest {
    @Test fun loginRejectsInvalidCredentials() {
        assertNotNull(InputValidation.login("invalid", "longpassword"))
        assertNotNull(InputValidation.login("runner@example.com", "short"))
        assertNull(InputValidation.login("runner@example.com", "password123"))
    }
    @Test fun profileValidatesBounds() {
        assertNotNull(InputValidation.profile(AthleteProfile(age = "15")))
        assertNotNull(InputValidation.profile(AthleteProfile(weeklyHours = "41")))
        assertNull(InputValidation.profile(AthleteProfile()))
    }
    @Test fun measurementsAllowEmptyOrValidDecimalsAndRejectInvalidValues() {
        assertNull(InputValidation.profile(AthleteProfile(weightKg = "72.5", heightCm = "178.5")))
        assertNull(InputValidation.profile(AthleteProfile(weightKg = "", heightCm = "")))
        listOf("abc", "NaN", "Infinity", "-1", "501").forEach {
            assertNotNull(InputValidation.profile(AthleteProfile(weightKg = it)))
        }
        listOf("abc", "NaN", "Infinity", "0", "261").forEach {
            assertNotNull(InputValidation.profile(AthleteProfile(heightCm = it)))
        }
    }
    @Test fun goalsRejectNonFiniteAndInvalidNumbers() {
        listOf("NaN", "Infinity", "0", "abc").forEach {
            assertNotNull(InputValidation.goal("City Marathon", it, "240"))
        }
        assertNotNull(InputValidation.goal("City Marathon", "42.2", "0"))
    }
    @Test fun signUpSavesProfileAndSignInRestoresIt() {
        val store = MemoryAccountStore()
        val first = AthleteViewModel(store)
        val profile = AthleteProfile("Alex Runner", "32", "Cycling", "10", "72.5", "178")
        assertNull(first.signUp(" ALEX@example.com ", "password123", "password123", profile))
        assertNull(first.saveProfile(profile.copy(weeklyHours = "12")))
        first.logout()
        val reopened = AthleteViewModel(store)
        assertNotNull(reopened.login("alex@example.com", "wrongpassword"))
        assertFalse(reopened.state.value.signedIn)
        assertNull(reopened.login("alex@example.com", "password123"))
        assertEquals(profile.copy(weeklyHours = "12"), reopened.state.value.profile)
        assertNotEquals("password123", store.find("alex@example.com")!!.passwordHash)
    }
    @Test fun signUpRejectsDuplicatesAndMismatchedPasswords() {
        val model = AthleteViewModel()
        assertNotNull(model.signUp("alex@example.com", "password123", "different", AthleteProfile()))
        assertNotNull(model.login("alex@example.com", "password123"))
        assertNull(model.signUp("alex@example.com", "password123", "password123", AthleteProfile()))
        assertNotNull(model.signUp("ALEX@example.com", "password123", "password123", AthleteProfile()))
    }
    @Test fun validChangesUpdateStateAndInvalidChangesDoNot() {
        val model = AthleteViewModel()
        assertNull(model.signUp("alex@example.com", "password123", "password123", AthleteProfile()))
        val count = model.state.value.goals.size
        assertNotNull(model.addGoal("", "42.2", "240"))
        assertEquals(count, model.state.value.goals.size)
        assertNull(model.addGoal("City Marathon", "42.2", "240"))
        assertEquals(count + 1, model.state.value.goals.size)
        model.removeGoal(model.state.value.goals.last())
        assertEquals(count, model.state.value.goals.size)
        assertNull(model.saveProfile(AthleteProfile(name = "Alex Runner")))
        assertEquals("Alex Runner", model.state.value.profile.name)
        model.login("alex@example.com", "password123")
        assertTrue(model.state.value.signedIn)
        model.logout()
        assertFalse(model.state.value.signedIn)
    }
}
