package tech.nomad4.chat;

/**
 * Represents the type of Telegram chat.
 * <p>
 * This enumeration defines two types of chats:
 * </p>
 * <ul>
 *     <li>{@link #PRIVATE} - Represents private chat with individual user.</li>
 *     <li>{@link #GROUP} - Represents group chat that can contain multiple participants.</li>
 * </ul>
 */
public enum ChatType {
    PRIVATE,
    GROUP
}
