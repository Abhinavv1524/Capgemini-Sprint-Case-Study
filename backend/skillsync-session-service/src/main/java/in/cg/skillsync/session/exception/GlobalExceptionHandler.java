package in.cg.skillsync.session.exception;

import in.cg.skillsync.common.dto.ResponseDTO;
import in.cg.skillsync.common.exception.BadRequestException;
import in.cg.skillsync.common.exception.ResourceNotFoundException;
import in.cg.skillsync.common.exception.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseDTO<Object>> handleNotFound(ResourceNotFoundException ex) {
        return new ResponseEntity<>(
                new ResponseDTO<>(false, ex.getMessage(), null),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ResponseDTO<Object>> handleBadRequest(BadRequestException ex) {
        return new ResponseEntity<>(
                new ResponseDTO<>(false, ex.getMessage(), null),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ResponseDTO<Object>> handleUnauthorized(UnauthorizedException ex) {
        return new ResponseEntity<>(
                new ResponseDTO<>(false, ex.getMessage(), null),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDTO<Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Validation failed")
                .orElse("Validation failed");

        return new ResponseEntity<>(
                new ResponseDTO<>(false, message, null),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO<Object>> handleGeneric(Exception ex) {
        return new ResponseEntity<>(
                new ResponseDTO<>(false, "Something went wrong", null),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
