package org.tma.intern.common.locale;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

@ApplicationScoped
//@RequestScoped
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class LocaleProvider {

    static HttpHeaders headers;

    public static Locale getLocale() {
        return headers.getAcceptableLanguages()
            .stream()
            .findFirst()
            .orElse(Locale.US);
    }

    public static String getTag() {
        log.info("[{}] Current tag: {}", LocaleProvider.class.getName(), getLocale());
        return getLocale().toLanguageTag();
    }

    public static String getCountry() {
        log.info("[{}] Current country: {}", LocaleProvider.class.getName(), getLocale());
        return getLocale().getCountry();
    }

    private static String getMessage(String key) {
        Locale locale = getLocale();
        ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
        return bundle.getString(key);
    }

    public static String getMessage(String key, Object... args) {
        try {
            return MessageFormat.format(getMessage(key), args);
        } catch (Exception exception) {
            log.error("Cannot get messages from bundle caused by {}", exception.getMessage());
            return key;
        }
    }

}