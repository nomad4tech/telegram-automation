package tech.nomad4.chat;

import lombok.*;

import java.time.Instant;
import java.util.List;

/**
 * The {@code TelegramPost} class represents message in Telegram chat.
 * It contains information about the message such as its ID, date, sender,
 * receiver, content, and associated media.
 */
@Getter
@Setter
@Builder
@EqualsAndHashCode(of = {"messageId"})
public class TelegramPost {

    /**
     * The unique identifier for the message.
     * <p>
     * This ID is used to distinguish between different messages in the chat.
     * </p>
     */
    private Long messageId;

    /**
     * The date the message was sent in human-readable format.
     * <p>
     * This format may vary based on the context (e.g., "TODAY", "YESTERDAY",
     * or specific date).
     * </p>
     */
    private String date;

    /**
     * The parsed date and time of the message as an {@link Instant}.
     * <p>
     * This is used for precise date-time calculations and comparisons. Psrsed from {@link #date} and {@link #time}.
     * </p>
     */
    private Instant parsedDate;

    /**
     * The time the message was sent, typically in the format "HH:mm".
     * <p>
     * This value is often displayed alongside the message content.
     * </p>
     */
    private String time;

    /**
     * The textual content of the message.
     * <p>
     * This may include plain text, emojis, or other characters sent by the user.
     * </p>
     */
    // TODO check emojis content available
    private String textContent;

    /**
     * The sender of the message.
     * <p>
     * This could be the username or display name of the user who sent the message.
     * </p>
     */
    private String sender;

    /**
     * The receiver of the message.
     * <p>
     * </p>
     */
    // TODO add description
    private String receiver;

    /**
     * List of media associated with the message.
     * <p>
     * This can include images, videos, documents, or other types of media
     * shared within the message.
     * </p>
     */
    // TODO xhecking possibility to get contrnt without Telegram
    private List<MediaContent> media;

    /**
     * The {@code MediaContent} class represents piece of media associated with message.
     */
    @Data
    public static class MediaContent {
        /**
         * The type of media (e.g., "image", "video").
         * <p>
         * This helps identify what kind of media is associated with the message.
         * </p>
         */
        private String type;

        /**
         * The unique identifier for the media.
         * <p>
         * This ID can be used to retrieve the media content.
         * </p>
         */
        private String mediaId;
    }
}
