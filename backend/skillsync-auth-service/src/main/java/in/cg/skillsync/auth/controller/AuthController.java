package in.cg.skillsync.auth.controller;

import in.cg.skillsync.auth.dto.LoginRequest;
import in.cg.skillsync.auth.dto.RegisterRequest;
import in.cg.skillsync.auth.service.AuthService;
import in.cg.skillsync.common.dto.ResponseDTO;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ResponseDTO<?>> register(@Valid @RequestBody RegisterRequest request) {
        ResponseDTO<?> response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<?>> login(@Valid @RequestBody LoginRequest request) {
        ResponseDTO<?> response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/test")
    public String test() {
        return "Protected API working";
    }

    @PutMapping("/users/{userId}/promote-mentor")
    public ResponseEntity<ResponseDTO<?>> promoteUserToMentor(@PathVariable Long userId, Authentication authentication) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));

        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(false, "Only admin can promote user to mentor", null));
        }

        ResponseDTO<?> response = authService.promoteUserToMentor(userId);
        return ResponseEntity.ok(response);
    }
}
