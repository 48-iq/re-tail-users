package dev.ilya_anna.user_service.services;

import dev.ilya_anna.user_service.events.UserSignOutEvent;

public interface BlacklistService {
    boolean isInBlacklist(String userId);
    void addToBlacklist(UserSignOutEvent userSignOutEvent);
}
