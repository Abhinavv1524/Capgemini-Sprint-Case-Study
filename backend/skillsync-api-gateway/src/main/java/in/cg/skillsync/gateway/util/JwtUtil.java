package in.cg.skillsync.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
	@Value("${jwt.secret}")
	private String secret;

	public Claims extractClaims(String token) {
	    return Jwts.parserBuilder()
	            .setSigningKey(secret.getBytes())
	            .build()
	            .parseClaimsJws(token)
	            .getBody();
	}

    public String extractUserId(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}