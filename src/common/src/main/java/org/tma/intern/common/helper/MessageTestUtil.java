package org.tma.intern.common.helper;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class MessageTestUtil {

    public static String getMessage(String key, Locale locale, Object... args) {
        ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
        String pattern = bundle.getString(key);
        return MessageFormat.format(pattern, args);
    }
}
