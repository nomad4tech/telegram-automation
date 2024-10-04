package tech.nomad4.chat;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import tech.nomad4.exceptions.UserNotLoggedInException;
import tech.nomad4.login.impl.TelegramLogin;

import static tech.nomad4.utils.PlaywrightUtils.*;

/**
 * The {@code TelegramChatDetailsFetcher} class is responsible for fetching details about specific Telegram chat.
 * It interacts with logged-in Telegram page using Playwright, navigating to the provided chat URL
 * and extracting relevant information such as chat summary, type, and public link.
 * <p>
 * This class requires the user to be logged in to Telegram in order to access chat details.
 * If the user is not logged in, {@link UserNotLoggedInException} will be thrown.
 * </p>
 */
@Slf4j
public class TelegramChatDetailsFetcher {

    public static final String CHAT_MIDDLE_HEADER_SELECTOR = "div.MiddleHeader div.info";
    public static final String CHAT_TITLE_RIGHT_SIDE_SELECTOR = "div.ChatExtra div.ListItem-button span.title";
    public static final String CHAT_INFO_HEADER = "div.RightHeader h3";

    /**
     *
     * Retrieves detailed information about specific Telegram chat.
     *
     * @param chatInfo The {@link TelegramChatSummary} object containing information about the chat,
     *                 including the URL from which to scrape chat details.
     * @return {@link TelegramChatDetails} object with chat info, type, public link
     * @throws UserNotLoggedInException if user is not logged in telegram page (page is not logged in)
     * @param page     The Playwright {@link Page} instance representing logged-in Telegram page.
     *                 This instance is used to navigate and interact with the web application.
     */
    public TelegramChatDetails getData(TelegramChatSummary chatInfo, Page page) throws UserNotLoggedInException {
        if (!TelegramLogin.isLoggedIn(page))
            throw new UserNotLoggedInException("Page not logged IN");
        page.navigate(EXAMPLE_URL);

        String url = chatInfo.getUrl();
        page.navigate(url);

        waitAndClick(page, CHAT_MIDDLE_HEADER_SELECTOR);

        return TelegramChatDetails.builder()
                .chatSummary(parseChatInf(page))
                .type(parseType(page))
                .publicLink(parsePublicLink(page))
                .build();
    }

    private String parsePublicLink(Page page) {
        ElementHandle elementHandle = page.querySelector(CHAT_TITLE_RIGHT_SIDE_SELECTOR);
        if (elementHandle != null)
            return elementHandle.textContent();
        return null;
    }

    private String parseType(Page page) {
        String info = page.querySelector(CHAT_INFO_HEADER).textContent();
        return StringUtils.substringBefore(info, " ");
    }

    private TelegramChatSummary parseChatInf(Page page) {
        // TODO: Implement parsing chat info
        return null;
    }

}
