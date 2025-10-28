package org.tma.intern.common.exception.error;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.tma.intern.common.locale.LocaleProvider;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubError {

    String code;

    String messageKey;

    Object[] args;

    public SubError(String code, String messageKey) {
        this.code = code;
        this.messageKey = messageKey;
    }

    public String getMessage() {
        return LocaleProvider.getMessage(this.getMessageKey(), this.args);
    }

    public SubError args(Object... args) {
        this.args = args;
        return this;
    }

}
