package in.cg.skillsync.mentor.exception;

import in.cg.skillsync.common.dto.ResponseDTO;
import in.cg.skillsync.common.exception.BadRequestException;
import in.cg.skillsync.common.exception.ResourceNotFoundException;
import in.cg.skillsync.common.exception.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseDTO<String> handleResourceNotFound(ResourceNotFoundException ex) {
        return new ResponseDTO<>(false, ex.getMessage(), null);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<String> handleBadRequest(BadRequestException ex) {
        return new ResponseDTO<>(false, ex.getMessage(), null);
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseDTO<String> handleUnauthorized(UnauthorizedException ex) {
        return new ResponseDTO<>(false, ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<String> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Validation failed")
                .orElse("Validation failed");
        return new ResponseDTO<>(false, message, null);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<String> handleGeneralException(Exception ex) {
        return new ResponseDTO<>(false, "Something went wrong", null);
    }
}
