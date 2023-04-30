package dev.voroby.springframework.telegram.client.updates;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.springframework.util.StringUtils.hasText;

/**
 * Authorization state of the client.
 *
 * @author Pavel Vorobyev
 */
public class ClientAuthorizationState {

    private final AtomicBoolean haveAuthorization = new AtomicBoolean();

    private final AtomicBoolean waitAuthenticationCode = new AtomicBoolean();

    private final AtomicBoolean waitAuthenticationPassword = new AtomicBoolean();

    private final AtomicBoolean waitEmailAddress = new AtomicBoolean();

    /*
    * code/password/emailAddress will be cleaned up after check
    */
    private volatile String code;

    private volatile String password;

    private volatile String emailAddress;

    /**
     * Sends an authentication code to the TDLib for check.
     *
     * @param code authentication code received from another logged in client/SMS/email
     */
    public synchronized void checkAuthenticationCode(String code) {
        if (waitAuthenticationCode.get()) {
            if (hasText(code)) {
                this.code = code;
                waitAuthenticationCode.set(false);
            }
        }
    }

    /**
     * Sends a password to the TDLib for check.
     *
     * @param password two-step verification password
     */
    public synchronized void checkAuthenticationPassword(String password) {
        if (waitAuthenticationPassword.get()) {
            if (hasText(password)) {
                this.password = password;
                waitAuthenticationPassword.set(false);
            }
        }
    }

    /**
     * Sends an email to the TDLib for check.
     *
     * @param email address
     */
    public synchronized void checkEmailAddress(String email) {
        if (waitEmailAddress.get()) {
            if (hasText(email)) {
                this.emailAddress = email;
                waitEmailAddress.set(false);
            }
        }
    }

    /**
     * @return authentication sate awaiting authentication code
     */
    public boolean isWaitAuthenticationCode() {
        return waitAuthenticationCode.get();
    }

    /**
     * @return authentication sate awaiting two-step verification password
     */
    public boolean isWaitAuthenticationPassword() {
        return waitAuthenticationPassword.get();
    }

    /**
     * @return authentication sate awaiting email address
     */
    public boolean isWaitEmailAddress() {
        return waitEmailAddress.get();
    }

    /**
     * @return authorization status
     */
    public boolean haveAuthorization() {
        return haveAuthorization.get();
    }

    String getCode() {
        return code;
    }

    String getPassword() {
        return password;
    }

    String getEmailAddress() {
        return emailAddress;
    }

    void setHaveAuthorization(boolean haveAuthorization) {
        this.haveAuthorization.set(haveAuthorization);
    }

    void setWaitAuthenticationCode() {
        waitAuthenticationCode.set(true);
    }

    void setWaitAuthenticationPassword() {
        waitAuthenticationPassword.set(true);
    }

    void setWaitEmailAddress() {
        waitEmailAddress.set(true);
    }

    void clearCode() {
        code = null;
    }

    void clearPassword() {
        password = null;
    }

    void clearEmailAddress() {
        emailAddress = null;
    }

}
