package org.tma.intern.common.base;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.tma.intern.common.exception.AppError;
import org.tma.intern.common.exception.HttpException;
import org.tma.intern.common.locale.LocaleProvider;
import org.tma.intern.common.security.IdentityContext;

@FieldDefaults(level = AccessLevel.PROTECTED)
@AllArgsConstructor
@NoArgsConstructor
public class BaseResource {

    @Inject
    IdentityContext identityContext;

    @Inject
    LocaleProvider locale;

    protected void checkRegion() {
        if (!identityContext.getClaim("region").equals(locale.getRegion()))
            throw new HttpException(AppError.REGION_INVALID, Response.Status.FORBIDDEN, null);
    }

}
