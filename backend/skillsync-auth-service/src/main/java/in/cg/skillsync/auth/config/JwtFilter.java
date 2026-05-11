package in.cg.skillsync.auth.config;

import in.cg.skillsync.auth.entity.User;
import in.cg.skillsync.auth.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        String token = null;
        String email = null;

//        System.out.println("JWT Filter Triggered");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
//            System.out.println("Token: " + token);

            try {
                email = jwtUtil.extractEmail(token);
//                System.out.println("Email: " + email);
            } catch (Exception e) {
//                System.out.println("JWT Error: " + e.getMessage());
            }
        }
        
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            User user = userRepository.findByEmail(email).orElse(null);

            if (user != null) {
            	List<SimpleGrantedAuthority> authorities = user.getRoles()
            	        .stream()
            	        .map(role -> new SimpleGrantedAuthority(role.getName()))
            	        .collect(Collectors.toList());

            	UsernamePasswordAuthenticationToken authToken =
            	        new UsernamePasswordAuthenticationToken(
            	                user.getEmail(),
            	                null,
            	                authorities
            	        );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
//        System.out.println("Setting authentication for user: " + email);
        filterChain.doFilter(request, response);
    }
}