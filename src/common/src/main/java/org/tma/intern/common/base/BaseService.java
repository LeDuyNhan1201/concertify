package org.tma.intern.common.base;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.tma.intern.common.exception.AppException;
import org.tma.intern.common.exception.AppError;
import org.tma.intern.common.locale.LocaleProvider;
import org.tma.intern.common.security.IdentityContext;
import org.tma.intern.common.type.Action;
import org.tma.intern.common.type.Region;

@ApplicationScoped
//@RequestScoped
@Slf4j
@FieldDefaults(level = AccessLevel.PROTECTED)
@AllArgsConstructor
@NoArgsConstructor
public class BaseService {

    @Inject
    IdentityContext identityContext;

    @Inject
    LocaleProvider locale;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class UniActionCombine<T> {

        Uni<T> uni;

        Action action;

        Class<?> entityType;

    }

    protected <T> Uni<T> assertActionFail(UniActionCombine<T> uniActionCombine) {
        return uniActionCombine.getUni()
            .onFailure()
            .transform(error ->
                actionFailed(
                    uniActionCombine.getAction(),
                    uniActionCombine.getEntityType(),
                    error
                )
            );
    }

    protected <T> Uni<T> assertNotFound(Uni<T> uni, Class<?> entityType) {
        return uni.onFailure().transform(error -> notFound(entityType, error));
    }

    protected <T> Uni<T> assertNotFoundAndReturn(Uni<T> uni, Class<?> entityType) {
        return uni.onItem()
            .ifNull()
            .failWith(() -> notFound(entityType, new NullPointerException()));
    }

    protected <TAction, TRollback> Uni<TAction> actionWithRollback(UniActionCombine<TAction> uniActionCombine, Uni<TRollback> uniRollbackAction) {
        return uniActionCombine.getUni()
            .onFailure().call(error ->
                handleRollbackFailure(
                    UniActionCombine.<TRollback>builder()
                        .uni(uniRollbackAction)
                        .action(uniActionCombine.getAction())
                        .entityType(uniActionCombine.getEntityType())
                        .build()
                ).replaceWith(() -> Uni.createFrom().failure(error))
            );
    }

    protected <T> Uni<T> handleRollbackFailure(UniActionCombine<T> uniActionCombine) {
        return uniActionCombine.getUni()
            .onFailure().invoke(rollbackError -> {
                log.warn("Rollback failed during {} on {}.",
                    uniActionCombine.getAction(),
                    uniActionCombine.getEntityType().getSimpleName()
                );
                log.error("Error during rollback: {}", rollbackError.getMessage(), rollbackError);
            });
    }

    protected <T> Uni<Void> commitSentEvent(Emitter<T> eventBus, Class<T> payloadType, T payload) {
        return Uni.createFrom().completionStage(() -> eventBus.send(payload))
            .invoke(() -> log.info("{} sent successfully!", payloadType.getSimpleName()));
    }

    protected AppException actionFailed(Action action, Class<?> entityType, Throwable error) {
        return new AppException(
            AppError.Failure.Action,
            error, Response.Status.NOT_IMPLEMENTED,
            action.message,
            entityType.getSimpleName()
        );
    }

    protected <T> AppException notFound(Class<T> entityType, Throwable error) {
        return new AppException(
            AppError.NotFound.Resource,
            error, Response.Status.NOT_FOUND,
            entityType.getSimpleName()
        );
    }

    protected void checkRegion(Region region) {
        if (!identityContext.getRegion().equals(region))
            throw new AppException(AppError.Invalid.Region, null, Response.Status.FORBIDDEN);
    }

    protected void checkOwner(String email) {
        if (!identityContext.getPrincipleName().equals(email))
            throw new AppException(AppError.Auth.NoPermission, null, Response.Status.FORBIDDEN);
    }

}