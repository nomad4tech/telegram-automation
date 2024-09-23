package tech.nomad4.chat;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import tech.nomad4.exceptions.UserNotLoggedInException;
import tech.nomad4.login.impl.TelegramLogin;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static tech.nomad4.utils.PlaywrightUtils.*;

public class TelegramChat {

    public static final String URL_TEMPLATE = "https://web.telegram.org/a/%s";

    public List<TelegramChatInfo> getChats(Page page) throws UserNotLoggedInException {
        if (!TelegramLogin.isLoggedIn(page))
            throw new UserNotLoggedInException("Page not logged IN");

        Set<TelegramChatInfo> all = getTelegramChatInfos(page);
        Set<TelegramChatInfo> archive = getTelegramChatInfos(page, true);
        all.addAll(archive);

        return new ArrayList<>(all);
    }

    private Set<TelegramChatInfo> getTelegramChatInfos(Page page) {
        return getTelegramChatInfos(page, false);
    }

    private Set<TelegramChatInfo> getTelegramChatInfos(Page page, boolean archive) {
        waitIsVisible(page, "div.chat-item-clickable");

        List<ElementHandle> tabs = page.querySelectorAll("div.TabList.no-scrollbar div.Tab.Tab--interactive");

        ElementHandle chats = tabs.stream().filter(e -> e.textContent().equalsIgnoreCase("All Chats")).findFirst().orElse(null);

        Set<TelegramChatInfo> infos = new LinkedHashSet<>();
        if (chats != null) {
            chats.click();
            if (archive)
                waitAndClick(page, "div.chat-item-clickable.chat-item-archive");

            int prevSize = 0;
            for (int i = 0; i < 1000; i++) {
                List<ElementHandle> chatItems = page.querySelectorAll("div.chat-item-clickable:not(.chat-item-archive)");
                if (!chatItems.isEmpty()) {
                    scrollToElement(chatItems.get(chatItems.size() - 1));
                }

                infos.addAll(chatItems.stream().map(this::getTelegramChatInfo).toList());

                int size = infos.size();

                if (prevSize < size) {
                    prevSize = size;
                } else {
                    break;
                }
            }
        }
        return infos;
    }

    private TelegramChatInfo getTelegramChatInfo(ElementHandle chatItem) {
        return TelegramChatInfo.builder()
                .title(parseTitle(chatItem))
                .url(parseUrl(chatItem))
                .type(parseType(chatItem))
                .build();
    }

    private String parseAvatar(ElementHandle chatItem) {
        String avatar = null;
        List<ElementHandle> elementHandles = tryWaitElements(chatItem, "div.status div.inner img", 3);
        if (!elementHandles.isEmpty())
            avatar = elementHandles.get(0).getAttribute("src");
        return avatar;
    }

    private String parseTitle(ElementHandle chatItem) {
        return chatItem.querySelector("div.info h3").textContent();
    }

    private String parseUrl(ElementHandle chatItem) {
        return URL_TEMPLATE.formatted(chatItem.querySelector("a").getAttribute("href"));
    }

    private String parseType(ElementHandle chatItem) {
        String attribute = chatItem.getAttribute("class");
        if (attribute.contains("private"))
            return "PRIVATE";
        return "GROUP";
    }

    @Builder
    @Getter
    @EqualsAndHashCode(of = {"url"})
    public static class TelegramChatInfo {
        private String title;
        private String url;
        private String type;
    }

}
