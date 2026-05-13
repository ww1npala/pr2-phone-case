package com.phonecase.service;

import com.phonecase.util.ValidationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Юніт-тести для ValidationUtil.
 * Перевіряє валідацію email, username, пароля та ціни.
 */
@DisplayName("ValidationUtil — юніт-тести")
class ValidationUtilTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "user@gmail.com",
        "user.name@domain.org",
        "user+tag@example.co.ua",
        "test123@test.com"
    })
    @DisplayName("Валідні email-адреси")
    void testValidEmails(String email) {
        assertTrue(ValidationUtil.isValidEmail(email),
                "Email має бути валідним: " + email);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "not-an-email",
        "missing@",
        "@no-local.com",
        "spaces in@email.com",
        ""
    })
    @DisplayName("Невалідні email-адреси")
    void testInvalidEmails(String email) {
        assertFalse(ValidationUtil.isValidEmail(email),
                "Email має бути невалідним: " + email);
    }

    @Test
    @DisplayName("Null email — невалідний")
    void testNullEmail() {
        assertFalse(ValidationUtil.isValidEmail(null));
    }


    @ParameterizedTest
    @ValueSource(strings = {"user", "user123", "user_name", "ABC", "abc_123"})
    @DisplayName("Валідні usernames")
    void testValidUsernames(String username) {
        assertTrue(ValidationUtil.isValidUsername(username));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ab", "user name", "user@mail", "", "   "})
    @DisplayName("Невалідні usernames")
    void testInvalidUsernames(String username) {
        assertFalse(ValidationUtil.isValidUsername(username));
    }


    @Test
    @DisplayName("Пароль 6+ символів — валідний")
    void testValidPassword() {
        assertTrue(ValidationUtil.isValidPassword("secure123"));
        assertTrue(ValidationUtil.isValidPassword("123456"));
    }

    @Test
    @DisplayName("Пароль менше 6 символів — невалідний")
    void testShortPassword() {
        assertFalse(ValidationUtil.isValidPassword("12345"));
        assertFalse(ValidationUtil.isValidPassword(""));
        assertFalse(ValidationUtil.isValidPassword(null));
    }


    @Test
    @DisplayName("Допустимі ціни")
    void testValidPrices() {
        assertTrue(ValidationUtil.isValidPrice(0.0));
        assertTrue(ValidationUtil.isValidPrice(199.99));
        assertTrue(ValidationUtil.isValidPrice(99999.99));
    }

    @Test
    @DisplayName("Від'ємна ціна — невалідна")
    void testNegativePrice() {
        assertFalse(ValidationUtil.isValidPrice(-1.0));
        assertFalse(ValidationUtil.isValidPrice(-0.01));
    }


    @Test
    @DisplayName("emailError повертає null для валідного email")
    void testEmailErrorNull() {
        assertNull(ValidationUtil.emailError("valid@gmail.com"));
    }

    @Test
    @DisplayName("emailError повертає повідомлення для невалідного email")
    void testEmailErrorMessage() {
        String error = ValidationUtil.emailError("invalid");
        assertNotNull(error);
        assertFalse(error.isBlank());
    }

    @Test
    @DisplayName("passwordError повертає null для валідного пароля")
    void testPasswordErrorNull() {
        assertNull(ValidationUtil.passwordError("secure123"));
    }

    @Test
    @DisplayName("passwordError для короткого пароля")
    void testPasswordErrorShort() {
        String error = ValidationUtil.passwordError("123");
        assertNotNull(error);
        assertTrue(error.contains("6 символів"));
    }
}