package org.tma.intern.common.locale;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

@RequestScoped
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class LocaleProvider {

    HttpHeaders headers;

    public Locale getLocale() {
        return Locale.forLanguageTag(headers.getAcceptableLanguages()
                .stream().map(Locale::getLanguage)
                .findFirst()
                .orElse(Locale.getDefault().getLanguage()));
    }

    public String getRegion() {
        log.info("[{}] Current Locale: {}", LocaleProvider.class.getName(), getLocale());
        return getLocale().toLanguageTag();
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