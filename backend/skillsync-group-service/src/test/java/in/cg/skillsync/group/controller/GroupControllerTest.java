package in.cg.skillsync.group.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.cg.skillsync.common.exception.UnauthorizedException;
import in.cg.skillsync.group.config.SecurityConfig;
import in.cg.skillsync.group.dto.CreateGroupRequestDTO;
import in.cg.skillsync.group.dto.GroupResponseDTO;
import in.cg.skillsync.group.exception.GlobalExceptionHandler;
import in.cg.skillsync.group.service.GroupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GroupController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GroupService groupService;

    @MockitoBean
    private CacheManager cacheManager;

    @Test
    void testCreateGroup_Success() throws Exception {
        CreateGroupRequestDTO request = new CreateGroupRequestDTO();
        request.setName("Spring Group");
        request.setDescription("desc");

        GroupResponseDTO response = new GroupResponseDTO(1L, "Spring Group", "desc", 10L, LocalDateTime.now());
        when(groupService.createGroup(any(CreateGroupRequestDTO.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/groups")
                        .with(csrf())
                        .with(user("learner").roles("LEARNER"))
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "ROLE_LEARNER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void testCreateGroup_InvalidRequest() throws Exception {
        CreateGroupRequestDTO request = new CreateGroupRequestDTO();
        request.setDescription("desc");

        mockMvc.perform(post("/api/groups")
                        .with(csrf())
                        .with(user("learner").roles("LEARNER"))
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "ROLE_LEARNER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testJoinGroup_Unauthorized_NoHeaders() throws Exception {
        mockMvc.perform(post("/api/groups/1/join").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testJoinGroup_Forbidden() throws Exception {
        doThrow(new UnauthorizedException("Only learners can join groups"))
                .when(groupService).joinGroup(eq(1L), any());

        mockMvc.perform(post("/api/groups/1/join")
                        .with(csrf())
                        .with(user("mentor").roles("MENTOR"))
                        .header("X-User-Id", "20")
                        .header("X-User-Role", "ROLE_MENTOR"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testGetAllGroups_Success() throws Exception {
        when(groupService.getAllGroups()).thenReturn(List.of(
                new GroupResponseDTO(1L, "Spring Group", "desc", 10L, LocalDateTime.now())
        ));

        mockMvc.perform(get("/api/groups")
                        .with(user("learner").roles("LEARNER"))
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "ROLE_LEARNER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Spring Group"));
    }
    
    @Test
    void testJoinGroup_Success() throws Exception {
        mockMvc.perform(post("/api/groups/1/join")
                        .with(csrf())
                        .with(user("learner").roles("LEARNER"))
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "ROLE_LEARNER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Joined group successfully"));
    }
    
    @Test
    void testLeaveGroup_Success() throws Exception {
        mockMvc.perform(post("/api/groups/1/leave")
                        .with(csrf())
                        .with(user("learner").roles("LEARNER"))
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "ROLE_LEARNER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Left group successfully"));
    }
    
    @Test
    void testLeaveGroup_Forbidden() throws Exception {
        doThrow(new UnauthorizedException("Not allowed"))
                .when(groupService).leaveGroup(eq(1L), any());

        mockMvc.perform(post("/api/groups/1/leave")
                        .with(csrf())
                        .with(user("mentor").roles("MENTOR"))
                        .header("X-User-Id", "20")
                        .header("X-User-Role", "ROLE_MENTOR"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void testGetGroupById_Success() throws Exception {
        GroupResponseDTO response =
                new GroupResponseDTO(1L, "Spring Group", "desc", 10L, LocalDateTime.now());

        when(groupService.getGroupById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/groups/1")
                        .with(user("learner").roles("LEARNER"))
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "ROLE_LEARNER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Spring Group"));
    }
}
