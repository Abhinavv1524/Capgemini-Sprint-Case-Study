package in.cg.skillsync.group.service;

import in.cg.skillsync.group.dto.CreateGroupRequestDTO;
import in.cg.skillsync.group.dto.GroupResponseDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface GroupService {

    GroupResponseDTO createGroup(CreateGroupRequestDTO request, HttpServletRequest servletRequest);

    void joinGroup(Long groupId, HttpServletRequest servletRequest);

    void leaveGroup(Long groupId, HttpServletRequest servletRequest);

    List<GroupResponseDTO> getAllGroups();

    GroupResponseDTO getGroupById(Long groupId);
}
