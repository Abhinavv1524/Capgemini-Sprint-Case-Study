package in.cg.skillsync.user.service;

import in.cg.skillsync.user.dto.UserRequestDTO;
import in.cg.skillsync.user.dto.UserResponseDTO;

import java.util.List;

public interface UserService {

    UserResponseDTO getUserById(Long id);
    
    UserResponseDTO getUserByAuthUserId(Long authUserId);

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO updateUser(Long id, UserRequestDTO requestDTO);
    
    UserResponseDTO createUser(Long authUserId, String role, UserRequestDTO dto);
}
