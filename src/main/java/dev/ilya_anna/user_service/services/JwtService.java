package dev.ilya_anna.user_service.services;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.ilya_anna.user_service.entities.User;

public interface JwtService {
    String generateAccess(User user);
    DecodedJWT verifyAccessToken(String token) throws JWTVerificationException;
}
