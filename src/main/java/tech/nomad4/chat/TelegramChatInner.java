package tech.nomad4.chat;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import lombok.*;
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

public class TelegramChatInner {

    private static int MAX_POST_COUNT = 1000;

    public List<TelegramPost> getPosts(TelegramChat.TelegramChatInfo chatInfo, Page page) throws UserNotLoggedInException {
        if (!TelegramLogin.isLoggedIn(page))
            throw new UserNotLoggedInException("Page not logged IN");
        page.navigate("http://example.com");
        String url = chatInfo.getUrl();
        page.navigate(url);

        if (StringUtils.equalsIgnoreCase(chatInfo.getType(), "PRIVATE")) {
            return parsePosts(page, chatInfo.getTitle());
        }

        return parsePosts(page);
    }

    public TelegramChatData getData(TelegramChat.TelegramChatInfo chatInfo, Page page) throws UserNotLoggedInException {
        if (!TelegramLogin.isLoggedIn(page))
            throw new UserNotLoggedInException("Page not logged IN");
        page.navigate("http://example.com");
        String url = chatInfo.getUrl();
        page.navigate(url);

        waitAndClick(page, "div.MiddleHeader div.info");

        return TelegramChatData.builder()
                .chatInfo(parseChatInf(page))
                .avatars(parseAvatars(page))
                .type(parseType(page))
                .publicLink(parsePublicLink(page))
                .posts(parsePosts(page))
                .subscribers(parseSubscribers(page))
                .build();
    }

    private Set<TelegramChatData.Subscriber> parseSubscribers(Page page) {
        Set<TelegramChatData.Subscriber> subscribers = new LinkedHashSet<>();
        ElementHandle elementHandle = page.querySelector("div.shared-media div.members-list");

        int prevCount = 0;

        for (int i = 0; i < 10000; i++) {
            List<ElementHandle> handles = tryWaitElements(elementHandle, "div.ListItem.chat-item-clickable.contact-list-item.scroll-item.small-icon");


            Set<TelegramChatData.Subscriber> collect = handles.stream().map(this::parseSubscriber).collect(Collectors.toSet());
            subscribers.addAll(collect);
            int size = subscribers.size();

            if (!handles.isEmpty()) {
                scrollToElement(handles.get(handles.size() - 1));
                List<ElementHandle> elementHandles = tryWaitElements(elementHandle, "div.ListItem.chat-item-clickable.contact-list-item.scroll-item.small-icon");
                scrollToElement(elementHandles.get(elementHandles.size() - 1));
            }


            if (size > prevCount)
                prevCount = size;
            else
                break;
        }
        return subscribers;
    }

    private TelegramChatData.Subscriber parseSubscriber(ElementHandle e) {
        TelegramChatData.Subscriber subscriber = TelegramChatData.Subscriber.builder()
                .url("")
                .peerId(e.querySelector("div.ChatInfo div.Avatar").getAttribute("data-peer-id"))
                .name(e.querySelector("h3").textContent())
                .build();
        return subscriber;
    }

    private String parsePublicLink(Page page) {
        ElementHandle elementHandle = page.querySelector("div.ChatExtra div.ListItem-button span.title");
        if (elementHandle != null)
            return elementHandle.textContent();
        return null;
    }

    private List<TelegramPost> parsePosts(Page page) {
        return parsePosts(page, null);
    }

    private List<TelegramPost> parsePosts(Page page, String title) {
        Set<TelegramPost> posts = new LinkedHashSet<>();
        waitIsVisible(page, "div.messages-container");
        ElementHandle elementHandle = page.querySelector("div.messages-container");

        int prevCount = 0;

        for (int i = 0; i < 10000; i++) {
            if (MAX_POST_COUNT <= prevCount)
                break;
            List<ElementHandle> dateGroup = tryWaitElements(elementHandle, "div.message-date-group");


            for (ElementHandle handle : dateGroup) {
                String date = handle.querySelector("div.sticky-date span").textContent();
                List<ElementHandle> postsElements = handle.querySelectorAll("div[id^='message']");

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
                ElementHandle elementHandle2 = elementHandle1.querySelectorAll("div[id^='message']").get(0);
                scrollToElement(elementHandle2);
                randomPause(5000, 7000);
                elementHandle2.evaluate("element => element.scrollTop = Math.max(0, element.scrollTop - 100);");
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
        ElementHandle elementHandle = e.querySelector("div.text-content");
        if (elementHandle != null) {
            String content = elementHandle.textContent();
            return StringUtils.substringBefore(content, time);
        }
        return null;
    }

    private List<MediaContent> parseMedia(ElementHandle e) {
        List<ElementHandle> elementHandles = e.querySelectorAll("div.content-inner div.media-inner");
        List<MediaContent> mediaContents = new ArrayList<>();
        for (ElementHandle elementHandle : elementHandles) {
            MediaContent mc = new MediaContent();
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

    private String parseType(Page page) {
        String info = page.querySelector("div.RightHeader h3").textContent();
        return StringUtils.substringBefore(info, " ");
    }

    private List<String> parseAvatars(Page page) {
        // TODO sometimes one of avatar is null
        var res = tryWaitElements(page, "div.profile-info div.ProfilePhoto img");
        return res.stream()
                .map(e -> e.getAttribute("src"))
                .toList();
    }

    private TelegramChat.TelegramChatInfo parseChatInf(Page page) {
        return null;
    }

    @Getter
    @Builder
    public static class TelegramChatData {

        private TelegramChat.TelegramChatInfo chatInfo;

        private String type;

        private String publicLink;

        private Set<Subscriber> subscribers;

        private List<String> avatars;

        private List<TelegramPost> posts;

        @Getter
        @Builder
        @EqualsAndHashCode(of = {"peerId"})
        static class Subscriber {

            private String url;

            private String peerId;

            private String name;

        }


    }

    @Getter
    @Setter
    @Builder
    @EqualsAndHashCode(of = {"messageId"})
    public static class TelegramPost {

        private Long messageId;

        private String date;
        private Instant parsedDate;

        private String time;

        private String textContent;

        private String sender;

        private String receiver;

        private List<MediaContent> media;

    }

    @Data
    public static class MediaContent {
        private String type;

        private String mediaId;
    }

}
