package tech.nomad4.chat;

import lombok.*;

@Builder
@Getter
@EqualsAndHashCode(of = {"url"})
@NoArgsConstructor
@AllArgsConstructor
public class TelegramChatSummary {
    private String title;
    private String url;
    @NonNull
    private ChatType type;
}
