package in.cg.skillsync.session.service.impl;

import in.cg.skillsync.common.dto.ResponseDTO;
import in.cg.skillsync.session.client.MentorClient;
import in.cg.skillsync.common.exception.BadRequestException;
import in.cg.skillsync.common.exception.ResourceNotFoundException;
import in.cg.skillsync.common.exception.UnauthorizedException;
import in.cg.skillsync.session.client.UserClient;
import in.cg.skillsync.session.dto.MentorDTO;
import in.cg.skillsync.session.dto.SessionRequestDTO;
import in.cg.skillsync.session.dto.SessionResponseDTO;
import in.cg.skillsync.session.dto.UserDTO;
import in.cg.skillsync.session.entity.Session;
import in.cg.skillsync.session.enums.SessionStatus;
import in.cg.skillsync.session.event.SessionEvent;
import in.cg.skillsync.session.event.SessionEventPublisher;
import in.cg.skillsync.session.repository.SessionRepository;
import in.cg.skillsync.session.service.SessionService;
import in.cg.skillsync.session.util.SecurityUtil;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SessionServiceImpl implements SessionService {

	@Autowired
    private SessionRepository sessionRepository;
    
    @Autowired
    private SessionEventPublisher eventPublisher;
    
    @Autowired
    private UserClient userClient;

    @Autowired
    private MentorClient mentorClient;
    
    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "createSessionFallback")
    @Caching(
            put = @CachePut(value = "sessionsById", key = "#result.id"),
            evict = {
                    @CacheEvict(value = "sessionsByUserRole", allEntries = true)
            }
    )
    public SessionResponseDTO createSession(SessionRequestDTO request, HttpServletRequest servletRequest) {
        requireRole(servletRequest, "ROLE_LEARNER", "Only learners can create sessions");
        Long learnerId = getRequiredUserId(servletRequest);
    	
    	// Validate learner (from header userId)
    	ResponseDTO<UserDTO> learnerResponse;

    	try {
    	    learnerResponse = userClient.getUserByAuthUserId(learnerId);
    	} catch (Exception e) {
    	    throw new BadRequestException("Learner not found or User Service unavailable");
    	}

    	UserDTO learner = learnerResponse.getData();

    	if (learner == null) {
    	    throw new BadRequestException("Learner not found");
    	}

        Long mentorUserId = resolveMentorUserId(request.getMentorId());
        if (mentorUserId.equals(learnerId)) {
            throw new BadRequestException("Learner cannot create a session with self");
        }

        Session session = new Session(
                mentorUserId,
                learnerId,
                request.getSessionTime(),
                SessionStatus.REQUESTED
        );
        
        Session savedSession = sessionRepository.save(session);

        SessionEvent event = new SessionEvent(
                "SESSION_BOOKED",
                savedSession.getId(),
                savedSession.getMentorId(),
                savedSession.getLearnerId(),
                savedSession.getSessionTime()
        );

        try {
            eventPublisher.publish(event);
        } catch (Exception e) {
            System.out.println("RabbitMQ not available, skipping event...");
        }

        return mapToDTO(savedSession);
    }

    @Override
    @Caching(
            put = @CachePut(value = "sessionsById", key = "#result.id"),
            evict = @CacheEvict(value = "sessionsByUserRole", allEntries = true)
    )
    public SessionResponseDTO acceptSession(Long sessionId, HttpServletRequest servletRequest) {
        Long mentorId = getRequiredUserId(servletRequest);
        requireMentorActionAccess(servletRequest, mentorId, "Only mentors can accept sessions");

        Session session = getSessionOrThrow(sessionId);

        // Ownership check
        if (!session.getMentorId().equals(mentorId)) {
        	throw new BadRequestException("You are not allowed to perform this action");
        }

        // State validation
        if (session.getStatus() != SessionStatus.REQUESTED) {
        	throw new BadRequestException("Invalid session state transition");
        }

        session.setStatus(SessionStatus.ACCEPTED);
        
        SessionEvent event = new SessionEvent(
                "SESSION_ACCEPTED",
                session.getId(),
                session.getMentorId(),
                session.getLearnerId(),
                session.getSessionTime()
        );

        eventPublisher.publish(event);

        return mapToDTO(sessionRepository.save(session));
    }

    @Override
    @Caching(
            put = @CachePut(value = "sessionsById", key = "#result.id"),
            evict = @CacheEvict(value = "sessionsByUserRole", allEntries = true)
    )
    public SessionResponseDTO rejectSession(Long sessionId, HttpServletRequest servletRequest) {
        Long mentorId = getRequiredUserId(servletRequest);
        requireMentorActionAccess(servletRequest, mentorId, "Only mentors can reject sessions");

        Session session = getSessionOrThrow(sessionId);

        if (!session.getMentorId().equals(mentorId)) {
        	throw new BadRequestException("You are not allowed to perform this action");
        }

        if (session.getStatus() != SessionStatus.REQUESTED) {
        	throw new BadRequestException("Invalid session state transition");
        }

        session.setStatus(SessionStatus.REJECTED);

        return mapToDTO(sessionRepository.save(session));
    }

    @Override
    @Caching(
            put = @CachePut(value = "sessionsById", key = "#result.id"),
            evict = @CacheEvict(value = "sessionsByUserRole", allEntries = true)
    )
    public SessionResponseDTO cancelSession(Long sessionId, HttpServletRequest servletRequest) {
        requireRole(servletRequest, "ROLE_LEARNER", "Only learners can cancel sessions");
        Long learnerId = getRequiredUserId(servletRequest);

        Session session = getSessionOrThrow(sessionId);

        if (!session.getLearnerId().equals(learnerId)) {
            throw new BadRequestException("You are not allowed to perform this action");
        }

        if (session.getStatus() == SessionStatus.COMPLETED ||
            session.getStatus() == SessionStatus.CANCELLED ||
            session.getStatus() == SessionStatus.REJECTED) {
        	throw new BadRequestException("Session cannot be cancelled in current state");
        }

        session.setStatus(SessionStatus.CANCELLED);

        return mapToDTO(sessionRepository.save(session));
    }

    @Override
    @Cacheable(value = "sessionsByUserRole", key = "#servletRequest.getHeader('X-User-Role') + ':' + #servletRequest.getHeader('X-User-Id')")
    public List<SessionResponseDTO> getSessionsByUser(HttpServletRequest servletRequest) {
        Long userId = getRequiredUserId(servletRequest);
        String role = SecurityUtil.getCurrentUserRole(servletRequest);

        List<Session> sessions;

        if ("ROLE_MENTOR".equals(role) || hasApprovedMentorProfile(userId)) {
            sessions = sessionRepository.findByMentorId(userId);
        } else if ("ROLE_LEARNER".equals(role)) {
            sessions = sessionRepository.findByLearnerId(userId);
        } else {
         	throw new UnauthorizedException("Only learners and mentors can view sessions");
        }

        return sessions.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable(value = "sessionsById", key = "#id")
    public SessionResponseDTO getSessionById(Long id) {

        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        return mapToDTO(session);
    }

    private Session getSessionOrThrow(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));
    }

    private SessionResponseDTO mapToDTO(Session session) {
        return new SessionResponseDTO(
                session.getId(),
                session.getMentorId(),
                session.getLearnerId(),
                session.getSessionTime(),
                session.getStatus(),
                session.getCreatedAt()
        );
    }

    private void requireRole(HttpServletRequest request, String expectedRole, String message) {
        String role = SecurityUtil.getCurrentUserRole(request);
        if (!expectedRole.equals(role)) {
            throw new UnauthorizedException(message);
        }
    }

    private Long getRequiredUserId(HttpServletRequest request) {
        String userId = SecurityUtil.getCurrentUserId(request);
        try {
            return Long.parseLong(userId);
        } catch (Exception ex) {
            throw new UnauthorizedException("Invalid user context");
        }
    }

    @SuppressWarnings("unused")
    private SessionResponseDTO createSessionFallback(SessionRequestDTO request,
                                                     HttpServletRequest servletRequest,
                                                     Throwable throwable) {
        if (throwable instanceof BadRequestException badRequestException) {
            throw badRequestException;
        }
        if (throwable instanceof UnauthorizedException unauthorizedException) {
            throw unauthorizedException;
        }
        throw new BadRequestException("User service is temporarily unavailable. Please try again.");
    }

    private Long resolveMentorUserId(Long mentorIdentifier) {
        try {
            ResponseDTO<MentorDTO> mentorResponse = mentorClient.getMentorById(mentorIdentifier);
            MentorDTO mentorProfile = mentorResponse != null ? mentorResponse.getData() : null;

            if (mentorProfile != null) {
                if (!"APPROVED".equalsIgnoreCase(mentorProfile.getStatus())) {
                    throw new BadRequestException("Mentor profile is not approved yet");
                }
                if (mentorProfile.getUserId() == null) {
                    throw new BadRequestException("Invalid mentor profile data");
                }
                return mentorProfile.getUserId();
            }
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ignored) {
            // Fallback to old behavior where mentorId is treated as auth user id.
        }

        try {
            ResponseDTO<MentorDTO> mentorByUserResponse = mentorClient.getMentorByUserId(mentorIdentifier);
            MentorDTO mentorProfile = mentorByUserResponse != null ? mentorByUserResponse.getData() : null;
            if (mentorProfile != null) {
                if (!"APPROVED".equalsIgnoreCase(mentorProfile.getStatus())) {
                    throw new BadRequestException("Mentor profile is not approved yet");
                }
                return mentorIdentifier;
            }
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ignored) {
            // Continue legacy fallback validation.
        }

        ResponseDTO<UserDTO> mentorResponse;
        try {
            mentorResponse = userClient.getUserByAuthUserId(mentorIdentifier);
        } catch (Exception e) {
            throw new BadRequestException("Mentor not found. Use a valid approved mentor profile id.");
        }

        UserDTO mentor = mentorResponse.getData();
        if (mentor == null || !"ROLE_MENTOR".equals(mentor.getRole())) {
            throw new BadRequestException("Invalid mentor id. Use approved mentor profile id.");
        }

        return mentorIdentifier;
    }

    private void requireMentorActionAccess(HttpServletRequest request, Long userId, String message) {
        String role = SecurityUtil.getCurrentUserRole(request);
        if ("ROLE_MENTOR".equals(role)) {
            return;
        }

        if (hasApprovedMentorProfile(userId)) {
            return;
        }

        throw new UnauthorizedException(message);
    }

    private boolean hasApprovedMentorProfile(Long userId) {
        try {
            ResponseDTO<MentorDTO> mentorResponse = mentorClient.getMentorByUserId(userId);
            MentorDTO mentor = mentorResponse != null ? mentorResponse.getData() : null;
            return mentor != null && "APPROVED".equalsIgnoreCase(mentor.getStatus());
        } catch (Exception ignored) {
            return false;
        }
    }
}
