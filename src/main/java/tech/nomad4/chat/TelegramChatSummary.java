package tech.nomad4.chat;

import lombok.*;

/**
 * Represents summary of Telegram chat.
 * <p>
 * This class encapsulates essential information about chat, including its title,
 * URL, and type (private or group). It is designed to provide concise overview
 * of chat for easy retrieval and display.
 * </p>
 * <p>
 * Note: The {@code url} field is unique for each chat, and the {@code type} field
 * must be non-null, indicating whether the chat is private or group.
 * </p>
 */
@Builder
@Getter
@EqualsAndHashCode(of = {"url"})
@NoArgsConstructor
@AllArgsConstructor
public class TelegramChatSummary {
    /**
     * The title of the chat.
     * <p>
     * This may represent the name of the group or the username of private chat.
     * </p>
     */
    private String title;

    /**
     * The unique URL of the chat.
     * <p>
     * This URL can be used to directly access the chat within the Telegram web
     * application.
     * Note: If chat is public, this URL is accessible to anyone.
     * </p>
     */
    private String url;

    /**
     * The {@link ChatType} type of chat (private or group).
     * <p>
     * This field is mandatory and cannot be null. It indicates the nature of the
     * chat, which influences how it should be handled by the application.
     * </p>
     */
    @NonNull
    private ChatType type;
}
