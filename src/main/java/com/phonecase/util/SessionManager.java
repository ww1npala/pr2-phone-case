package com.phonecase.util;

import com.phonecase.dto.UserDTO;

/**
 * Утилітний клас для збереження поточної сесії користувача.
 */

public final class SessionManager {

    private static UserDTO currentUser;

    private SessionManager() {

    }

    public static void setCurrentUser(UserDTO user) {
        currentUser = user;
    }


    public static UserDTO getCurrentUser() {
        return currentUser;
    }


    public static boolean isAdmin() {
        return currentUser != null && "ADMIN".equals(currentUser.getRole());
    }

    public static void logout() {
        currentUser = null;
    }
}