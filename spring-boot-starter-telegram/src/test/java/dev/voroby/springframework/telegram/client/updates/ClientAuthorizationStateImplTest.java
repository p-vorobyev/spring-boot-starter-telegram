package dev.voroby.springframework.telegram.client.updates;

import dev.voroby.springframework.telegram.AbstractTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class ClientAuthorizationStateImplTest extends AbstractTest {

    @Autowired
    private ClientAuthorizationState clientAuthorizationState;

    @Test
    void checkAuthenticationCode() {
        try {
            //setup flag that client waits authentication code
            ((ClientAuthorizationStateImpl) clientAuthorizationState).setWaitAuthenticationCode();
            assertTrue(clientAuthorizationState.isWaitAuthenticationCode());

            //check code
            var code = "code";
            clientAuthorizationState.checkAuthenticationCode(code);

            //code accepted
            assertFalse(clientAuthorizationState.isWaitAuthenticationCode());
            assertEquals(code, ((ClientAuthorizationStateImpl) clientAuthorizationState).getCode());
        } finally {
            ((ClientAuthorizationStateImpl) clientAuthorizationState).clearCode();
        }
    }

    @Test
    void checkAuthenticationPassword() {
        try {
            //setup flag that client waits authentication password
            ((ClientAuthorizationStateImpl) clientAuthorizationState).setWaitAuthenticationPassword();
            assertTrue(clientAuthorizationState.isWaitAuthenticationPassword());

            //check password
            var password = "password";
            clientAuthorizationState.checkAuthenticationPassword(password);

            //password accepted
            assertFalse(clientAuthorizationState.isWaitAuthenticationPassword());
            assertEquals(password, ((ClientAuthorizationStateImpl) clientAuthorizationState).getPassword());
        } finally {
            ((ClientAuthorizationStateImpl) clientAuthorizationState).clearPassword();
        }
    }

    @Test
    void checkEmailAddress() {
        try {
            //setup flag that client waits authentication email
            ((ClientAuthorizationStateImpl) clientAuthorizationState).setWaitEmailAddress();
            assertTrue(clientAuthorizationState.isWaitEmailAddress());

            //check email
            var email = "some_email";
            clientAuthorizationState.checkEmailAddress(email);

            //email accepted
            assertFalse(clientAuthorizationState.isWaitEmailAddress());
            assertEquals(email, ((ClientAuthorizationStateImpl) clientAuthorizationState).getEmailAddress());
        } finally {
            ((ClientAuthorizationStateImpl) clientAuthorizationState).clearCode();
        }
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