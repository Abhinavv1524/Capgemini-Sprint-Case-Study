package in.cg.skillsync.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.cg.skillsync.common.exception.UnauthorizedException;
import in.cg.skillsync.user.config.SecurityConfig;
import in.cg.skillsync.user.dto.UserRequestDTO;
import in.cg.skillsync.user.dto.UserResponseDTO;
import in.cg.skillsync.user.exception.GlobalExceptionHandler;
import in.cg.skillsync.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CacheManager cacheManager;

    // CREATE USER SUCCESS
    @Test
    void testCreateUser_Success() throws Exception {
        UserRequestDTO request = new UserRequestDTO();
        request.setName("John");
        request.setEmail("john@test.com");
        request.setBio("bio");
        request.setSkills("java");

        UserResponseDTO response = new UserResponseDTO();
        response.setId(1L);
        response.setAuthUserId(10L);
        response.setName("John");
        response.setEmail("john@test.com");
        response.setRole("ROLE_LEARNER");

        when(userService.createUser(eq(10L), eq("ROLE_LEARNER"), any(UserRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "ROLE_LEARNER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    // INVALID BODY
    @Test
    void testCreateUser_InvalidBody() throws Exception {
        UserRequestDTO request = new UserRequestDTO();
        request.setEmail("john@test.com");

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .with(user("learner").roles("LEARNER"))
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "ROLE_LEARNER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // UNAUTHORIZED
    @Test
    void testGetUserById_Unauthorized_NoHeaders() throws Exception {
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isUnauthorized());
    }

    // GET BY ID SUCCESS
    @Test
    void testGetUserById_Success() throws Exception {
        UserResponseDTO response = new UserResponseDTO();
        response.setId(1L);
        response.setName("John");

        when(userService.getUserById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/users/1")
                        .with(user("learner").roles("LEARNER"))
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "ROLE_LEARNER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("John"));
    }

    // GET BY AUTH USER ID (NEW)
    @Test
    void testGetUserByAuthUserId_Success() throws Exception {
        UserResponseDTO response = new UserResponseDTO();
        response.setId(1L);
        response.setName("John");

        when(userService.getUserByAuthUserId(10L)).thenReturn(response);

        mockMvc.perform(get("/api/users/auth/10")
                        .with(user("admin").roles("ADMIN"))
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("John"));
    }

    // GET ALL USERS (NEW)
    @Test
    void testGetAllUsers_Success() throws Exception {
        UserResponseDTO user = new UserResponseDTO();
        user.setId(1L);
        user.setName("John");

        when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users")
                        .with(user("admin").roles("ADMIN"))
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("John"));
    }

    // UPDATE FORBIDDEN
    @Test
    void testUpdateUser_Forbidden() throws Exception {
        UserRequestDTO request = new UserRequestDTO();
        request.setName("John");
        request.setEmail("john@test.com");

        when(userService.updateUser(eq(1L), any(UserRequestDTO.class)))
                .thenThrow(new UnauthorizedException("Not allowed"));

        mockMvc.perform(put("/api/users/1")
                        .with(csrf())
                        .with(user("mentor").roles("MENTOR"))
                        .header("X-User-Id", "20")
                        .header("X-User-Role", "ROLE_MENTOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Not allowed"));
    }

    // UPDATE SUCCESS (NEW)
    @Test
    void testUpdateUser_Success() throws Exception {
        UserRequestDTO request = new UserRequestDTO();
        request.setName("Updated");
        request.setEmail("updated@test.com");

        UserResponseDTO response = new UserResponseDTO();
        response.setId(1L);
        response.setName("Updated");

        when(userService.updateUser(eq(1L), any(UserRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/users/1")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "ROLE_ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated"));
    }
}