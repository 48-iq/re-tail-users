package dev.ilya_anna.user_service.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import dev.ilya_anna.user_service.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;


@Service
public class DefaultJwtService implements JwtService{

    @Value("${app.jwt.issuer}")
    private String issuer;

    @Value("${app.jwt.subject}")
    private String subject;

    @Value("${app.jwt.access.secret}")
    private String accessSecret;

    @Value("${app.jwt.access.duration}")
    private Long accessDuration;

    @Override
    public DecodedJWT verifyAccessToken(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(accessSecret))
                .withSubject(subject)
                .withIssuer(issuer)
                .withClaimPresence("userId")
                .build();

        return verifier.verify(token);
    }

    @Override
    public String generateAccess(User user) {
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(subject)
                .withExpiresAt(ZonedDateTime.now().plusSeconds(accessDuration).toInstant())
                .withClaim("userId", user.getId())
                .sign(Algorithm.HMAC256(accessSecret));
    }
}
