package dev.voroby.springframework.telegram.client.updates;

import dev.voroby.springframework.telegram.AbstractTest;
import dev.voroby.springframework.telegram.client.QueryResultHandler;
import org.drinkless.tdlib.TdApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UpdateAuthorizationStateTest extends AbstractTest {

    @Autowired
    private UpdateNotificationListener<TdApi.UpdateAuthorizationState> updateAuthorizationNotification;

    @Autowired
    private ClientAuthorizationState clientAuthorizationState;

    private final TdApi.UpdateAuthorizationState updateAuthorizationState = new TdApi.UpdateAuthorizationState();

    private static Stream<Arguments> authorizationStates() {
        return Stream.of(
                Arguments.of(new TdApi.AuthorizationStateWaitTdlibParameters()),
                Arguments.of(new TdApi.AuthorizationStateWaitPhoneNumber()),
                Arguments.of(new TdApi.AuthorizationStateWaitOtherDeviceConfirmation()),
                Arguments.of(new TdApi.AuthorizationStateWaitCode()),
                Arguments.of(new TdApi.AuthorizationStateWaitPassword()),
                Arguments.of(new TdApi.AuthorizationStateWaitEmailAddress()),
                Arguments.of(new TdApi.AuthorizationStateWaitEmailCode()),
                Arguments.of(new TdApi.AuthorizationStateReady()),
                Arguments.of(new TdApi.AuthorizationStateLoggingOut()),
                Arguments.of(new TdApi.AuthorizationStateClosing()),
                Arguments.of(new TdApi.AuthorizationStateClosed())
        );
    }

    private final String authCode = "auth_code";
    private final String twoStepPassword = "pass";
    private final String email = "email";

    @MethodSource("authorizationStates")
    @ParameterizedTest
    void handleNotification(TdApi.AuthorizationState authorizationState) {
        updateAuthorizationState.authorizationState = authorizationState;
        switch (authorizationState.getConstructor()) {
            case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR -> {
                updateAuthorizationNotification.handleNotification(updateAuthorizationState);
                handleAuthorizationStateWaitTdlibParameters();
            }
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR -> {
                updateAuthorizationNotification.handleNotification(updateAuthorizationState);
                handleAuthorizationStateWaitPhoneNumber();
            }
            case TdApi.AuthorizationStateWaitOtherDeviceConfirmation.CONSTRUCTOR -> {
                updateAuthorizationNotification.handleNotification(updateAuthorizationState);
                handleAuthorizationStateWaitOtherDeviceConfirmation();
            }
            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR -> {
                AuthorizationStateCache.codeInputToCheck = authCode;
                updateAuthorizationNotification.handleNotification(updateAuthorizationState);
                verifyAuthorizationStateWaitCode();
            }
            case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR -> {
                AuthorizationStateCache.passwordInputToCheck = twoStepPassword;
                updateAuthorizationNotification.handleNotification(updateAuthorizationState);
                verifyAuthorizationStateWaitPassword();
            }
            case TdApi.AuthorizationStateWaitEmailAddress.CONSTRUCTOR -> {
                AuthorizationStateCache.emailAddressInputToCheck = email;
                updateAuthorizationNotification.handleNotification(updateAuthorizationState);
                verifyAuthorizationStateWaitEmailAddress();
            }
            case TdApi.AuthorizationStateWaitEmailCode.CONSTRUCTOR -> {
                AuthorizationStateCache.codeInputToCheck = authCode;
                updateAuthorizationNotification.handleNotification(updateAuthorizationState);
                verifyAuthorizationStateWaitEmailCode();
            }
            case TdApi.AuthorizationStateReady.CONSTRUCTOR -> {
                updateAuthorizationNotification.handleNotification(updateAuthorizationState);
                verifyAuthorizationStateReady();
            }
            case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR, TdApi.AuthorizationStateClosing.CONSTRUCTOR -> {
                updateAuthorizationNotification.handleNotification(updateAuthorizationState);
                verifyAuthorizationStateLoggingOutOrClosing();
            }
            case TdApi.AuthorizationStateClosed.CONSTRUCTOR -> {
                updateAuthorizationNotification.handleNotification(updateAuthorizationState);
                verifyAuthorizationStateClosed();
            }
            default -> throw new RuntimeException("Unknown state");
        }
    }

    @Test
    void notificationType() {
        assertEquals(TdApi.UpdateAuthorizationState.class, updateAuthorizationNotification.notificationType());
    }

    private void handleAuthorizationStateWaitTdlibParameters() {
        ArgumentCaptor<TdApi.SetTdlibParameters> paramsCaptor = ArgumentCaptor.forClass(TdApi.SetTdlibParameters.class);
        verify(telegramClient).sendWithCallback(paramsCaptor.capture(), any(QueryResultHandler.class));
        verify(telegramClient).sendWithCallback(any(TdApi.AddProxy.class), any(QueryResultHandler.class));
        TdApi.SetTdlibParameters tdlibParameters = paramsCaptor.getValue();
        assertEquals(123, tdlibParameters.apiId);
    }

    private void handleAuthorizationStateWaitPhoneNumber() {
        ArgumentCaptor<TdApi.SetAuthenticationPhoneNumber> captor = ArgumentCaptor.forClass(TdApi.SetAuthenticationPhoneNumber.class);
        verify(telegramClient).sendWithCallback(captor.capture(), any(QueryResultHandler.class));
        assertEquals("123456789", captor.getValue().phoneNumber);
    }

    private void handleAuthorizationStateWaitOtherDeviceConfirmation() {
        verifyTelegramClientNotInvoked();
    }

    private void verifyTelegramClientNotInvoked() {
        verify(telegramClient, never()).sendWithCallback(any(TdApi.Function.class), any(QueryResultHandler.class));
        verify(telegramClient, never()).send(any(TdApi.Function.class));
        verify(telegramClient, never()).send(any(TdApi.Function.class));
        verify(telegramClient, never()).sendAsync(any(TdApi.Function.class));
        verify(telegramClient, never()).sendAsync(any(TdApi.Function.class));
    }

    private void verifyAuthorizationStateWaitCode() {
        var authCodeCaptor = ArgumentCaptor.forClass(TdApi.CheckAuthenticationCode.class);
        verify(telegramClient).sendWithCallback(authCodeCaptor.capture(), any(QueryResultHandler.class));
        assertEquals(authCode, authCodeCaptor.getValue().code);
        assertNull(AuthorizationStateCache.codeInputToCheck); // drop from cache after check
    }

    private void verifyAuthorizationStateWaitPassword() {
        var passwordCaptor = ArgumentCaptor.forClass(TdApi.CheckAuthenticationPassword.class);
        verify(telegramClient).sendWithCallback(passwordCaptor.capture(), any(QueryResultHandler.class));
        assertEquals(twoStepPassword, passwordCaptor.getValue().password);
        assertNull(AuthorizationStateCache.passwordInputToCheck); // drop from cache after check
    }

    private void verifyAuthorizationStateWaitEmailAddress() {
        var emailAddressCaptor = ArgumentCaptor.forClass(TdApi.SetAuthenticationEmailAddress.class);
        verify(telegramClient).sendWithCallback(emailAddressCaptor.capture(), any(QueryResultHandler.class));
        assertEquals(email, emailAddressCaptor.getValue().emailAddress);
        assertNull(AuthorizationStateCache.emailAddressInputToCheck); // drop from cache after check
    }

    private void verifyAuthorizationStateWaitEmailCode() {
        var codeFromEmailCaptor = ArgumentCaptor.forClass(TdApi.CheckAuthenticationEmailCode.class);
        verify(telegramClient).sendWithCallback(codeFromEmailCaptor.capture(), any(QueryResultHandler.class));
        TdApi.EmailAddressAuthenticationCode emailCode = (TdApi.EmailAddressAuthenticationCode) codeFromEmailCaptor.getValue().code;
        assertEquals(authCode, emailCode.code);
        assertNull(AuthorizationStateCache.codeInputToCheck); // drop from cache after check
    }

    private void verifyAuthorizationStateReady() {
        assertTrue(AuthorizationStateCache.haveAuthorization.get());
    }

    private void verifyAuthorizationStateLoggingOutOrClosing() {
        assertFalse(AuthorizationStateCache.haveAuthorization.get());
    }

    private void verifyAuthorizationStateClosed() {
        verifyTelegramClientNotInvoked();
        assertTrue(AuthorizationStateCache.stateClosed.get());
    }
}