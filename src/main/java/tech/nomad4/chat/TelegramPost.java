package tech.nomad4.chat;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@EqualsAndHashCode(of = {"messageId"})
public class TelegramPost {

    private Long messageId;

    private String date;

    private Instant parsedDate;

    private String time;

    private String textContent;

    private String sender;

    private String receiver;

    private List<MediaContent> media;

    @Data
    public static class MediaContent {
        private String type;

        private String mediaId;
    }
}
