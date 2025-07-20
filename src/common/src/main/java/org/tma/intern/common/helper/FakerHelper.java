package org.tma.intern.common.helper;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class FakerHelper {

    public static <T extends Enum<?>> T randomEnum(Class<T> enumClass) {
        Random random = new Random();
        int x = random.nextInt(enumClass.getEnumConstants().length);
        return enumClass.getEnumConstants()[x];
    }

    public static <T> T randomElement(List<T> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("List must not be null or empty");
        }
        int index = ThreadLocalRandom.current().nextInt(list.size());
        return list.get(index);
    }

}