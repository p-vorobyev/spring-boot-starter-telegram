package dev.voroby.springframework.telegram.client.updates;

import org.drinkless.tdlib.TdApi;

/**
 * Authorization state of the client.
 * Used for client authorization when {@link TdApi.UpdateAuthorizationState} notification
 * received from TDLib.
 * Check functions will not take effect after `UpdateAuthorizationState` have been processed.
 *
 * @author Pavel Vorobyev
 */
public sealed interface ClientAuthorizationState permits ClientAuthorizationStateImpl {
    /**
     * Sends an authentication code to the TDLib for check.
     *
     * @param code authentication code received from another logged in client/SMS/email
     */
    void checkAuthenticationCode(String code);

    /**
     * Sends a password to the TDLib for check.
     *
     * @param password two-step verification password
     */
    void checkAuthenticationPassword(String password);

    /**
     * Sends an email to the TDLib for check.
     *
     * @param email address
     */
    void checkEmailAddress(String email);

    /**
     * @return authentication sate awaiting authentication code
     */
    boolean isWaitAuthenticationCode();

    /**
     * @return authentication sate awaiting two-step verification password
     */
    boolean isWaitAuthenticationPassword();

    /**
     * @return authentication sate awaiting email address
     */
    boolean isWaitEmailAddress();

    /**
     * @return authorization status
     */
    boolean haveAuthorization();

    /**
     * All databases are closed and all resources are released. No other updates will be received after this.
     * All queries will be responded to with error code 500.
     * @return is TDLib client in its final state
     */
    boolean isStateClosed();
}
