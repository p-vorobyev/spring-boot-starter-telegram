package dev.voroby.springframework.telegram.client.updates;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.springframework.util.StringUtils.hasText;

/**
 * Implementation of {@link ClientAuthorizationState}.
 *
 * @author Pavel Vorobyev
 */
public class ClientAuthorizationStateImpl implements ClientAuthorizationState {

    private final AtomicBoolean haveAuthorization = new AtomicBoolean();

    private final AtomicBoolean waitAuthenticationCode = new AtomicBoolean();

    private final AtomicBoolean waitAuthenticationPassword = new AtomicBoolean();

    private final AtomicBoolean waitEmailAddress = new AtomicBoolean();

    private final AtomicBoolean stateClosed = new AtomicBoolean();

    /*
    * code/password/emailAddress will be cleaned up after check
    */
    private volatile String code;

    private volatile String password;

    private volatile String emailAddress;

    @Override
    public synchronized void checkAuthenticationCode(String code) {
        if (waitAuthenticationCode.get()) {
            if (hasText(code)) {
                this.code = code;
                waitAuthenticationCode.set(false);
            }
        }
    }

    @Override
    public synchronized void checkAuthenticationPassword(String password) {
        if (waitAuthenticationPassword.get()) {
            if (hasText(password)) {
                this.password = password;
                waitAuthenticationPassword.set(false);
            }
        }
    }

    @Override
    public synchronized void checkEmailAddress(String email) {
        if (waitEmailAddress.get()) {
            if (hasText(email)) {
                this.emailAddress = email;
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

    void setStateClosed() {
        stateClosed.set(true);
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
