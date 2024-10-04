package tech.nomad4.utils;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.List;

import static tech.nomad4.utils.CommonUtils.randomPause;

public class PlaywrightUtils {

    public final static String EXAMPLE_URL = "http://example.com";

    private final static Integer TIMEOUT = 30_000;

    private PlaywrightUtils() { /* Prevent instantiation */ }

    public static boolean waitIsVisible(Page page, String selector, int timeout) {
        Locator elementLocator = page.locator(selector);
        try {
            elementLocator.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(timeout));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean waitIsVisible(Page page, String selector) {
        return waitIsVisible(page, selector, TIMEOUT);
    }

    public static boolean waitIsDetached(Page page, String selector, int timeout) {
        Locator elementLocator = page.locator(selector);
        try {
            elementLocator.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.DETACHED)
                    .setTimeout(timeout));
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public static boolean waitIsDetached(Page page, String selector) {
        return waitIsDetached(page, selector, TIMEOUT);
    }

    public static void waitAndClick(Page page, String selector, int timeout) {
        Locator elementLocator = page.locator(selector);

        try {
            elementLocator.waitFor(new Locator.WaitForOptions().setTimeout(timeout));
        } catch (Exception e) {
            // TODO mb it makes sense to return boolean value for more flexible event handling
            throw new RuntimeException(e);
        }

        elementLocator.click();
        randomPause(500, 1000);
    }

    public static void waitAndClick(Page page, String selector) {
        waitAndClick(page, selector, TIMEOUT);
    }

    public static void waitAndFill(Page page, String selector, String content, int timeout) {
        Locator elementLocator = page.locator(selector);

        try {
            elementLocator.waitFor(new Locator.WaitForOptions().setTimeout(timeout));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        elementLocator.fill(content);
        randomPause(500, 1000);
    }

    public static void waitAndFill(Page page, String selector, String content) {
        waitAndFill(page, selector, content, TIMEOUT);
    }

    public static List<ElementHandle> tryWaitElements(ElementHandle elementHandle,
                                               String selector,
                                               int timeout,
                                               int minCount) {
        long startTime = System.currentTimeMillis();
        List<ElementHandle> handles;
        while (true) {
            handles = elementHandle.querySelectorAll(selector);
            if (handles.size() >= minCount) {
                return handles;
            }
            if (System.currentTimeMillis() - startTime > timeout) {
                return handles;
            }
            randomPause(50, 100);
        }
    }

    public static List<ElementHandle> tryWaitElements(ElementHandle elementHandle,
                                               String selector,
                                               int timeout) {
        return tryWaitElements(elementHandle, selector, timeout, 1);
    }

    public static List<ElementHandle> tryWaitElements(ElementHandle elementHandle,
                                                      String selector) {
        return tryWaitElements(elementHandle, selector, TIMEOUT, 1);
    }

     public static List<ElementHandle> tryWaitElements(Page page,
                                               String selector,
                                               int timeout,
                                               int minCount) {
        long startTime = System.currentTimeMillis();
        List<ElementHandle> handles;
        while (true) {
            handles = page.querySelectorAll(selector);
            if (handles.size() >= minCount) {
                return handles;
            }
            if (System.currentTimeMillis() - startTime > timeout) {
                return handles;
            }
            randomPause(50, 100);
        }
    }

    public static List<ElementHandle> tryWaitElements(Page page,
                                               String selector,
                                               int timeout) {
        return tryWaitElements(page, selector, timeout, 1);
    }

    public static List<ElementHandle> tryWaitElements(Page page,
                                                      String selector) {
        return tryWaitElements(page, selector, TIMEOUT, 1);
    }

    public static void scrollToElement(ElementHandle elementHandle) {
        elementHandle.evaluate("element => element.scrollIntoView({ behavior: 'smooth', block: 'center' });");
    }

    public static String waitGetContentOrNull(Page page, String selector, int timeout) {
        Locator elementLocator = page.locator(selector);
        try {
            elementLocator.waitFor(new Locator.WaitForOptions().setTimeout(timeout));
            return elementLocator.textContent();
        } catch (Exception e) {
            return null;
        }
    }

    public static ElementHandle waitOrNull(Page page, String selector, int timeout) {
        waitIsVisible(page, selector, timeout);
        return page.querySelector(selector);
    }

    public static ElementHandle waitOrNull(Page page, String selector) {
        return waitOrNull(page, selector, TIMEOUT);
    }

    public static void scrollTop(ElementHandle handle, int pixels) {
        handle.evaluate("element => element.scrollTop -= " + pixels + ";");
    }

    public static void scrollDown(ElementHandle handle, int pixels) {
        handle.evaluate("element => element.scrollTop += " + pixels + ";");
    }

}
