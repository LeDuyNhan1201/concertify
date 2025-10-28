package org.tma.intern.common.exception.handler;

import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import jakarta.validation.ConstraintViolation;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.tma.intern.common.helper.StringHelper;
import org.tma.intern.common.locale.LocaleProvider;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Provider
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ValidationExceptionMapper implements ExceptionMapper<ResteasyReactiveViolationException> {

    @Override
    public Response toResponse(ResteasyReactiveViolationException exception) {
        List<Violation> violations = exception.getConstraintViolations()
            .stream()
            .map(this::toViolation)
            .collect(Collectors.toList());

        ValidationResponse error = new ValidationResponse(
            LocaleProvider.getMessage("Action.Fail", "Validate", "inputs"),
            violations
        );

        return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
    }

    private Violation toViolation(ConstraintViolation<?> violation) {
        String annotationType = violation.getConstraintDescriptor()
            .getAnnotation()
            .annotationType()
            .getSimpleName();

        String field = StringHelper.uppercaseFirstChar(
            StringHelper.getLastSegment(violation.getPropertyPath().toString(), '.')
        );

        Map<String, Object> attributes = violation.getConstraintDescriptor().getAttributes();
        String messageKey = violation.getMessage();
        String message;

        switch (annotationType) {
            case "Size" -> {
                String min = attributes.get("min").toString();
                String max = attributes.get("max").toString();
                message = LocaleProvider.getMessage(messageKey, field, min, max);

            } case "Min" -> {
                String min = attributes.get("min").toString();
                message = LocaleProvider.getMessage(messageKey, field, min);

            } case "Max" -> {
                String max = attributes.get("max").toString();
                message = LocaleProvider.getMessage(messageKey, field, max);

            } default -> message = LocaleProvider.getMessage(messageKey, field);
        }

        return new Violation(field, message);
    }


    public static class Violation {
        public String field;
        public String message;

        public Violation(String field, String message) {
            this.field = field;
            this.message = message;
        }
    }

    public static class ValidationResponse {
        public String message;
        public List<Violation> violations;

        public ValidationResponse(String message, List<Violation> violations) {
            this.message = message;
            this.violations = violations;
        }
    }

}
