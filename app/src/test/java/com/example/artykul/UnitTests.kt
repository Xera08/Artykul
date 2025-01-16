import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class AdminFunctionsTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Simulated Firestore functions for registration
    private suspend fun simulatedRegisterAdmin(
        email: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val mockExistingEmails = listOf("existingadmin@gmail.com", "admin@gmail.com")
        if (mockExistingEmails.contains(email)) {
            onFailure("Email already exists")
        } else {
            onSuccess()
        }
    }

    // Validation function for room numbers
    private fun validateRoomNumber(input: String): Pair<Boolean, String> {
        return when {
            input.length > 3 -> Pair(false, "Room number cannot exceed 3 digits")
            input.any { !it.isDigit() } -> Pair(false, "Room number must contain only digits")
            else -> Pair(true, "Valid room number")
        }
    }

    @Test
    fun `test admin cannot register with duplicate email`() = runBlocking {
        val email = "existingadmin@gmail.com"
        var successCalled = false
        var failureMessage: String? = null

        simulatedRegisterAdmin(
            email,
            onSuccess = { successCalled = true },
            onFailure = { failureMessage = it }
        )

        assertFalse(successCalled)
        assertEquals("Email already exists", failureMessage)
    }

    @Test
    fun `test admin can register with unique email`() = runBlocking {
        val email = "newadmin@gmail.com"
        var successCalled = false
        var failureMessage: String? = null

        simulatedRegisterAdmin(
            email,
            onSuccess = { successCalled = true },
            onFailure = { failureMessage = it }
        )

        assertTrue(successCalled)
        assertEquals(null, failureMessage)
    }

    @Test
    fun `test room number cannot exceed three digits`() {
        val input = "1234"
        val (isValid, message) = validateRoomNumber(input)

        assertFalse(isValid)
        assertEquals("Room number cannot exceed 3 digits", message)
    }

    @Test
    fun `test room number must contain only digits`() {
        val input = "12A"
        val (isValid, message) = validateRoomNumber(input)

        assertFalse(isValid)
        assertEquals("Room number must contain only digits", message)
    }

    @Test
    fun `test valid room number passes validation`() {
        val input = "101"
        val (isValid, message) = validateRoomNumber(input)

        assertTrue(isValid)
        assertEquals("Valid room number", message)
    }

    // Validation function for user creation (admin)
    private fun validateUserData(name: String, email: String, password: String): Pair<Boolean, String> {
        return when {
            name.isBlank() -> Pair(false, "Name cannot be empty")
            email.isBlank() -> Pair(false, "Email cannot be empty")
            password.isBlank() -> Pair(false, "Password cannot be empty")
            else -> Pair(true, "Valid data")
        }
    }

    // Validation function for equipment data (user)
    private fun validateEquipmentData(name: String, room: String, itemCode: String): Pair<Boolean, String> {
        return when {
            name.isBlank() -> Pair(false, "Name cannot be empty")
            room.isBlank() -> Pair(false, "Room cannot be empty")
            itemCode.isBlank() -> Pair(false, "itemCode cannot be empty")
            else -> Pair(true, "Valid data")
        }
    }

    @Test
    fun `test admin cannot add user with empty name`() {
        val name = ""
        val email = "newadmin@gmail.com"
        val password = "password123"
        val (isValid, message) = validateUserData(name, email, password)

        assertFalse(isValid)
        assertEquals("Name cannot be empty", message)
    }

    @Test
    fun `test admin cannot add user with empty email`() {
        val name = "New User"
        val email = ""
        val password = "password123"
        val (isValid, message) = validateUserData(name, email, password)

        assertFalse(isValid)
        assertEquals("Email cannot be empty", message)
    }

    @Test
    fun `test admin cannot add user with empty password`() {
        val name = "New User"
        val email = "newuser@gmail.com"
        val password = ""
        val (isValid, message) = validateUserData(name, email, password)

        assertFalse(isValid)
        assertEquals("Password cannot be empty", message)
    }

    @Test
    fun `test admin can add user with all fields filled`() {
        val name = "New User"
        val email = "newuser@gmail.com"
        val password = "password123"
        val (isValid, message) = validateUserData(name, email, password)

        assertTrue(isValid)
        assertEquals("Valid data", message)
    }

    @Test
    fun `test user cannot add equipment with empty name`() {
        val name = ""
        val room = "039"
        val itemCode = "12345"
        val (isValid, message) = validateEquipmentData(name, room, itemCode)

        assertFalse(isValid)
        assertEquals("Name cannot be empty", message)
    }

    @Test
    fun `test user cannot add equipment with empty room`() {
        val name = "Laptop"
        val room = ""
        val itemCode = "12345"
        val (isValid, message) = validateEquipmentData(name, room, itemCode)

        assertFalse(isValid)
        assertEquals("Room cannot be empty", message)
    }

    @Test
    fun `test user cannot add equipment with empty itemCode`() {
        val name = "Laptop"
        val room = "039"
        val itemCode = ""
        val (isValid, message) = validateEquipmentData(name, room, itemCode)

        assertFalse(isValid)
        assertEquals("itemCode cannot be empty", message)
    }

    @Test
    fun `test user can add equipment with all fields filled`() {
        val name = "Laptop"
        val room = "039"
        val itemCode = "12345"
        val (isValid, message) = validateEquipmentData(name, room, itemCode)

        assertTrue(isValid)
        assertEquals("Valid data", message)
    }

}
