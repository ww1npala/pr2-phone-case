package com.phonecase.service;

import com.phonecase.dto.UserDTO;
import com.phonecase.model.User;
import com.phonecase.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Реалізація UserService.
 * Реалізує патерни: Singleton (через Guice), Facade (приховує деталі репозиторіїв),
 * DI (через конструктор).
 * Забезпечує валідацію бізнес-логіки.
 */
@Singleton
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    @Inject
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<UserDTO> authenticate(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            logger.warn("Спроба входу з порожніми даними.");
            return Optional.empty();
        }

        return userRepository.findByUsername(username.trim())
                .filter(User::isActive)
                .filter(u -> verifyPassword(password, u.getPassword()))
                .map(this::toDTO);
    }

    @Override
    public UserDTO register(String username, String password, String email) {
        validateUsername(username);
        validatePassword(password);
        validateEmail(email);

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Користувач з таким ім'ям вже існує: " + username);
        }

        User user = new User(username.trim(), hashPassword(password), email.trim(), "USER");
        User saved = userRepository.save(user);
        logger.info("Зареєстровано нового користувача: {}", username);
        return toDTO(saved);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public Optional<UserDTO> findByUsername(String username) {
        return userRepository.findByUsername(username).map(this::toDTO);
    }

    @Override
    public UserDTO updateUser(UserDTO dto) {
        validateEmail(dto.getEmail());
        User user = userRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Користувача не знайдено: " + dto.getId()));
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setActive(dto.isActive());
        return toDTO(userRepository.update(user));
    }

    @Override
    public boolean deleteUser(int userId) {
        return userRepository.deleteById(userId);
    }

    @Override
    public boolean isAdmin(UserDTO user) {
        return user != null && "ADMIN".equals(user.getRole());
    }


    private void validateUsername(String username) {
        if (username == null || username.trim().length() < 3)
            throw new IllegalArgumentException("Ім'я користувача повинно містити щонайменше 3 символи.");
        if (!username.trim().matches("[a-zA-Z0-9_]+"))
            throw new IllegalArgumentException("Ім'я користувача може містити лише літери, цифри та підкреслення.");
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 6)
            throw new IllegalArgumentException("Пароль повинен містити щонайменше 6 символів.");
    }

    private void validateEmail(String email) {
        if (email == null || !email.trim().matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$"))
            throw new IllegalArgumentException("Невірний формат email: " + email);
    }

    /**
     * спрощений хеш пароля для demo-версії
     * потім замінити на BCrypt
     */
    private String hashPassword(String plaintext) {
        return "$2a$10$" + plaintext.hashCode() + "_hash";
    }

    /**
     * перевірка пароля в demo-режимі приймає любий пароль для тестових даних
     */
    private boolean verifyPassword(String plaintext, String hash) {
        if (hash != null && hash.contains("_placeholder")) return true;
        return hash != null && hash.equals(hashPassword(plaintext));
    }

    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        return dto;
    }
}