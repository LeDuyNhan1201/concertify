package org.tma.intern.common.base;

import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.tma.intern.common.exception.AppException;
import org.tma.intern.common.exception.AppError;
import org.tma.intern.common.locale.LocaleProvider;
import org.tma.intern.common.security.IdentityContext;
import org.tma.intern.common.type.Region;
import org.tma.intern.common.type.identity.IdentityRole;

import java.util.List;

@FieldDefaults(level = AccessLevel.PROTECTED)
@AllArgsConstructor
@NoArgsConstructor
public class BaseResource {

    @Inject
    IdentityContext identityContext;

    @Inject
    LocaleProvider locale;

    final String ROLE_GLOBAL_ADMIN = "global_admin";

    final String ROLE_CREATE_CONCERT = "concert:create";

    final String ROLE_UPDATE_CONCERT = "concert:update";

    final String ROLE_READ_CONCERT = "concert:read";

    final String ROLE_VIEW_CONCERT = "concert:view";

    final String ROLE_UPDATE_SEAT = "concert:seat:update";

    final String ROLE_CREATE_BOOKING = "booking:create";

    final String ROLE_UPDATE_BOOKING = "booking:update";

    final String ROLE_DELETE_BOOKING = "booking:delete";

    final String ROLE_READ_BOOKING = "booking:read";

    final String ROLE_VIEW_BOOKING = "booking:view";

    protected void hasRole(IdentityRole role) {
        Region currentRegion = identityContext.getRegion();
        if (!identityContext.hasAnyRole(
            List.of(
                String.format("%s_%s", role.prefix, currentRegion.country)
            )
        )) {
            throw new AppException(
                AppError.Invalid.Role,
                new ForbiddenException(),
                Response.Status.FORBIDDEN
            );
        }
    }

    protected void checkRegion(Region region) {
        if (!identityContext.getRegion().equals(region)) {
            throw new AppException(
                AppError.Invalid.Region,
                new ForbiddenException(),
                Response.Status.FORBIDDEN
            );
        }
    }

}
