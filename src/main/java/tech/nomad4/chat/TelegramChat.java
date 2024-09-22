package tech.nomad4.chat;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class TelegramChat {

    public List<String> getChats(Page page) {
        ElementHandle leftColumn = page.querySelector("#LeftColumn-main");
        ElementHandle chatSlide = page.querySelector("div.chat-list.custom-scroll.Transition_slide.Transition_slide-active");
        if (chatSlide != null) {
            chatSlide.evaluate("element => element.scrollBy(0, 1000)");
        }

        List<ElementHandle> chatItems = page.querySelectorAll("div.chat-item-clickable");

        List<TelegramChatInfo> infos = new ArrayList<>();

        for (int i = 1; i < chatItems.size(); i++) {
            ElementHandle chatItem = chatItems.get(i);
            String title = chatItem.querySelector("div.info h3").textContent();
            String link = "https://web.telegram.org/a/" + chatItem.querySelector("a").getAttribute("href");
            String type = parseType(chatItem);
            String avatar = null;
            try {
                avatar = chatItem.querySelector("div.status div.inner img").getAttribute("src");
            } catch (Exception e) {
                System.out.println();
            }
            ElementHandle lastMessageMeta = chatItem.querySelector("div.LastMessageMeta");

            TelegramChatInfo chatInfo = TelegramChatInfo.builder()
                    .title(title)
                    .url(link)
                    .avatar(avatar)
                    .type(type)
                    .build();
            infos.add(chatInfo);

        }

        return new ArrayList<>();
    }

    private String parseType(ElementHandle chatItem) {
        String attribute = chatItem.getAttribute("class");

        if (attribute.contains("private"))
            return "PRIVATE";
        return "GROUP";
    }


    @Builder
    @Getter
    public static class TelegramChatInfo {
        private String title;
        private String url;
        private String avatar;
        private String type;
    }


}
