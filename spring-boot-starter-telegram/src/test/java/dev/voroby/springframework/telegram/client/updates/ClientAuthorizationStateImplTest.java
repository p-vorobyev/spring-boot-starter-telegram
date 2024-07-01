package dev.voroby.springframework.telegram.client.updates;

import dev.voroby.springframework.telegram.AbstractTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static dev.voroby.springframework.telegram.client.updates.AuthorizationStateCache.*;
import static org.junit.jupiter.api.Assertions.*;

class ClientAuthorizationStateImplTest extends AbstractTest {

    @Autowired
    private ClientAuthorizationState clientAuthorizationState;

    @BeforeEach
    void clearCacheValues() {
        codeInputToCheck = null;
        passwordInputToCheck = null;
        emailAddressInputToCheck = null;
        waitAuthenticationCode.set(false);
        waitAuthenticationPassword.set(false);
        waitEmailAddress.set(false);
        haveAuthorization.set(false);
        stateClosed.set(false);
    }

    @Test
    void checkAuthenticationCode() {
        //setup flag that client waits authentication code
        waitAuthenticationCode.set(true);
        assertTrue(clientAuthorizationState.isWaitAuthenticationCode());

        //check code
        var code = "code";
        clientAuthorizationState.checkAuthenticationCode(code);

        //code accepted
        assertFalse(clientAuthorizationState.isWaitAuthenticationCode());
        assertEquals(code, AuthorizationStateCache.codeInputToCheck);
    }

    @Test
    void checkAuthenticationPassword() {
        //setup flag that client waits authentication password
        waitAuthenticationPassword.set(true);
        assertTrue(clientAuthorizationState.isWaitAuthenticationPassword());

        //check password
        var password = "password";
        clientAuthorizationState.checkAuthenticationPassword(password);

        //password accepted
        assertFalse(clientAuthorizationState.isWaitAuthenticationPassword());
        assertEquals(password, AuthorizationStateCache.passwordInputToCheck);
    }

    @Test
    void checkEmailAddress() {
        //setup flag that client waits authentication email
        waitEmailAddress.set(true);
        assertTrue(clientAuthorizationState.isWaitEmailAddress());

        //check email
        var email = "some_email";
        clientAuthorizationState.checkEmailAddress(email);

        //email accepted
        assertFalse(clientAuthorizationState.isWaitEmailAddress());
        assertEquals(email, emailAddressInputToCheck);
    }

    @Test
    void checkDefaults() {
        assertFalse(clientAuthorizationState.haveAuthorization());
        assertFalse(clientAuthorizationState.isWaitAuthenticationCode());
        assertFalse(clientAuthorizationState.isWaitAuthenticationPassword());
        assertFalse(clientAuthorizationState.isWaitEmailAddress());
        assertFalse(clientAuthorizationState.isStateClosed());
    }

}