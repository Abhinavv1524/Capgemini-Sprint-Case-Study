package in.cg.skillsync.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.cg.skillsync.auth.config.JwtFilter;
import in.cg.skillsync.auth.dto.LoginRequest;
import in.cg.skillsync.auth.dto.RegisterRequest;
import in.cg.skillsync.auth.service.AuthService;
import in.cg.skillsync.common.dto.ResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CacheManager cacheManager;

    @Test
    void testRegister_Success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("John");
        request.setEmail("john@test.com");
        request.setPassword("Pass@123");
        request.setRole("ROLE_LEARNER");

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(new ResponseDTO<>(true, "User registered successfully", null));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    @Test
    void testRegister_InvalidRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("John");
        request.setEmail("john@test.com");
        request.setRole("ROLE_LEARNER");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@test.com");
        request.setPassword("Pass@123");

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new ResponseDTO<>(true, "Login successful", "jwt-token"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("jwt-token"));
    }

    @Test
    void testProtectedEndpoint_ReturnsExpectedMessage() throws Exception {
        mockMvc.perform(get("/api/auth/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("Protected API working"));
    }
}
