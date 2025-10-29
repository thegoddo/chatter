package com.project.Chatter.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtils {

    // Inject the secret key from application.properties
    @Value(("${jwt.secret}"))
    private String jwtSecret;

    /**
     * Generates a new JWT token for a given username (subject).
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                // Token validity: 10 hours
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                // Using the corrected signing method
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }


    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validates the token against the user's email and checks for expiration.
     * Includes error handling for internal parsing failure.
     */
    public Boolean validateToken(String token, String userEmail) {
        // We wrap the entire validation in a try-catch to prevent a crash from bad signature/malformed token.
        try {
            final String email = extractEmail(token);

            // This is the core validation logic: username matches AND token is not expired.
            boolean isValid = (email.equals(userEmail) && !isTokenExpired(token));

            if (!isValid) {
                if (!email.equals(userEmail)) {
                    System.err.println("VALIDATION FAILED: Extracted email does not match user details email.");
                }
                if (isTokenExpired(token)) {
                    System.err.println("VALIDATION FAILED: Token is expired.");
                }
            }

            return isValid;
        } catch (Exception e) {
            // This catches signature, expiration, or other parsing errors (e.g., if the token is invalid JSON)
            System.err.println("ERROR: JWT Validation failed (Internal parsing error). Token rejected. Message: " + e.getMessage());
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Converts the Base64 secret string into a cryptographic Key object.
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}