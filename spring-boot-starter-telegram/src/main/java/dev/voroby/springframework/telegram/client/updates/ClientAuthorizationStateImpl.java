package dev.voroby.springframework.telegram.client.updates;

import static dev.voroby.springframework.telegram.client.updates.AuthorizationStateCache.*;
import static org.springframework.util.StringUtils.hasText;

/**
 * Implementation of {@link ClientAuthorizationState}.
 *
 * @author Pavel Vorobyev
 */
public final class ClientAuthorizationStateImpl implements ClientAuthorizationState {

    @Override
    public synchronized void checkAuthenticationCode(String code) {
        if (waitAuthenticationCode.get()) {
            if (hasText(code)) {
                AuthorizationStateCache.codeInputToCheck = code;
                waitAuthenticationCode.set(false);
            }
        }
    }

    @Override
    public synchronized void checkAuthenticationPassword(String password) {
        if (waitAuthenticationPassword.get()) {
            if (hasText(password)) {
                AuthorizationStateCache.passwordInputToCheck = password;
                waitAuthenticationPassword.set(false);
            }
        }
    }

    @Override
    public synchronized void checkEmailAddress(String email) {
        if (waitEmailAddress.get()) {
            if (hasText(email)) {
                AuthorizationStateCache.emailAddressInputToCheck = email;
                waitEmailAddress.set(false);
            }
        }
    }

    @Override
    public boolean isWaitAuthenticationCode() {
        return waitAuthenticationCode.get();
    }

    @Override
    public boolean isWaitAuthenticationPassword() {
        return waitAuthenticationPassword.get();
    }

    @Override
    public boolean isWaitEmailAddress() {
        return waitEmailAddress.get();
    }

    @Override
    public boolean haveAuthorization() {
        return haveAuthorization.get();
    }

    @Override
    public boolean isStateClosed() {
        return stateClosed.get();
    }

}
