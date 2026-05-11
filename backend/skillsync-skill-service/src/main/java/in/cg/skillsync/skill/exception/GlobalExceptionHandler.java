package in.cg.skillsync.skill.exception;

import in.cg.skillsync.common.dto.ResponseDTO;
import in.cg.skillsync.common.exception.BadRequestException;
import in.cg.skillsync.common.exception.ResourceNotFoundException;
import in.cg.skillsync.common.exception.UnauthorizedException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseDTO<Object>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(404)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ResponseDTO<Object>> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(400)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ResponseDTO<Object>> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(403)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDTO<Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Validation failed")
                .orElse("Validation failed");

        return ResponseEntity.status(400)
                .body(new ResponseDTO<>(false, message, null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO<Object>> handleGeneral(Exception ex) {
        return ResponseEntity.status(500)
                .body(new ResponseDTO<>(false, "Something went wrong", null));
    }
}
