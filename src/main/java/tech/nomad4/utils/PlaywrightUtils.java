package tech.nomad4.utils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

import static tech.nomad4.utils.CommonUtils.randomPause;

public class PlaywrightUtils {

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

}
