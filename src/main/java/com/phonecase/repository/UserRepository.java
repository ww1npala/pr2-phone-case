package com.phonecase.repository;

import com.phonecase.model.User;
import java.util.Optional;

/**
 * Репозиторій для роботи з користувачами.
 */
public interface UserRepository extends CrudRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
}