package tech.nomad4.chat;

import lombok.Builder;
import lombok.Getter;

/**
 * The {@code TelegramChatDetails} class encapsulates detailed information about specific Telegram chat.
 * It includes {@link TelegramChatSummary} information, {@link ChatType}, public link, additional info, and the number of subscribers.
 */
@Getter
@Builder
public class TelegramChatDetails {

    /**
     * Ssummary of the chat, including its title, URL, and type.
     * Not implemented yet.
     */
    private TelegramChatSummary chatSummary;

    /**
     * The type of the chat (e.g., public, private).
     */
    private String type;

    /**
     * The public link to the chat, if available.
     * <p>
     * This link can be used to access the chat directly within the Telegram web application.
     * If the chat is private, this field may be null.
     * </p>
     */
    private String publicLink;

    /**
     * Additional information about chat.
     * <p>
     * Not used yet.
     * </p>
     */
    private String info;

    /**
     * The number of subscribers in chat.
     * <p>
     * Not implemented yet.
     * This value represents the total count of users subscribed to the chat.
     * </p>
     */
    private int subscribersCount;
}