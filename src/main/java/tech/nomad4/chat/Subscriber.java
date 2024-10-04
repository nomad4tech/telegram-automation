package tech.nomad4.chat;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * The {@code Subscriber} class represents Telegram user who subscribes to service or channel.
 * It contains information about the subscriber, including their contact details and social links.
 */
@Getter
@Setter
@Builder
@EqualsAndHashCode(of = {"peerId"})
public class Subscriber {

    /**
     * The URL associated with the subscriber.
     * <p>
     * Note: This URL is typically only accessible by explicitly finding the user's page/chat,
     * for example, through group or direct messages with user.
     * </p>
     * */
    private String url;

    /** The unique identifier for the subscriber in Telegram system. */
    private String peerId;

    /**
     * The public link to the subscriber's profile.
     * <p>
     * Note: This link is always accessible as long as the subscriber's profile is public.
     * May change if the subscriber updates their profile.
     * Not all subscribers may have publicLink available. ????
     * </p>
     * */
    private String publicLink;

    /**
     * The phone number of the subscriber.
     * <p>
     * Note: Not all subscribers may have a phone number available.
     * </p>
     */
    private String phone;

    /** The name of the subscriber. */
    private String name;

    /**
     * The birth date of the subscriber, not formatted, raw from telegram ui.
     * <p>
     * Note: Not all subscribers may have birthDay available.
     * </p>
     * */
    private String birthDay;

    /**
     * Brief biography or description of the subscriber.
     * <p>
     * Note: Not all subscribers may have bio available.
     * </p>
     * */
    private String bio;

}
