package org.tma.intern.common.locale;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
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

    HttpHeaders headers;

    public Locale getLocale() {
        return headers.getAcceptableLanguages()
                .stream()
                .findFirst()
                .orElse(Locale.getDefault());
    }

    public String getTag() {
        log.info("[{}] Current tag: {}", LocaleProvider.class.getName(), getLocale());
        return getLocale().toLanguageTag();
    }

    public String getCountry() {
        log.info("[{}] Current country: {}", LocaleProvider.class.getName(), getLocale());
        return getLocale().getCountry();
    }

    public String getMessage(String key) {
        Locale locale = getLocale();
        ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
        return bundle.getString(key);
    }

    public String getMessage(String key, Object... args) {
        return MessageFormat.format(getMessage(key), args);
    }

}