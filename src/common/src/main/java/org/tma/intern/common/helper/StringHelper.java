package org.tma.intern.common.helper;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class StringHelper {

    public static String getMessage(String key, Locale locale, Object... args) {
        ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
        String pattern = bundle.getString(key);
        return MessageFormat.format(pattern, args);
    }

    public static String getLastSegment(String input, Character character) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        int lastDot = input.lastIndexOf(character);
        if (lastDot == -1 || lastDot == input.length() - 1) {
            return "";
        }
        return input.substring(lastDot + 1);
    }

    public static String uppercaseFirstChar(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

}
