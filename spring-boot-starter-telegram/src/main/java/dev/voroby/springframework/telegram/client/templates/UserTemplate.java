package dev.voroby.springframework.telegram.client.templates;

import dev.voroby.springframework.telegram.client.TdApi;
import dev.voroby.springframework.telegram.client.TelegramClient;
import dev.voroby.springframework.telegram.exception.TelegramClientTdApiException;

/**
 * This class simplifies the use of {@link TelegramClient} for {@link TdApi.User} related objects.
 *
 * @author Pavel Vorobyev
 */
public class UserTemplate {

    private final TelegramClient telegramClient;

    public UserTemplate(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    /**
     * Returns information about a user by their identifier. This is an offline request.
     *
     * @param userId User identifier.
     * @return {@link TdApi.User}.
     * @throws TelegramClientTdApiException for TDLib request timeout or returned {@link TdApi.Error}.
     */
    public TdApi.User getUser(long userId) {
        return telegramClient.sendSync(new TdApi.GetUser(userId));
    }

    /**
     * Returns full information about a user by their identifier.
     *
     * @param userId User identifier.
     * @return {@link TdApi.UserFullInfo}.
     * @throws TelegramClientTdApiException for TDLib request timeout or returned {@link TdApi.Error}.
     */
    public TdApi.UserFullInfo getUserFullInfo(long userId) {
        return telegramClient.sendSync(new TdApi.GetUserFullInfo(userId));
    }

    /**
     * Returns an HTTPS link, which can be used to get information about the current user.
     *
     * @return {@link TdApi.UserLink}.
     * @throws TelegramClientTdApiException for TDLib request timeout or returned {@link TdApi.Error}.
     */
    public TdApi.UserLink getUserLink() {
        return telegramClient.sendSync(new TdApi.GetUserLink());
    }

    /**
     * Returns the current user.
     *
     * @return {@link TdApi.User}.
     * @throws TelegramClientTdApiException for TDLib request timeout or returned {@link TdApi.Error}.
     */
    public TdApi.User getMe() {
        return telegramClient.sendSync(new TdApi.GetMe());
    }


    /**
     * Returns profile photo of the user. May be null.
     *
     * @param userId User identifier.
     * @return {@link TdApi.ProfilePhoto} or null.
     * @throws TelegramClientTdApiException for TDLib request timeout or returned {@link TdApi.Error}.
     */
    public TdApi.ProfilePhoto getProfilePhoto(long userId) {
        return getUser(userId).profilePhoto;
    }


    /**
     * Returns user profile photo visible if the main photo is hidden by privacy settings. May be null.
     *
     * @param userId User identifier.
     * @return {@link TdApi.ChatPhoto} or null.
     * @throws TelegramClientTdApiException for TDLib request timeout or returned {@link TdApi.Error}.
     */
    public TdApi.ChatPhoto getPublicPhoto(long userId) {
        return getUserFullInfo(userId).publicPhoto;
    }

    /**
     * Returns the profile photos of a user. Personal and public photo aren't returned.
     *
     * @param userId User identifier.
     * @param offset The number of photos to skip; must be non-negative.
     * @param limit The maximum number of photos to be returned; up to 100.
     * @return {@link TdApi.ChatPhotos}.
     * @throws TelegramClientTdApiException for TDLib request timeout or returned {@link TdApi.Error}.
     */
    public TdApi.ChatPhotos getUserProfilePhotos(long userId, int offset, int limit) {
        return telegramClient.sendSync(new TdApi.GetUserProfilePhotos(userId, offset, limit));
    }

    /**
     * Searches a user by their phone number. Returns null if user can't be found.
     *
     * @param phoneNumber Phone number in international format to search for.
     * @return {@link TdApi.User} or null.
     * @throws TelegramClientTdApiException for TDLib request timeout or returned {@link TdApi.Error}.
     */
    public TdApi.User searchUserByPhoneNumber(String phoneNumber) {
        try {
            return telegramClient.sendSync(new TdApi.SearchUserByPhoneNumber(phoneNumber));
        } catch (TelegramClientTdApiException e) {
            if (e.getError().code == 404) return null;
            throw e;
        }
    }

}
