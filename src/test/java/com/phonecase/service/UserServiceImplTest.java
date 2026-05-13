package com.phonecase.service;

import com.phonecase.dto.UserDTO;
import com.phonecase.model.User;
import com.phonecase.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Юніт-тести для UserServiceImpl.
 * Використовує Mockito для мокірування UserRepository.
 * Перевіряє бізнес-логіку без реального підключення до БД.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService — юніт-тести")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "$2a$10$admin_hash_placeholder", "test@test.com", "USER");
        testUser.setId(1);
        testUser.setActive(true);
    }


    @Test
    @DisplayName("Успішна автентифікація з правильними даними")
    void testAuthenticateSuccess() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        Optional<UserDTO> result = userService.authenticate("testuser", "anypassword");

        assertTrue(result.isPresent(), "Автентифікація повинна бути успішною");
        assertEquals("testuser", result.get().getUsername());
        assertEquals("USER", result.get().getRole());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("Невдала автентифікація для неіснуючого користувача")
    void testAuthenticateUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        Optional<UserDTO> result = userService.authenticate("unknown", "password");

        assertFalse(result.isPresent(), "Автентифікація повинна провалитись для неіснуючого юзера");
    }

    @Test
    @DisplayName("Невдала автентифікація для неактивного користувача")
    void testAuthenticateInactiveUser() {
        testUser.setActive(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        Optional<UserDTO> result = userService.authenticate("testuser", "anypassword");

        assertFalse(result.isPresent(), "Неактивний користувач не повинен авторизуватись");
    }

    @Test
    @DisplayName("Автентифікація з порожнім username повертає порожній Optional")
    void testAuthenticateEmptyUsername() {
        Optional<UserDTO> result = userService.authenticate("", "password");

        assertFalse(result.isPresent());
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    @DisplayName("Автентифікація з null паролем повертає порожній Optional")
    void testAuthenticateNullPassword() {
        Optional<UserDTO> result = userService.authenticate("testuser", null);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Успішна реєстрація нового користувача")
    void testRegisterSuccess() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        User savedUser = new User("newuser", "hash", "new@test.com", "USER");
        savedUser.setId(10);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDTO result = userService.register("newuser", "password123", "new@test.com");

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("USER", result.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Реєстрація з вже існуючим username кидає виключення")
    void testRegisterDuplicateUsername() {
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
            userService.register("existinguser", "password123", "email@test.com")
        );
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Реєстрація з коротким паролем кидає виключення")
    void testRegisterShortPassword() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            userService.register("validuser", "12345", "valid@test.com")
        );
        assertTrue(ex.getMessage().contains("6 символів"));
    }

    @Test
    @DisplayName("Реєстрація з невалідним email кидає виключення")
    void testRegisterInvalidEmail() {
        assertThrows(IllegalArgumentException.class, () ->
            userService.register("validuser", "password123", "not-an-email")
        );
    }

    @Test
    @DisplayName("Реєстрація з коротким username кидає виключення")
    void testRegisterShortUsername() {
        assertThrows(IllegalArgumentException.class, () ->
            userService.register("ab", "password123", "valid@test.com")
        );
    }


    @Test
    @DisplayName("getAllUsers повертає список DTO")
    void testGetAllUsers() {
        List<User> users = List.of(
                testUser,
                new User("user2", "hash", "user2@test.com", "ADMIN")
        );
        when(userRepository.findAll()).thenReturn(users);

        List<UserDTO> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("testuser", result.get(0).getUsername());
    }

    @Test
    @DisplayName("isAdmin повертає true для ADMIN ролі")
    void testIsAdmin() {
        UserDTO adminUser = new UserDTO();
        adminUser.setRole("ADMIN");

        UserDTO regularUser = new UserDTO();
        regularUser.setRole("USER");

        assertTrue(userService.isAdmin(adminUser));
        assertFalse(userService.isAdmin(regularUser));
        assertFalse(userService.isAdmin(null));
    }
}