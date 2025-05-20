package dev.ilya_anna.user_service.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.ilya_anna.user_service.entities.User;
import dev.ilya_anna.user_service.exceptions.JwtAuthenticationException;
import dev.ilya_anna.user_service.exceptions.UserNotFoundException;
import dev.ilya_anna.user_service.repositories.UserRepository;
import dev.ilya_anna.user_service.services.BlacklistService;
import dev.ilya_anna.user_service.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private BlacklistService blacklistService;
    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        try{
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwtToken = authHeader.substring(7);

                DecodedJWT decodedJWT = jwtService.verifyAccessToken(jwtToken);
                String userId = decodedJWT.getClaim("userId").asString();

                User user = userRepository.findById(userId).orElseThrow(
                        () -> new UserNotFoundException("User with id " + userId + " not found")
                );

                if (blacklistService.isInBlacklist(userId)) {
                    throw new JwtAuthenticationException("Token is blacklisted");
                }

                DaoUserDetails daoUserDetails = new DaoUserDetails(user);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                daoUserDetails,
                                null,
                                daoUserDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        }
        catch (JWTVerificationException e) {
            response.getWriter().write("Invalid authorization JWT");
            response.setStatus(HttpStatus.FORBIDDEN.value());
        } catch (UserNotFoundException e) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
        } catch (JwtAuthenticationException e) {
            response.getWriter().write("JWT is in blacklist");
            response.setStatus(HttpStatus.FORBIDDEN.value());
        }
    }
}
