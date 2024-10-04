package tech.nomad4.chat;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import tech.nomad4.exceptions.UserNotLoggedInException;
import tech.nomad4.login.impl.TelegramLogin;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static tech.nomad4.utils.PlaywrightUtils.*;
import static tech.nomad4.utils.PlaywrightUtils.scrollToElement;

/**
 * The {@code TelegramSubscribersFetcher} class is responsible for fetching subscribers from Telegram chat.
 * It utilies Playwright to navigates through the Telegram web interface to scrape data about subscribers,
 * including their URLs, peer IDs, names, and additional information.
 * <p>
 * The class ensures that the user is logged into Telegram before attempting to fetch posts. If the user
 * is not logged in, {@link UserNotLoggedInException} is thrown.
 * </p>
 */
@Slf4j
public class TelegramSubscribersFetcher {

    private static final String CHAT_MIDDLE_HEADER_SELECTOR = "div.MiddleHeader div.info";
    private static final String MEMBER_LIST_SELECTOR = "div.shared-media div.members-list";
    private static final String SUBS_ITEMS_SELECTOR = "div.ListItem.chat-item-clickable.contact-list-item.scroll-item.small-icon";
    private static final String MEMBERS_LIST_SELECTOR = "div.Profile.custom-scroll.Transition_slide.Transition_slide-active";
    private static final String PHONE_SELECTOR = ".icon-phone + .multiline-item .title";
    private static final String CLENDAR_SELECTOR = ".icon-calendar + .multiline-item .title";
    private static final String BIO_SELECTOR = ".icon-info + .multiline-item .title";
    private static final String MENTION_SELECTOR = ".icon-mention + .multiline-item .title";
    private static final String AVATAR_SELECTOR = "div.ChatInfo div.Avatar";
    private static final String DATA_PEER_ID = "data-peer-id";
    private static final String TELEGRAM_BASE_URL = "https://t.me/";
    private static final String TELEGRAM_URL_TEMPLATE = "https://web.telegram.org/a/#%s";

    /**
     * @param chatInfo The {@link TelegramChatSummary} object containing information about the chat,
     *                 including the URL from which to scrape subscribers.
     * @param page     The Playwright {@link Page} instance representing logged-in Telegram page.
     *                 This instance is used to navigate and interact with the web application.
     * @return list of {@link Subscriber} with url, peerId, public link, phone, name, birthDay, bio
     * @throws UserNotLoggedInException if user is not logged in telegram page (page is not logged in)
     */
    public List<Subscriber> getSubscribers(TelegramChatSummary chatInfo, Page page) throws UserNotLoggedInException {
        if (!TelegramLogin.isLoggedIn(page))
            throw new UserNotLoggedInException("Page not logged IN");
        page.navigate(EXAMPLE_URL);
        String url = chatInfo.getUrl();
        page.navigate(url);

        waitAndClick(page, CHAT_MIDDLE_HEADER_SELECTOR);

        return parseSubscribers(page);
    }

    private List<Subscriber> parseSubscribers(Page page) {
        Set<Subscriber> subscribers = new LinkedHashSet<>();
        ElementHandle elementHandle = waitOrNull(page, MEMBER_LIST_SELECTOR);

        int prevCount = 0;

        for (int i = 0; i < 10000; i++) {
            List<ElementHandle> handles = tryWaitElements(elementHandle, SUBS_ITEMS_SELECTOR);

            Set<Subscriber> collect = handles.stream().map(this::parseSubscriber).collect(Collectors.toSet());
            subscribers.addAll(collect);
            int size = subscribers.size();

            // TODO rework this horrible mess
            // TODO how to get subscribers real count?
            if (!handles.isEmpty()) {
                scrollToElement(handles.get(handles.size() - 1));
                List<ElementHandle> elementHandles = tryWaitElements(elementHandle, SUBS_ITEMS_SELECTOR);
                if (!elementHandles.isEmpty()) {
                    ElementHandle elementHandle1 = elementHandles.get(elementHandles.size() - 1);
                    scrollToElement(elementHandle1);

                    for (int j = 0; j < 2; j++) {
                        ElementHandle membersList = page.querySelector(MEMBERS_LIST_SELECTOR);
                        scrollTop(membersList, 500);
                        scrollDown(membersList, 1000);
                    }

                    elementHandles = tryWaitElements(elementHandle, SUBS_ITEMS_SELECTOR, 5000, elementHandles.size() + 1);
                    elementHandle1 = elementHandles.get(elementHandles.size() - 1);
                    scrollToElement(elementHandle1);
                }
            }

            if (size > prevCount)
                prevCount = size;
            else
                break;
        }

        List<Subscriber> subscriberList = new ArrayList<>(subscribers);
        for (int i = 0; i < subscriberList.size(); i++) {
            var subscriber = subscriberList.get(i);
            scrapeAdditionalData(page, subscriber);
            log.info("Scraped subscriber {}/{}: {}", i + 1, subscriberList.size(), subscriber.getName());
        }

        return subscriberList;
    }

    private Subscriber scrapeAdditionalData(Page page, Subscriber subscriber) {
        page.navigate(EXAMPLE_URL);
        page.navigate(subscriber.getUrl());

        subscriber.setPublicLink(getPublicLink(page));
        subscriber.setPhone(waitGetContentOrNull(page, PHONE_SELECTOR, 1000));
        subscriber.setBirthDay(waitGetContentOrNull(page, CLENDAR_SELECTOR, 1000));
        subscriber.setBio(waitGetContentOrNull(page, BIO_SELECTOR, 1000));

        return subscriber;
    }

    private String getPublicLink(Page page) {
        String content = waitGetContentOrNull(page, MENTION_SELECTOR, 5000);
        return StringUtils.isNotBlank(content)
                ? TELEGRAM_BASE_URL + StringUtils.substringAfter(content, "@")
                : null;
    }

    private Subscriber parseSubscriber(ElementHandle e) {
        String peerId = e.querySelector(AVATAR_SELECTOR).getAttribute(DATA_PEER_ID);
        String name = e.querySelector("h3").textContent();

        return Subscriber.builder()
                .url(String.format(TELEGRAM_URL_TEMPLATE, peerId))
                .peerId(peerId)
                .name(name)
                .build();
    }
}
