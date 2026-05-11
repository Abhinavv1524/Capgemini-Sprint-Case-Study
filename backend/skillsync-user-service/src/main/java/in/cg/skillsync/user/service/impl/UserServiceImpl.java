package in.cg.skillsync.user.service.impl;

import in.cg.skillsync.common.exception.BadRequestException;
import in.cg.skillsync.common.exception.ResourceNotFoundException;
import in.cg.skillsync.user.dto.UserRequestDTO;
import in.cg.skillsync.user.dto.UserResponseDTO;
import in.cg.skillsync.user.entity.User;
import in.cg.skillsync.user.repository.UserRepository;
import in.cg.skillsync.user.service.UserService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    // CREATE USER
    @Override
    @Caching(
            put = {
                    @CachePut(value = "usersById", key = "#result.id"),
                    @CachePut(value = "usersByAuthId", key = "#authUserId")
            },
            evict = @CacheEvict(value = "allUsers", allEntries = true)
    )
    public UserResponseDTO createUser(Long authUserId, String role, UserRequestDTO dto) {

        if (userRepository.findByAuthUserId(authUserId).isPresent()) {
            throw new BadRequestException("User already exists");
        }

        User user = new User();

        user.setAuthUserId(authUserId);
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setBio(dto.getBio());
        user.setSkills(dto.getSkills());

        // ✅ Role comes from trusted header (Gateway/JWT)
        user.setRole(role);

        User savedUser = userRepository.save(user);

        return mapToResponseDTO(savedUser);
    }

    // GET USER BY ID
    @Override
    @Cacheable(value = "usersById", key = "#id")
    public UserResponseDTO getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + id));

        return mapToResponseDTO(user);
    }
    
    @Override
    @Cacheable(value = "usersByAuthId", key = "#authUserId")
    public UserResponseDTO getUserByAuthUserId(Long authUserId) {
        User user = userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with auth user id: " + authUserId));

        return mapToResponseDTO(user);
    }

    // GET ALL USERS
    @Override
    @Cacheable(value = "allUsers")
    public List<UserResponseDTO> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // UPDATE USER (NO ROLE UPDATE)
    @Override
    @Caching(
            put = {
                    @CachePut(value = "usersById", key = "#result.id"),
                    @CachePut(value = "usersByAuthId", key = "#result.authUserId")
            },
            evict = @CacheEvict(value = "allUsers", allEntries = true)
    )
    public UserResponseDTO updateUser(Long id, UserRequestDTO dto) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + id));

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setBio(dto.getBio());
        user.setSkills(dto.getSkills());

        // ❌ DO NOT update role here (security rule)

        User updatedUser = userRepository.save(user);

        return mapToResponseDTO(updatedUser);
    }

    // MAPPING METHOD
    private UserResponseDTO mapToResponseDTO(User user) {

        UserResponseDTO dto = new UserResponseDTO();

        dto.setId(user.getId());
        dto.setAuthUserId(user.getAuthUserId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setBio(user.getBio());
        dto.setSkills(user.getSkills());
        dto.setRole(user.getRole());

        return dto;
    }
}
