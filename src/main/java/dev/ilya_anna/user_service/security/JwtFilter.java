package dev.ilya_anna.user_service.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import dev.ilya_anna.user_service.exceptions.JwtAuthenticationException;
import dev.ilya_anna.user_service.services.JwtBlacklistService;
import dev.ilya_anna.user_service.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final JwtBlacklistService jwtBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwtToken = authHeader.substring(7);

            if (jwtBlacklistService.isTokenBlacklisted(jwtToken)) {
                throw new JwtAuthenticationException("Token is blacklisted");
            }

            try {
                DecodedJWT decodedJWT = jwtService.verifyAccessToken(jwtToken);
                String userId = decodedJWT.getClaim("userId").asString();

                DaoUserDetails daoUserDetails = new DaoUserDetails(userId);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                daoUserDetails,
                                null,
                                daoUserDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                throw new JwtAuthenticationException("Invalid JWT token");
            }
        }

        filterChain.doFilter(request, response);
    }
}
