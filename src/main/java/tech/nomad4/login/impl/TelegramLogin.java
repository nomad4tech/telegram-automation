package tech.nomad4.login.impl;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.nomad4.login.decl.CodeProvider;
import tech.nomad4.login.decl.ContextProvider;

import static tech.nomad4.utils.CommonUtils.randomPause;
import static tech.nomad4.utils.PlaywrightUtils.*;


@Slf4j
public class TelegramLogin {
    public static final String URL = "https://web.telegram.org/";
    private final CodeProvider codeProvider;
    private final ContextProvider contextProvider;

    public TelegramLogin(TelegramLoginConfig config) {
        this.codeProvider = config.getCodeProvider();
        this.contextProvider = config.getContextProvider();
    }

    public Page login(String phone, Browser browser) {
        Page page = getBrowserContext(phone, browser).newPage();
        page.navigate(URL);

        if (isLoggedIn(page)) {
            log.info("{} is logged in by browser context", phone);
        } else {
            log.info("{} not logged in. start try to login", phone);
            if (!performLogin(page, phone)) {
                return page;
            }
        }

        contextProvider.updateContext(phone, page.context().storageState());
        return page;
    }

    private boolean performLogin(Page page, String phone) {
        waitAndClick(page, "button:has-text('Log in by phone Number')");

        // TODO rework for something predictable
        randomPause(1000, 5000);
        waitIsVisible(page, "#sign-in-phone-number");
        waitAndFill(page, "#sign-in-phone-number", phone);
        waitAndClick(page, "button[type='submit']");

        waitAndFill(page, "#sign-in-code", codeProvider.getCode(phone));

        boolean loggedIn = isLoggedIn(page);
        log.info("{} {} logged in", phone, loggedIn ? "SUCCESS" : "FAIL");
        return loggedIn;
    }

    public static boolean isLoggedIn(Page page) {
        return waitIsVisible(page, "#LeftColumn", 5000);
    }

    private BrowserContext getBrowserContext(String phone, Browser browser) {
        return contextProvider.getContext(phone)
                .map(context -> browser.newContext(new Browser.NewContextOptions().setStorageState(context)))
                .orElseGet(browser::newContext);
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TelegramLoginConfig {
        private CodeProvider codeProvider = new CodeProviderCLI();
        private ContextProvider contextProvider = new ContextProviderFile();
    }
}
