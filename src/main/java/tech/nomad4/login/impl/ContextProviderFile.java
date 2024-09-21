package tech.nomad4.login.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import tech.nomad4.login.decl.ContextProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Slf4j
class ContextProviderFile implements ContextProvider {
    private static final String PATH_TEMPL = "browser_context" + File.separator + "%s.json";
    @Override
    public void updateContext(String phone, String context) {
        try {
            Path path = Path.of(PATH_TEMPL.formatted(phone));
            if (Files.notExists(path.getParent())) {
                Files.createDirectories(path.getParent());
                log.info("create directory: {}", path.toAbsolutePath());
            }
            Files.writeString(path, context);
            log.info("save browser context: {}", path.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public Optional<String> getContext(String phone) {
        Path path = Path.of(PATH_TEMPL.formatted(phone));
        try {
            if (Files.exists(path)) {
                log.info("get browser context from: {}", path.toAbsolutePath());
                String value = Files.readString(path);
                if (isValid(value))
                    return Optional.of(value);
                log.info("not valid browser context from: {}", path.toAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("browser context not exist: {}", path.toAbsolutePath());
        return Optional.empty();
    }
    private boolean isValid(String value) {
        // TODO add some validation for result
        return StringUtils.isNotBlank(value);
    }
}
