package dev.ilya_anna.user_service.authorizers;

import dev.ilya_anna.user_service.security.DaoUserDetails;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class DaoUserAuthorizer implements AuthorizationManager<RequestAuthorizationContext> {


    @Override
    public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier,
                                       RequestAuthorizationContext authorizationContext) {
        Authentication authentication = authenticationSupplier.get();
        DaoUserDetails userDetails = (DaoUserDetails) authentication.getPrincipal();
        String userId = authorizationContext.getVariables().get("userId");
        String requestSenderId = userDetails.getUser().getId();
        return new AuthorizationDecision(userId.equals(requestSenderId));
    }
}
