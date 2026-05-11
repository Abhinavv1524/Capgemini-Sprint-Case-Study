package in.cg.skillsync.group.service;

import in.cg.skillsync.common.exception.BadRequestException;
import in.cg.skillsync.common.exception.ResourceNotFoundException;
import in.cg.skillsync.common.exception.UnauthorizedException;
import in.cg.skillsync.group.dto.CreateGroupRequestDTO;
import in.cg.skillsync.group.dto.GroupResponseDTO;
import in.cg.skillsync.group.entity.Group;
import in.cg.skillsync.group.entity.GroupMember;
import in.cg.skillsync.group.repository.GroupMemberRepository;
import in.cg.skillsync.group.repository.GroupRepository;
import in.cg.skillsync.group.service.impl.GroupServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupServiceImplTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @InjectMocks
    private GroupServiceImpl groupService;

    @Test
    void testCreateGroup_Success() {
        CreateGroupRequestDTO request = new CreateGroupRequestDTO();
        request.setName("Spring Group");
        request.setDescription("desc");

        HttpServletRequest servletRequest = requestWith("10", "ROLE_LEARNER");

        Group saved = new Group("Spring Group", "desc", 10L, LocalDateTime.now());
        ReflectionTestUtils.setField(saved, "id", 1L);

        when(groupRepository.save(any(Group.class))).thenReturn(saved);

        GroupResponseDTO response = groupService.createGroup(request, servletRequest);

        assertEquals(1L, response.getId());
        assertEquals("Spring Group", response.getName());
        verify(groupMemberRepository).save(any(GroupMember.class));
    }

    @Test
    void testJoinGroup_UnauthorizedRole() {
        HttpServletRequest servletRequest = requestWith("10", "ROLE_MENTOR");

        assertThrows(UnauthorizedException.class, () -> groupService.joinGroup(1L, servletRequest));
    }

    @Test
    void testJoinGroup_DuplicateMember() {
        HttpServletRequest servletRequest = requestWith("10", "ROLE_LEARNER");
        when(groupRepository.findById(1L)).thenReturn(Optional.of(new Group()));
        when(groupMemberRepository.existsByGroupIdAndUserId(1L, 10L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> groupService.joinGroup(1L, servletRequest));
    }

    @Test
    void testLeaveGroup_NotMember() {
        HttpServletRequest servletRequest = requestWith("10", "ROLE_LEARNER");
        when(groupMemberRepository.findByGroupIdAndUserId(1L, 10L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> groupService.leaveGroup(1L, servletRequest));
    }

    @Test
    void testGetGroupById_NotFound() {
        when(groupRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> groupService.getGroupById(99L));
    }
    
    @Test
    void testJoinGroup_Success() {
        HttpServletRequest request = requestWith("10", "ROLE_LEARNER");

        Group group = new Group();
        ReflectionTestUtils.setField(group, "id", 1L);

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(groupMemberRepository.existsByGroupIdAndUserId(1L, 10L)).thenReturn(false);

        groupService.joinGroup(1L, request);

        verify(groupMemberRepository).save(any(GroupMember.class));
    }
    
    @Test
    void testJoinGroup_GroupNotFound() {
        HttpServletRequest request = requestWith("10", "ROLE_LEARNER");

        when(groupRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> groupService.joinGroup(1L, request));
    }
    
    @Test
    void testLeaveGroup_Success() {
        HttpServletRequest request = requestWith("10", "ROLE_LEARNER");

        GroupMember member = new GroupMember();
        when(groupMemberRepository.findByGroupIdAndUserId(1L, 10L))
                .thenReturn(Optional.of(member));

        groupService.leaveGroup(1L, request);

        verify(groupMemberRepository).delete(member);
    }
    
    @Test
    void testGetGroupById_Success() {
        Group group = new Group("Spring", "desc", 10L, LocalDateTime.now());
        ReflectionTestUtils.setField(group, "id", 1L);

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        GroupResponseDTO response = groupService.getGroupById(1L);

        assertEquals("Spring", response.getName());
    }
    
    @Test
    void testGetAllGroups_Success() {
        Group group = new Group("Spring", "desc", 10L, LocalDateTime.now());
        ReflectionTestUtils.setField(group, "id", 1L);

        when(groupRepository.findAll()).thenReturn(java.util.List.of(group));

        var result = groupService.getAllGroups();

        assertEquals(1, result.size());
    }
    

    private HttpServletRequest requestWith(String userId, String role) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        lenient().when(request.getHeader("X-User-Id")).thenReturn(userId);
        lenient().when(request.getHeader("X-User-Role")).thenReturn(role);
        return request;
    }
}
