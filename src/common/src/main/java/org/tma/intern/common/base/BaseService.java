package org.tma.intern.common.base;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.tma.intern.common.locale.LocaleProvider;
import org.tma.intern.common.security.IdentityContext;

@ApplicationScoped
@FieldDefaults(level = AccessLevel.PUBLIC)
@AllArgsConstructor
@NoArgsConstructor
public class BaseService {

    @Inject
    IdentityContext identityContext;

    @Inject
    LocaleProvider locale;

}