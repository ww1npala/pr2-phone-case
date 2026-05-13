package com.phonecase.util;

import java.util.regex.Pattern;

/**
 * Утилітний клас для валідації вхідних даних.
 * Реалізує принцип DRY — централізовані правила валідації.
 */

public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$");

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[a-zA-Z0-9_]{3,30}$");

    private ValidationUtil() {}

    /**
     * Перевіряє формат email
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Перевіряє ім'я користувача (3-30 символів, лише латиниця, цифри, _)
     */
    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    /**
     * Перевіряє пароль (мінімум 6 символів)
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * Перевіряє що ціна невід'ємна та не перевищує максимум
     */
    public static boolean isValidPrice(double price) {
        return price >= 0 && price <= 100_000;
    }

    /**
     * Перевіряє рядок на непорожність
     */
    public static boolean isNotBlank(String value, int minLength) {
        return value != null && value.trim().length() >= minLength;
    }

    /**
     * Повертає рядок помилки для email, або null
     */
    public static String emailError(String email) {
        if (email == null || email.isBlank()) return "Email обов'язковий.";
        if (!isValidEmail(email)) return "Невірний формат email (наприклад: user@gmail.com).";
        return null;
    }

    /**
     * Повертає рядок помилки для пароля, або null
     */
    public static String passwordError(String password) {
        if (password == null || password.isBlank()) return "Пароль обов'язковий.";
        if (!isValidPassword(password)) return "Пароль повинен містити щонайменше 6 символів.";
        return null;
    }
}