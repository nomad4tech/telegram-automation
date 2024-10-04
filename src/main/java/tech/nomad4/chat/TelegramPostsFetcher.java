package tech.nomad4.chat;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import org.apache.commons.lang3.StringUtils;
import tech.nomad4.exceptions.UserNotLoggedInException;
import tech.nomad4.login.impl.TelegramLogin;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import static tech.nomad4.utils.CommonUtils.randomPause;
import static tech.nomad4.utils.PlaywrightUtils.*;
import static tech.nomad4.utils.PlaywrightUtils.scrollToElement;

/**
 * The {@code TelegramPostsFetcher} class is responsible for fetching posts from specific Telegram chat.
 * It utilizes Playwright to navigate to given chat URL and extracts message data such as content,
 * sender, receiver, timestamp, and media associated with each post.
 * <p>
 * The class ensures that the user is logged into Telegram before attempting to fetch posts. If the user
 * is not logged in, {@link UserNotLoggedInException} is thrown.
 * </p>
 */
public class TelegramPostsFetcher {
    private static final String MESSAGE_CONTAINER_SELECTOR = "div.messages-container";
    private static final String DATE_GROUP_SELECTOR = "div.message-date-group";
    private static final String DATE_SELECTOR = "div.sticky-date span";
    private static final String POSTS_SELECTOR = "div[id^='message']";
    private static final String CONTENT_SELECTOR = "div.text-content";
    private static final String MEDIA_IN_POST_SELECTOR = "div.content-inner div.media-inner";
    private static final String PROFILE_PHOTO_SELECTOR = "div.profile-info div.ProfilePhoto img";

    /**
     * Fetches limited number of postsfrom specific Telegram chat.
     * The number of posts retrieved is determined by the {@code maxPosts} parameter,
     * which may result in fetching fewer or more posts than specified due to the way messages
     * are loaded and displayed in the Telegram web interface.
     *
     *
     * @param chatInfo The {@link TelegramChatSummary} object containing information about the chat,
     *                 including the URL from which to scrape posts.
     * @param maxPosts The maximum number of posts to retrieve from the chat. The method will attempt
     *                 to retrieve up to this number, but the actual count may vary.
     * @param page     The Playwright {@link Page} instance representing logged-in Telegram page.
     *                 This instance is used to navigate and interact with the web application.
     * @return list of {@link TelegramPost} objects, each representing message in the chat.
     *         Each object contains details such as message ID, text content, date, media content,
     *         receiver, sender, time, and the parsed date of the message.
     * @throws UserNotLoggedInException if user is not logged in telegram page (page is not logged in)
     */
    public List<TelegramPost> getPosts(TelegramChatSummary chatInfo, int maxPosts, Page page) throws UserNotLoggedInException {
        if (!TelegramLogin.isLoggedIn(page))
            throw new UserNotLoggedInException("Page not logged IN");
        page.navigate(EXAMPLE_URL);
        String url = chatInfo.getUrl();
        page.navigate(url);

        if (ChatType.PRIVATE == chatInfo.getType()) {
            return parsePosts(page, chatInfo.getTitle(), maxPosts);
        }

        return parsePosts(page, maxPosts);
    }

    private List<TelegramPost> parsePosts(Page page, int maxPosts) {
        return parsePosts(page, null, maxPosts);
    }

    private List<TelegramPost> parsePosts(Page page, String title, int maxPosts) {
        Set<TelegramPost> posts = new LinkedHashSet<>();
        ElementHandle elementHandle = waitOrNull(page, MESSAGE_CONTAINER_SELECTOR);

        int prevCount = 0;

        for (int i = 0; i < 10000; i++) {
            if (maxPosts <= prevCount)
                break;
            List<ElementHandle> dateGroup = tryWaitElements(elementHandle, DATE_GROUP_SELECTOR);

            for (ElementHandle handle : dateGroup) {
                String date = handle.querySelector(DATE_SELECTOR).textContent();
                List<ElementHandle> postsElements = handle.querySelectorAll(POSTS_SELECTOR);

                for (ElementHandle postsElement : postsElements) {
                    TelegramPost telegramPost = parseTelegramPost(postsElement, date, title);
                    posts.add(telegramPost);
                }

                if (!postsElements.isEmpty()) {
                    scrollToElement(postsElements.get(0));
                }

            }

            int size = posts.size();

            if (!dateGroup.isEmpty()) {
                // TODO need some pauses for upload all posts
                ElementHandle elementHandle1 = dateGroup.get(0);
                scrollToElement(elementHandle1);
                ElementHandle elementHandle2 = elementHandle1.querySelectorAll(POSTS_SELECTOR).get(0);
                scrollToElement(elementHandle2);
                randomPause(5000, 7000);
                scrollTop(elementHandle2, 1000);
                randomPause(5000, 7000);
            }

            if (size > prevCount)
                prevCount = size;
            else
                break;
        }

        // TODO add sender in multiple messages from one sender
        // TODO remove sender if its repost
        // TODO add reposter
        List<TelegramPost> collect = posts.stream()
                .sorted(Comparator.comparing(TelegramPost::getMessageId))
                .collect(Collectors.toList());

        return collect;
    }

    private TelegramPost parseTelegramPost(ElementHandle e, String date, String title) {
        ElementHandle elementHandle = e.querySelector("span.message-time");
        String time = elementHandle != null ? elementHandle.textContent() : null;

        String receiver = null;
        String sender = null;

        ElementHandle repost = e.querySelector("div.message-subheader");

        if (repost != null) {
            // TODO reposted message without any mark/id
        }

        // TODO come up with something better than this mess
        if (StringUtils.isNotBlank(title) && e.querySelector("div.with-outgoing-icon") != null) {
            receiver = title;
        } else if (StringUtils.isNotBlank(title)) {
            sender = title;
        } else {
            ElementHandle elementHandle1 = e.querySelector("span.message-title-name");
            if (elementHandle1 != null) {
                sender = elementHandle1.textContent();
            } else {
                // TODO check if message without title
            }
        }

        return TelegramPost.builder()
                .messageId(parseId(e))
                .textContent(parseTextContent(e, time))
                .date(date)
                .media(parseMedia(e))
                .receiver(receiver)
                .sender(sender)
                .time(time)
                .parsedDate(parseDate(date, time))
                .build();
    }

    private Long parseId(ElementHandle e) {
        String id = e.getAttribute("id");
        return Long.parseLong(StringUtils.getDigits(id));
    }

    private String parseTextContent(ElementHandle e, String time) {
        ElementHandle elementHandle = e.querySelector(CONTENT_SELECTOR);
        if (elementHandle != null) {
            String content = elementHandle.textContent();
            return StringUtils.substringBefore(content, time);
        }
        return null;
    }

    private List<TelegramPost.MediaContent> parseMedia(ElementHandle e) {
        List<ElementHandle> elementHandles = e.querySelectorAll(MEDIA_IN_POST_SELECTOR);
        List<TelegramPost.MediaContent> mediaContents = new ArrayList<>();
        for (ElementHandle elementHandle : elementHandles) {
            TelegramPost.MediaContent mc = new TelegramPost.MediaContent();
            mc.setMediaId(elementHandle.getAttribute("id"));
            mediaContents.add(mc);

            ElementHandle handle = elementHandle.querySelectorAll("*").get(0);
            String tagName = handle.evaluate("element => element.tagName").toString();

            mc.setType(tagName + " " + handle.getAttribute("class"));
        }

        return mediaContents;
    }

    private Instant parseDate(String date, String time) {
        if (StringUtils.isBlank(time))
            return null;

        time = time.replace("edited", "").trim();

        LocalDate datePart;

        if (date.equalsIgnoreCase("TODAY")) {
            datePart = LocalDate.now();
        } else if (date.equalsIgnoreCase("YESTERDAY")) {
            datePart = LocalDate.now().minusDays(1);
        } else if (date.matches(".*\\d{4}$")) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);
            datePart = LocalDate.parse(date, formatter);
        } else if (date.matches("^[A-Za-z]+ \\d+$")) {
            String currentYear = String.valueOf(Year.now().getValue());
            String dateWithYear = date + " " + currentYear;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d yyyy", Locale.ENGLISH);
            datePart = LocalDate.parse(dateWithYear, formatter);
        } else if (date.matches("^[A-Za-z]+$")) {
            DayOfWeek dayOfWeek = DayOfWeek.valueOf(date.toUpperCase(Locale.ENGLISH));
            datePart = LocalDate.now().with(TemporalAdjusters.previousOrSame(dayOfWeek));
        } else {
            throw new IllegalArgumentException("Unknown date format: " + date);
        }

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
        LocalTime timePart = LocalTime.parse(time, timeFormatter);

        LocalDateTime dateTime = LocalDateTime.of(datePart, timePart);

        return dateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    private List<String> parseAvatars(Page page) {
        // TODO sometimes one of avatar is null
        var res = tryWaitElements(page, PROFILE_PHOTO_SELECTOR);
        return res.stream()
                .map(e -> e.getAttribute("src"))
                .toList();
    }
}
