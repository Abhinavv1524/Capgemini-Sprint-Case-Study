package in.cg.skillsync.auth.service;

import in.cg.skillsync.auth.client.UserClient;
import in.cg.skillsync.auth.config.JwtUtil;
import in.cg.skillsync.auth.dto.LoginRequest;
import in.cg.skillsync.auth.dto.RegisterRequest;
import in.cg.skillsync.auth.entity.Role;
import in.cg.skillsync.auth.entity.User;
import in.cg.skillsync.auth.repository.RoleRepository;
import in.cg.skillsync.auth.repository.UserRepository;
import in.cg.skillsync.auth.service.impl.AuthServiceImpl;
import in.cg.skillsync.common.dto.ResponseDTO;
import in.cg.skillsync.common.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setName("John");
        request.setEmail("john@test.com");
        request.setPassword("Pass@123");
        request.setRole("ROLE_LEARNER");

        Role role = new Role("ROLE_LEARNER");
        User saved = new User();
        saved.setId(10L);
        saved.setName("John");
        saved.setEmail("john@test.com");

        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_LEARNER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("Pass@123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(userClient.createUserProfile(eq(10L), eq("ROLE_LEARNER"), any()))
                .thenReturn(new ResponseDTO<>(true, "ok", null));

        ResponseDTO<?> response = authService.register(request);

        assertTrue(response.isSuccess());
        assertEquals("User registered successfully", response.getMessage());
        verify(userRepository).save(any(User.class));
        verify(userClient).createUserProfile(eq(10L), eq("ROLE_LEARNER"), any());
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("john@test.com");

        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(new User()));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.register(request));
        assertEquals("Email already exists", ex.getMessage());
    }

    @Test
    void testRegister_RoleNotFound() {
        RegisterRequest request = new RegisterRequest();
        request.setName("John");
        request.setEmail("john@test.com");
        request.setPassword("Pass@123");
        request.setRole("ROLE_UNKNOWN");

        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_UNKNOWN")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Pass@123")).thenReturn("encoded");

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.register(request));
        assertEquals("Role not found", ex.getMessage());
    }

    @Test
    void testRegister_UserProfileSyncFailed() {
        RegisterRequest request = new RegisterRequest();
        request.setName("John");
        request.setEmail("john@test.com");
        request.setPassword("Pass@123");
        request.setRole("ROLE_LEARNER");

        Role role = new Role("ROLE_LEARNER");
        User saved = new User();
        saved.setId(11L);
        saved.setName("John");
        saved.setEmail("john@test.com");

        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_LEARNER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("Pass@123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(userClient.createUserProfile(eq(11L), eq("ROLE_LEARNER"), any()))
                .thenThrow(new RuntimeException("downstream error"));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.register(request));
        assertEquals("Registration failed: User profile sync failed", ex.getMessage());
    }

    @Test
    void testLogin_Success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@test.com");
        request.setPassword("Pass@123");

        User user = new User();
        user.setId(20L);
        user.setEmail("john@test.com");
        user.setPassword("encoded");
        user.getRoles().add(new Role("ROLE_ADMIN"));

        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Pass@123", "encoded")).thenReturn(true);
        when(jwtUtil.generateToken(20L, "ROLE_ADMIN", "john@test.com")).thenReturn("jwt-token");

        ResponseDTO<String> response = authService.login(request);

        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
        assertEquals("jwt-token", response.getData());
    }

    @Test
    void testLogin_InvalidCredentials_UserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail("nope@test.com");
        request.setPassword("x");

        when(userRepository.findByEmail("nope@test.com")).thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.login(request));
        assertEquals("Invalid credentials", ex.getMessage());
    }

    @Test
    void testLogin_InvalidCredentials_WrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@test.com");
        request.setPassword("wrong");

        User user = new User();
        user.setEmail("john@test.com");
        user.setPassword("encoded");

        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.login(request));
        assertEquals("Invalid credentials", ex.getMessage());
    }
}
