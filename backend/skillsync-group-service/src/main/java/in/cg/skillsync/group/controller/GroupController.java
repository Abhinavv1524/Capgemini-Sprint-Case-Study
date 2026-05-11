package in.cg.skillsync.group.controller;

import in.cg.skillsync.common.dto.ResponseDTO;
import in.cg.skillsync.group.dto.CreateGroupRequestDTO;
import in.cg.skillsync.group.dto.GroupResponseDTO;
import in.cg.skillsync.group.service.GroupService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    // Create Group
    @PostMapping
    public ResponseEntity<ResponseDTO<GroupResponseDTO>> createGroup(
            @Valid @RequestBody CreateGroupRequestDTO request,
            HttpServletRequest servletRequest) {

        GroupResponseDTO response = groupService.createGroup(request, servletRequest);

        return ResponseEntity.ok(
                new ResponseDTO<>(true, "Group created successfully", response)
        );
    }

    // Join Group
    @PostMapping("/{id}/join")
    public ResponseEntity<ResponseDTO<Void>> joinGroup(
            @PathVariable Long id,
            HttpServletRequest servletRequest) {

        groupService.joinGroup(id, servletRequest);

        return ResponseEntity.ok(
                new ResponseDTO<>(true, "Joined group successfully", null)
        );
    }

    // ✅ Leave Group
    @PostMapping("/{id}/leave")
    public ResponseEntity<ResponseDTO<Void>> leaveGroup(
            @PathVariable Long id,
            HttpServletRequest servletRequest) {

        groupService.leaveGroup(id, servletRequest);

        return ResponseEntity.ok(
                new ResponseDTO<>(true, "Left group successfully", null)
        );
    }

    // Get All Groups
    @GetMapping
    public ResponseEntity<ResponseDTO<List<GroupResponseDTO>>> getAllGroups() {

        List<GroupResponseDTO> groups = groupService.getAllGroups();

        return ResponseEntity.ok(
                new ResponseDTO<>(true, "Groups fetched successfully", groups)
        );
    }

    // Get Group By ID
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<GroupResponseDTO>> getGroupById(
            @PathVariable Long id) {

        GroupResponseDTO group = groupService.getGroupById(id);

        return ResponseEntity.ok(
                new ResponseDTO<>(true, "Group fetched successfully", group)
        );
    }
}
