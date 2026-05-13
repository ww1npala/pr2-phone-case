package com.phonecase.service;

import com.phonecase.dto.UserDTO;
import com.phonecase.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Сервіс для роботи з користувачами.
 * Реалізує бізнес-логіку автентифікації, авторизації та реєстрації.
 */
public interface UserService {
    Optional<UserDTO> authenticate(String username, String password);

    UserDTO register(String username, String password, String email);

    List<UserDTO> getAllUsers();
    Optional<UserDTO> findByUsername(String username);
    UserDTO updateUser(UserDTO userDTO);
    boolean deleteUser(int userId);


    boolean isAdmin(UserDTO user);
}