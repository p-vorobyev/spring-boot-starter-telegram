package dev.voroby.springframework.telegram.client.updates;

import dev.voroby.springframework.telegram.AbstractTest;
import dev.voroby.springframework.telegram.client.Client;
import dev.voroby.springframework.telegram.client.TdApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UpdateAuthorizationNotificationTest extends AbstractTest {

    @Autowired
    private UpdateNotificationListener<TdApi.UpdateAuthorizationState> updateAuthorizationNotification;

    @MockBean(name = "clientAuthorizationState")
    private ClientAuthorizationStateImpl clientAuthorizationState;

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
                when(clientAuthorizationState.getCode()).thenReturn(authCode);
                updateAuthorizationNotification.handleNotification(updateAuthorizationState);
                handleAuthorizationStateWaitCode();
            }
            case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR -> {
                when(clientAuthorizationState.getPassword()).thenReturn(twoStepPassword);
                updateAuthorizationNotification.handleNotification(updateAuthorizationState);
                handleAuthorizationStateWaitPassword();
            }
            case TdApi.AuthorizationStateWaitEmailAddress.CONSTRUCTOR -> {
                when(clientAuthorizationState.getEmailAddress()).thenReturn(email);
                updateAuthorizationNotification.handleNotification(updateAuthorizationState);
                handleAuthorizationStateWaitEmailAddress();
            }
            case TdApi.AuthorizationStateWaitEmailCode.CONSTRUCTOR -> {
                when(clientAuthorizationState.getCode()).thenReturn(authCode);
                updateAuthorizationNotification.handleNotification(updateAuthorizationState);
                handleAuthorizationStateWaitEmailCode();
            }
            case TdApi.AuthorizationStateReady.CONSTRUCTOR -> {
                updateAuthorizationNotification.handleNotification(updateAuthorizationState);
                handeAuthorizationStateReady();
            }
            case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR, TdApi.AuthorizationStateClosing.CONSTRUCTOR -> {
                updateAuthorizationNotification.handleNotification(updateAuthorizationState);
                handleAuthorizationStateLoggingOutOrClosing();
            }
            case TdApi.AuthorizationStateClosed.CONSTRUCTOR -> {
                updateAuthorizationNotification.handleNotification(updateAuthorizationState);
                handleAuthorizationStateClosed();
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
        verify(telegramClient).sendWithCallback(paramsCaptor.capture(), any(Client.ResultHandler.class));
        verify(telegramClient).sendSync(any(TdApi.AddProxy.class));
        TdApi.SetTdlibParameters tdlibParameters = paramsCaptor.getValue();
        assertEquals(123, tdlibParameters.apiId);
    }

    private void handleAuthorizationStateWaitPhoneNumber() {
        ArgumentCaptor<TdApi.SetAuthenticationPhoneNumber> captor = ArgumentCaptor.forClass(TdApi.SetAuthenticationPhoneNumber.class);
        verify(telegramClient).sendWithCallback(captor.capture(), any(Client.ResultHandler.class));
        assertEquals("123456789", captor.getValue().phoneNumber);
    }

    private void handleAuthorizationStateWaitOtherDeviceConfirmation() {
        verifyTelegramClientNotInvoked();
    }

    private void verifyTelegramClientNotInvoked() {
        verify(telegramClient, never()).sendWithCallback(any(TdApi.Function.class), any(Client.ResultHandler.class));
        verify(telegramClient, never()).sendSync(any(TdApi.Function.class));
        verify(telegramClient, never()).sendSync(any(TdApi.Function.class));
        verify(telegramClient, never()).sendAsync(any(TdApi.Function.class));
        verify(telegramClient, never()).sendAsync(any(TdApi.Function.class));
    }

    private void handleAuthorizationStateWaitCode() {
        ArgumentCaptor<TdApi.CheckAuthenticationCode> authCodeCaptor = ArgumentCaptor.forClass(TdApi.CheckAuthenticationCode.class);
        verify(telegramClient).sendWithCallback(authCodeCaptor.capture(), any(Client.ResultHandler.class));
        assertEquals(authCode, authCodeCaptor.getValue().code);
        verify(clientAuthorizationState).clearCode();
    }

    private void handleAuthorizationStateWaitPassword() {
        ArgumentCaptor<TdApi.CheckAuthenticationPassword> authCodeCaptor = ArgumentCaptor.forClass(TdApi.CheckAuthenticationPassword.class);
        verify(telegramClient).sendWithCallback(authCodeCaptor.capture(), any(Client.ResultHandler.class));
        assertEquals(twoStepPassword, authCodeCaptor.getValue().password);
        verify(clientAuthorizationState).clearPassword();
    }

    private void handleAuthorizationStateWaitEmailAddress() {
        ArgumentCaptor<TdApi.SetAuthenticationEmailAddress> emailAddressCaptor = ArgumentCaptor.forClass(TdApi.SetAuthenticationEmailAddress.class);
        verify(telegramClient).sendWithCallback(emailAddressCaptor.capture(), any(Client.ResultHandler.class));
        assertEquals(email, emailAddressCaptor.getValue().emailAddress);
        verify(clientAuthorizationState).clearEmailAddress();
    }

    private void handleAuthorizationStateWaitEmailCode() {
        ArgumentCaptor<TdApi.CheckAuthenticationEmailCode> emailCheckCaptor = ArgumentCaptor.forClass(TdApi.CheckAuthenticationEmailCode.class);
        verify(telegramClient).sendWithCallback(emailCheckCaptor.capture(), any(Client.ResultHandler.class));
        TdApi.EmailAddressAuthenticationCode emailCode = (TdApi.EmailAddressAuthenticationCode) emailCheckCaptor.getValue().code;
        assertEquals(authCode, emailCode.code);
        verify(clientAuthorizationState).clearCode();
    }

    private void handeAuthorizationStateReady() {
        assertTrue(verifyAndCaptureIsHaveAuthorization().getValue());
    }

    private void handleAuthorizationStateLoggingOutOrClosing() {
        assertFalse(verifyAndCaptureIsHaveAuthorization().getValue());
    }

    private void handleAuthorizationStateClosed() {
        verifyTelegramClientNotInvoked();
        verify(clientAuthorizationState).setStateClosed();
    }

    private ArgumentCaptor<Boolean> verifyAndCaptureIsHaveAuthorization() {
        verifyTelegramClientNotInvoked();
        ArgumentCaptor<Boolean> haveAuthorizationCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(clientAuthorizationState).setHaveAuthorization(haveAuthorizationCaptor.capture());

        return haveAuthorizationCaptor;
    }
}