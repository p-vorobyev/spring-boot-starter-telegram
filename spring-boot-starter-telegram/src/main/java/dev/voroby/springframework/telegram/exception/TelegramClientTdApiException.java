package dev.voroby.springframework.telegram.exception;

/**
 * Telegram TDLib exceptions.
 * @author Pavel Vorobyev
 */
public class TelegramClientTdApiException extends RuntimeException {

    public TelegramClientTdApiException(String message) {
        super(message);
    }

}
