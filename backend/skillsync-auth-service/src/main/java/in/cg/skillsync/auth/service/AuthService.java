package in.cg.skillsync.auth.service;

import in.cg.skillsync.auth.dto.LoginRequest;
import in.cg.skillsync.auth.dto.RegisterRequest;
import in.cg.skillsync.common.dto.ResponseDTO;

public interface AuthService {

    ResponseDTO<?> register(RegisterRequest request);

    ResponseDTO<String> login(LoginRequest request);

    ResponseDTO<?> promoteUserToMentor(Long userId);
}
