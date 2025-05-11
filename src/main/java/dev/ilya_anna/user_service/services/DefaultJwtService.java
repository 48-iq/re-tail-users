package dev.ilya_anna.user_service.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class DefaultJwtService implements JwtService{

    @Value("${app.jwt.issuer}")
    private String issuer;

    @Value("${app.jwt.subject}")
    private String subject;

    @Value("${app.jwt.access.secret}")
    private String accessSecret;

    @Override
    public DecodedJWT verifyAccessToken(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(accessSecret))
                .withSubject(subject)
                .withIssuer(issuer)
                .withClaimPresence("userId")
                .withClaimPresence("roles")
                .withClaimPresence("username")
                .build();

        return verifier.verify(token);
    }
}
