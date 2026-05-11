package in.cg.skillsync.user.controller;

import in.cg.skillsync.common.dto.ResponseDTO;
import in.cg.skillsync.common.exception.UnauthorizedException;
import in.cg.skillsync.user.dto.UserRequestDTO;
import in.cg.skillsync.user.dto.UserResponseDTO;
import in.cg.skillsync.user.service.UserService;
import in.cg.skillsync.user.util.SecurityUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // CREATE USER
    @PostMapping
    public ResponseEntity<ResponseDTO<UserResponseDTO>> createUser(
            HttpServletRequest servletRequest,
            @Valid @RequestBody UserRequestDTO requestDTO) {

        Long authUserId = getRequiredUserId(servletRequest);
        String role = getRequiredRole(servletRequest);
        UserResponseDTO response = userService.createUser(authUserId, role, requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>(true, "User created successfully", response));
    }

    // GET USER BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<UserResponseDTO>> getUserById(@PathVariable Long id) {

        UserResponseDTO response = userService.getUserById(id);

        return ResponseEntity.ok(
                new ResponseDTO<>(true, "User fetched successfully", response)
        );
    }
    
    @GetMapping("/auth/{authUserId}")
    public ResponseEntity<ResponseDTO<UserResponseDTO>> getUserByAuthUserId(@PathVariable Long authUserId) {

        UserResponseDTO response = userService.getUserByAuthUserId(authUserId);

        return ResponseEntity.ok(
                new ResponseDTO<>(true, "User fetched successfully", response)
        );
    }

    // GET ALL USERS
    @GetMapping
    public ResponseEntity<ResponseDTO<List<UserResponseDTO>>> getAllUsers() {

        List<UserResponseDTO> users = userService.getAllUsers();

        return ResponseEntity.ok(
                new ResponseDTO<>(true, "Users fetched successfully", users)
        );
    }

    // UPDATE USER
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<UserResponseDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDTO requestDTO) {

        UserResponseDTO response = userService.updateUser(id, requestDTO);

        return ResponseEntity.ok(
                new ResponseDTO<>(true, "User updated successfully", response)
        );
    }

    private Long getRequiredUserId(HttpServletRequest request) {
        String userId = SecurityUtil.getCurrentUserId(request);
        try {
            return Long.parseLong(userId);
        } catch (Exception ex) {
            throw new UnauthorizedException("Invalid user context");
        }
    }

    private String getRequiredRole(HttpServletRequest request) {
        String role = SecurityUtil.getCurrentUserRole(request);
        if (role == null || role.isBlank()) {
            throw new UnauthorizedException("Invalid role context");
        }
        return role;
    }
}
