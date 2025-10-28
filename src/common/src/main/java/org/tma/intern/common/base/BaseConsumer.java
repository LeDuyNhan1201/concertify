package org.tma.intern.common.base;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.eclipse.microprofile.reactive.messaging.Metadata;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@FieldDefaults(level = AccessLevel.PROTECTED)
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseConsumer<TEvent> {

    protected abstract Uni<Void> consume(KafkaRecord<String, TEvent> record);

    protected <T> Uni<Void> assertActionFailWithRollback(
        Uni<T> uniAction,
        Uni<Void> sendRollbackAction,
        KafkaRecord<String, ?> record
    ) {
        return uniAction
            .chain(() -> Uni.createFrom().completionStage(record.ack()))
            .onFailure()
            .recoverWithUni(error -> {
                log.error("Failed to process event: {}", error.getMessage(), error);
                return sendRollbackAction.chain(() -> Uni.createFrom().completionStage(record.ack()));
            });
    }

    protected <T> Uni<Void> assertActionFailWithDeadLetter(
        Uni<T> uniAction,
        String deadLetterKey,
        KafkaRecord<String, ?> record
    ) {
        return uniAction
            .chain(() -> Uni.createFrom().completionStage(record.ack()))
            .onFailure()
            .recoverWithUni(error -> {
                log.error("Failed to rollback: {}", error.getMessage(), error);
                var metadata = OutgoingKafkaRecordMetadata.<String>builder()
                    .withKey(deadLetterKey + ":" + UUID.randomUUID())
                    .withHeaders(new RecordHeaders()
                        .add("error-type", deadLetterKey.getBytes(StandardCharsets.UTF_8))
                        .add("error-msg", error.getMessage().getBytes(StandardCharsets.UTF_8)))
                    .build();
                return Uni.createFrom().completionStage(record.nack(error, Metadata.of(metadata)));
            });
    }

    protected <AT, RB> Uni<AT> actionWithRollback(Uni<AT> uniAction, Uni<RB> uniRollbackAction) {
        return uniAction
            .onFailure().call(error ->
                handleRollbackFailure(uniRollbackAction).onItem().failWith(() -> {
                    throw new RuntimeException("Failed to rollback.", error);
                })
            );
    }

    protected <T> Uni<T> handleRollbackFailure(Uni<T> uniAction) {
        return uniAction
            .onFailure().invoke(rollbackError ->
                log.error("Error during rollback: {}", rollbackError.getMessage(), rollbackError)
            );
    }

}