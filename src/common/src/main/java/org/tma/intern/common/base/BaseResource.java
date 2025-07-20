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
    final String CREATE_ROLE = "concert:create";
    final String UPDATE_ROLE = "concert:edit";
    final String VIEW_ROLE = "concert:view";

    protected void hasOnlyRole(IdentityRole role) {
        Region currentRegion = identityContext.getRegion();
        if (!identityContext.hasAnyRole(List.of(
            String.format("%s_%s", role.prefix, currentRegion.country)
        ))) throw new HttpException(AppError.ROLE_INVALID, Response.Status.FORBIDDEN, null);
    }

    protected void checkRegion(Region region) {
        if (!identityContext.getRegion().equals(region))
            throw new HttpException(AppError.REGION_INVALID, Response.Status.FORBIDDEN, null);
    }

}
