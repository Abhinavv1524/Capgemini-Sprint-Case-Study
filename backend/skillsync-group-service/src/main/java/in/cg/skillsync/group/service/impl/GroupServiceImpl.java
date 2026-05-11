package in.cg.skillsync.group.service.impl;

import in.cg.skillsync.common.exception.BadRequestException;
import in.cg.skillsync.common.exception.ResourceNotFoundException;
import in.cg.skillsync.common.exception.UnauthorizedException;
import in.cg.skillsync.group.dto.CreateGroupRequestDTO;
import in.cg.skillsync.group.dto.GroupResponseDTO;
import in.cg.skillsync.group.entity.Group;
import in.cg.skillsync.group.entity.GroupMember;
import in.cg.skillsync.group.repository.GroupMemberRepository;
import in.cg.skillsync.group.repository.GroupRepository;
import in.cg.skillsync.group.service.GroupService;
import in.cg.skillsync.group.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    public GroupServiceImpl(GroupRepository groupRepository,
                            GroupMemberRepository groupMemberRepository) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    @Override
    @Caching(
            put = @CachePut(value = "groupsById", key = "#result.id"),
            evict = @CacheEvict(value = "allGroups", allEntries = true)
    )
    public GroupResponseDTO createGroup(CreateGroupRequestDTO request, HttpServletRequest servletRequest) {
        Long userId = getRequiredUserId(servletRequest);

        Group group = new Group(
                request.getName(),
                request.getDescription(),
                userId,
                LocalDateTime.now()
        );

        Group savedGroup = groupRepository.save(group);

        // Add creator as member
        GroupMember member = new GroupMember(
                savedGroup.getId(),
                userId,
                LocalDateTime.now()
        );

        groupMemberRepository.save(member);

        return mapToDTO(savedGroup);
    }

    @Override
    public void joinGroup(Long groupId, HttpServletRequest servletRequest) {
        requireRole(servletRequest, "ROLE_LEARNER", "Only learners can join groups");
        Long userId = getRequiredUserId(servletRequest);

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new BadRequestException("User already joined this group");
        }

        GroupMember member = new GroupMember(
                groupId,
                userId,
                LocalDateTime.now()
        );

        groupMemberRepository.save(member);
    }

    @Override
    public void leaveGroup(Long groupId, HttpServletRequest servletRequest) {
        requireRole(servletRequest, "ROLE_LEARNER", "Only learners can leave groups");
        Long userId = getRequiredUserId(servletRequest);

        GroupMember member = groupMemberRepository
                .findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new BadRequestException("User is not a member of this group"));

        groupMemberRepository.delete(member);
    }

    @Override
    @Cacheable(value = "allGroups")
    public List<GroupResponseDTO> getAllGroups() {

        return groupRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "groupsById", key = "#groupId")
    public GroupResponseDTO getGroupById(Long groupId) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        return mapToDTO(group);
    }

    // Mapper
    private GroupResponseDTO mapToDTO(Group group) {
        return new GroupResponseDTO(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getCreatedBy(),
                group.getCreatedAt()
        );
    }

    private void requireRole(HttpServletRequest request, String expectedRole, String message) {
        String role = SecurityUtil.getCurrentUserRole(request);
        if (!expectedRole.equals(role)) {
            throw new UnauthorizedException(message);
        }
    }

    private Long getRequiredUserId(HttpServletRequest request) {
        String userId = SecurityUtil.getCurrentUserId(request);
        try {
            return Long.parseLong(userId);
        } catch (Exception ex) {
            throw new UnauthorizedException("Invalid user context");
        }
    }
}
