package org.tma.intern.common.base;

import jakarta.enterprise.context.ApplicationScoped;
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
import org.tma.intern.common.type.Region;

@ApplicationScoped
@FieldDefaults(level = AccessLevel.PUBLIC)
@AllArgsConstructor
@NoArgsConstructor
public class BaseService {

    @Inject
    IdentityContext identityContext;

    @Inject
    LocaleProvider locale;

    protected void checkRegion(Region region) {
        if (!identityContext.getRegion().equals(region))
            throw new HttpException(AppError.REGION_INVALID, Response.Status.FORBIDDEN, null);
    }

    protected void checkOwner(String id) {
        if (!identityContext.getClaim("sub").equals(id))
            throw new HttpException(AppError.CANNOT_ACCESS, Response.Status.FORBIDDEN, null);
    }

}