package tech.nomad4.chat;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TelegramChatDetails {

    private TelegramChatSummary chatSummary;

    private String type;

    private String publicLink;

    private String info;

    private int subscribersCount;
}