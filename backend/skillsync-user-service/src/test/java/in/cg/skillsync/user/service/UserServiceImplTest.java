package in.cg.skillsync.user.service;

import in.cg.skillsync.common.exception.BadRequestException;
import in.cg.skillsync.common.exception.ResourceNotFoundException;
import in.cg.skillsync.user.dto.UserRequestDTO;
import in.cg.skillsync.user.dto.UserResponseDTO;
import in.cg.skillsync.user.entity.User;
import in.cg.skillsync.user.repository.UserRepository;
import in.cg.skillsync.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void testCreateUser_Success() {
        UserRequestDTO request = buildRequest("A", "a@test.com");
        User saved = buildEntity(1L, 101L, "A", "a@test.com", "ROLE_LEARNER");

        when(userRepository.findByAuthUserId(101L)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponseDTO response = userService.createUser(101L, "ROLE_LEARNER", request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(101L, response.getAuthUserId());
        assertEquals("ROLE_LEARNER", response.getRole());
    }

    @Test
    void testCreateUser_DuplicateAuthUserId() {
        when(userRepository.findByAuthUserId(101L)).thenReturn(Optional.of(new User()));

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> userService.createUser(101L, "ROLE_LEARNER", buildRequest("A", "a@test.com"))
        );
        assertEquals("User already exists", ex.getMessage());
    }

    @Test
    void testGetUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(buildEntity(1L, 101L, "A", "a@test.com", "ROLE_LEARNER")));

        UserResponseDTO response = userService.getUserById(1L);

        assertEquals(1L, response.getId());
        assertEquals("A", response.getName());
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(99L));
    }

    @Test
    void testGetUserByAuthUserId_NotFound() {
        when(userRepository.findByAuthUserId(500L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserByAuthUserId(500L));
    }

    @Test
    void testGetAllUsers_Success() {
        when(userRepository.findAll()).thenReturn(List.of(
                buildEntity(1L, 101L, "A", "a@test.com", "ROLE_LEARNER"),
                buildEntity(2L, 102L, "B", "b@test.com", "ROLE_MENTOR")
        ));

        List<UserResponseDTO> response = userService.getAllUsers();

        assertEquals(2, response.size());
    }

    @Test
    void testUpdateUser_Success() {
        User existing = buildEntity(1L, 101L, "Old", "old@test.com", "ROLE_LEARNER");
        User updated = buildEntity(1L, 101L, "New", "new@test.com", "ROLE_LEARNER");
        UserRequestDTO request = buildRequest("New", "new@test.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenReturn(updated);

        UserResponseDTO response = userService.updateUser(1L, request);

        assertEquals("New", response.getName());
        assertEquals("new@test.com", response.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateUser_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(999L, buildRequest("A", "a@test.com")));
    }

    private UserRequestDTO buildRequest(String name, String email) {
        UserRequestDTO request = new UserRequestDTO();
        request.setName(name);
        request.setEmail(email);
        request.setBio("bio");
        request.setSkills("java,spring");
        return request;
    }

    private User buildEntity(Long id, Long authUserId, String name, String email, String role) {
        User user = new User();
        user.setId(id);
        user.setAuthUserId(authUserId);
        user.setName(name);
        user.setEmail(email);
        user.setBio("bio");
        user.setSkills("java,spring");
        user.setRole(role);
        return user;
    }
}
