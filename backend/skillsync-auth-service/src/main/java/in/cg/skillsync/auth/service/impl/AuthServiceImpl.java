package in.cg.skillsync.auth.service.impl;

import in.cg.skillsync.auth.config.JwtUtil;
import in.cg.skillsync.auth.client.UserClient;
import in.cg.skillsync.auth.dto.LoginRequest;
import in.cg.skillsync.auth.dto.RegisterRequest;
import in.cg.skillsync.auth.dto.UserProfileSyncRequest;
import in.cg.skillsync.auth.entity.Role;
import in.cg.skillsync.auth.entity.User;
import in.cg.skillsync.auth.repository.RoleRepository;
import in.cg.skillsync.auth.repository.UserRepository;
import in.cg.skillsync.auth.service.AuthService;
import in.cg.skillsync.common.dto.ResponseDTO;
import in.cg.skillsync.common.exception.BadRequestException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserClient userClient;

    @Override
    @Transactional
    public ResponseDTO<?> register(RegisterRequest request) {

        // Check if user exists
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new BadRequestException("Email already exists");
        }

        // Create user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        String roleName = request.getRole();

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new BadRequestException("Role not found"));

        user.getRoles().add(role);

        User savedUser = userRepository.save(user);

        try {
            ResponseDTO<?> profileResponse = userClient.createUserProfile(
                    savedUser.getId(),
                    roleName,
                    new UserProfileSyncRequest(
                            savedUser.getName(),
                            savedUser.getEmail(),
                            null,
                            null
                    )
            );
            if (profileResponse == null || !profileResponse.isSuccess()) {
                throw new BadRequestException("User profile sync response was unsuccessful");
            }
        } catch (Exception ex) {
            throw new BadRequestException("Registration failed: User profile sync failed");
        }

        return new ResponseDTO<>(true, "User registered successfully", null);
    }

    @Override
    public ResponseDTO<String> login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }
        
        String role = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .orElse("ROLE_LEARNER");

        String token = jwtUtil.generateToken(
                user.getId(),
                role,
                user.getEmail()
        );

        return new ResponseDTO<>(true, "Login successful", token);
    }

    @Override
    @Transactional
    public ResponseDTO<?> promoteUserToMentor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        Role mentorRole = roleRepository.findByName("ROLE_MENTOR")
                .orElseThrow(() -> new BadRequestException("Role not found"));

        Set<Role> updatedRoles = new HashSet<>();
        updatedRoles.add(mentorRole);
        user.setRoles(updatedRoles);
        userRepository.save(user);

        return new ResponseDTO<>(true, "User promoted to mentor successfully", null);
    }
}
