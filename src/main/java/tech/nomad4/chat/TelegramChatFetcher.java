package tech.nomad4.chat;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import tech.nomad4.exceptions.UserNotLoggedInException;
import tech.nomad4.login.impl.TelegramLogin;

import java.util.*;

import static tech.nomad4.utils.PlaywrightUtils.*;

/**
 * The {@code TelegramChatFetcher} class is responsible for retrieving chat information
 * from the Telegram web application. It can fetch both active and archived chats,
 * providing details such as the title, URL, and type (private or group) of each chat.
 * <p>
 * The class ensures that the user is logged into Telegram before attempting to fetch posts. If the user
 * is not logged in, {@link UserNotLoggedInException} is thrown.
 * </p>
 */
public class TelegramChatFetcher {

    private static final String URL_TEMPLATE = "https://web.telegram.org/a/%s";
    private static final String ARCHIVE_SELECTOR = "div.chat-item-clickable.chat-item-archive";
    private static final String CHAT_ITEMS_SELECTOR = "div.chat-item-clickable:not(.chat-item-archive)";
    private static final String TITTLE_HEADER_SELECTOR = "div.info h3";
    public static final String CHATS_TABS_SELECTOR = "div.TabList.no-scrollbar div.Tab.Tab--interactive";
    public static final String ALL_CHATS_STR_START = "All Chats";
    public static final String CHAT_ITEM_CLICKABLE = "div.chat-item-clickable";

    /**
     * Retrieves list of all chats from the Telegram web page, including archived chats.
     *
     * @param page     The Playwright {@link Page} instance representing logged-in Telegram page.
     *                 This instance is used to navigate and interact with the web application.
     * @return list of {@link TelegramChatSummary} objects with title, url and type (private or group) of chat included archive chats
     * @throws UserNotLoggedInException if user is not logged in telegram page (page is not logged in)
     */
    public List<TelegramChatSummary> getChats(Page page) throws UserNotLoggedInException {
        if (!TelegramLogin.isLoggedIn(page))
            throw new UserNotLoggedInException("Page not logged IN");

        Set<TelegramChatSummary> all = getTelegramChatInfos(page);
        Set<TelegramChatSummary> archive = getTelegramChatInfos(page, true);
        all.addAll(archive);

        return new ArrayList<>(all);
    }

    private Set<TelegramChatSummary> getTelegramChatInfos(Page page) {
        return getTelegramChatInfos(page, false);
    }

    private Set<TelegramChatSummary> getTelegramChatInfos(Page page, boolean archive) {
        waitIsVisible(page, CHAT_ITEM_CLICKABLE);
        List<ElementHandle> tabs = page.querySelectorAll(CHATS_TABS_SELECTOR);

        // TODO rework for more specific filter for "All Chats"
        ElementHandle chats = tabs.stream().filter(e -> e.textContent().startsWith(ALL_CHATS_STR_START))
                .findFirst()
                .orElse(null);

        if (chats != null) {
            chats.click();
            if (archive)
                waitAndClick(page, ARCHIVE_SELECTOR);

            return getTelegramChatSummaries(page);
        }
        return Collections.EMPTY_SET;
    }

    private Set<TelegramChatSummary> getTelegramChatSummaries(Page page) {
        int prevSize = 0;
        Set<TelegramChatSummary> infos = new LinkedHashSet<>();

        for (int i = 0; i < 1000; i++) {
            List<ElementHandle> chatItems = page.querySelectorAll(CHAT_ITEMS_SELECTOR);
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
        return infos;
    }

    private TelegramChatSummary getTelegramChatInfo(ElementHandle chatItem) {
        return TelegramChatSummary.builder()
                .title(parseTitle(chatItem))
                .url(parseUrl(chatItem))
                .type(parseType(chatItem))
                .build();
    }

    private String parseTitle(ElementHandle chatItem) {
        return chatItem.querySelector(TITTLE_HEADER_SELECTOR).textContent();
    }

    private String parseUrl(ElementHandle chatItem) {
        return URL_TEMPLATE.formatted(chatItem.querySelector("a").getAttribute("href"));
    }

    private ChatType parseType(ElementHandle chatItem) {
        String attribute = chatItem.getAttribute("class");
        if (attribute.contains("private"))
            return ChatType.PRIVATE;
        return ChatType.GROUP;
    }

}
