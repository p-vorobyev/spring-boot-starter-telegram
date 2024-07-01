package dev.voroby.springframework.telegram.client.updates;

import java.util.concurrent.atomic.AtomicBoolean;

final class AuthorizationStateCache {

    static final AtomicBoolean haveAuthorization = new AtomicBoolean();

    static final AtomicBoolean waitAuthenticationCode = new AtomicBoolean();

    static final AtomicBoolean waitAuthenticationPassword = new AtomicBoolean();

    static final AtomicBoolean waitEmailAddress = new AtomicBoolean();

    static final AtomicBoolean stateClosed = new AtomicBoolean();

    /*
     * codeInputToCheck/passwordInputToCheck/emailAddressInputToCheck will be cleaned up after check
     */
    static volatile String codeInputToCheck;

    static volatile String passwordInputToCheck;

    static volatile String emailAddressInputToCheck;

    private AuthorizationStateCache() {
    }
}
