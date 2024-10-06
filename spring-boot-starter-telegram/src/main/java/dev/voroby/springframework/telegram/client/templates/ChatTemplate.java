package dev.voroby.springframework.telegram.client.templates;

import org.drinkless.tdlib.TdApi;
import dev.voroby.springframework.telegram.client.TelegramClient;
import dev.voroby.springframework.telegram.client.templates.response.Response;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * This class simplifies the use of {@link TelegramClient} for chat related objects.
 *
 * @author Pavel Vorobyev
 */
public class ChatTemplate {

    private final TelegramClient telegramClient;

    public ChatTemplate(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    /**
     * Returns information about a chat by its identifier; this is an offline request if the current user is not a bot.
     *
     * @param chatId Chat identifier.
     * @return {@link CompletableFuture<Response<TdApi.Chat>>}.
     */
    public CompletableFuture<Response<TdApi.Chat>> getChat(long chatId) {
        return telegramClient.sendAsync(new TdApi.GetChat(chatId));
    }

    /**
     * Adds the current user as a new member to a chat. Private and secret chats can't be joined using this method.
     * May return an error with a message "INVITE_REQUEST_SENT" if only a join request was created.
     *
     * @param chatId Chat identifier.
     * @return {@link CompletableFuture<Response<TdApi.Ok>>} Response to action.
     */
    public CompletableFuture<Response<TdApi.Ok>> joinChat(long chatId) {
        return telegramClient.sendAsync(new TdApi.JoinChat(chatId));
    }

    /**
     * Searches for the specified query in the title and username of already known chats; this is an offline request.
     * Returns chats in the order seen in the main chat list.
     *
     * @param query Query to search for.
     * @param limit The maximum number of chats to be returned.
     * @return {@link CompletableFuture<Response<TdApi.Chats>>}.
     */
    public CompletableFuture<Response<TdApi.Chats>> searchChats(String query, int limit) {
        Objects.requireNonNull(query);
        return telegramClient.sendAsync(new TdApi.SearchChats(query, limit));
    }

    /**
     * Searches a public chat by its username. Currently, only private chats, supergroups and channels can be public.
     * Returns the chat if found; otherwise, an error is returned.
     *
     * @param username Username to be resolved.
     * @return {@link CompletableFuture<Response<TdApi.Chat>>}.
     */
    public CompletableFuture<Response<TdApi.Chat>> searchPublicChat(String username) {
        Objects.requireNonNull(username);
        return telegramClient.sendAsync(new TdApi.SearchPublicChat(username));
    }

    /**
     * Searches public chats by looking for specified query in their username and title. Currently, only private chats,
     * supergroups and channels can be public. Returns a meaningful number of results.
     * Excludes private chats with contacts and chats from the chat list from the results.
     *
     * @param query Query to search for.
     * @return {@link CompletableFuture<Response<TdApi.Chats>>}.
     */
    public CompletableFuture<Response<TdApi.Chats>> searchPublicChats(String query) {
        Objects.requireNonNull(query);
        return telegramClient.sendAsync(new TdApi.SearchPublicChats(query));
    }

    /**
     * Removes the current user from chat members. Private and secret chats can't be left using this method.
     *
     * @param chatId Chat identifier.
     * @return {@link CompletableFuture<Response<TdApi.Ok>>}.
     */
    public CompletableFuture<Response<TdApi.Ok>> leaveChat(long chatId) {
        return telegramClient.sendAsync(new TdApi.LeaveChat(chatId));
    }

    /**
     * Deletes a chat along with all messages in the corresponding chat for all chat members.
     * For group chats this will release the usernames and remove all members.
     * Use the field chat.canBeDeletedForAllUsers to find whether the method can be applied to the chat.
     *
     * @param chatId Chat identifier.
     * @return {@link CompletableFuture<Response<TdApi.Ok>>}.
     */
    public CompletableFuture<Response<TdApi.Ok>> deleteChat(long chatId) {
        return telegramClient.sendAsync(new TdApi.DeleteChat(chatId));
    }

    /**
     * Returns information about a basic group by its identifier. This is an offline request if the current user is not a bot.
     *
     * @param basicGroupId Basic group identifier.
     * @return {@link CompletableFuture<Response<TdApi.BasicGroup>>}.
     */
    public CompletableFuture<Response<TdApi.BasicGroup>> getBasicGroup(long basicGroupId) {
        return telegramClient.sendAsync(new TdApi.GetBasicGroup(basicGroupId));
    }

    /**
     * Returns full information about a basic group by its identifier.
     *
     * @param basicGroupId Basic group identifier.
     * @return {@link CompletableFuture<Response<TdApi.BasicGroupFullInfo>>}.
     */
    public CompletableFuture<Response<TdApi.BasicGroupFullInfo>> getBasicGroupFullInfo(long basicGroupId) {
        return telegramClient.sendAsync(new TdApi.GetBasicGroupFullInfo(basicGroupId));
    }

    /**
     * Returns information about a supergroup or a channel by its identifier.
     * This is an offline request if the current user is not a bot.
     *
     * @param supergroupId Supergroup or channel identifier.
     * @return {@link CompletableFuture<Response<TdApi.Supergroup>>}.
     */
    public CompletableFuture<Response<TdApi.Supergroup>> getSupergroup(long supergroupId) {
        return telegramClient.sendAsync(new TdApi.GetSupergroup(supergroupId));
    }

    /**
     * Returns full information about a supergroup or a channel by its identifier, cached for up to 1 minute.
     *
     * @param supergroupId Supergroup or channel identifier.
     * @return {@link CompletableFuture<Response<TdApi.SupergroupFullInfo>>}.
     */
    public CompletableFuture<Response<TdApi.SupergroupFullInfo>> getSupergroupFullInfo(long supergroupId) {
        return telegramClient.sendAsync(new TdApi.GetSupergroupFullInfo(supergroupId));
    }

}
