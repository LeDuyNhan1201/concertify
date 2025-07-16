package org.tma.intern.concert.service;

import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.bson.types.ObjectId;
import org.tma.intern.common.base.BaseService;
import org.tma.intern.common.dto.PageResponse;
import org.tma.intern.common.type.Region;
import org.tma.intern.common.exception.AppError;
import org.tma.intern.common.exception.HttpException;
import org.tma.intern.concert.data.Concert;
import org.tma.intern.concert.data.ConcertRepository;
import org.tma.intern.concert.dto.ConcertMapper;
import org.tma.intern.concert.dto.ConcertRequest;
import org.tma.intern.concert.dto.ConcertResponse;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.IntStream;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConcertServiceImpl extends BaseService implements ConcertService {

    SeatService seatService;

    ConcertRepository concertRepository;

    ConcertMapper concertMapper;

    Faker faker;

    @Override
    public Uni<List<String>> seedData(int count) {
        List<Concert> concerts = IntStream.range(0, count)
            .mapToObj(i -> createRandomConcert())
            .toList();

        return concertRepository.persist(concerts)
            .replaceWith(concerts)
            .flatMap(concertList ->
                    Multi.createFrom().iterable(concertList)
                        .onItem().transformToUniAndMerge(concert ->
                                seatService.generateSeatsForConcert(concert.getId())
//                            .onItem().invoke(seatIds ->
//                                seatIds.forEach(id ->
//                                    log.info("Seat {} is created.", id)
//                                )
//                            )
                                    .onFailure().call(throwable -> {
                                        log.error("Failed to generate seats for concert {}", concert.getId(), throwable);
                                        return concertRepository.deleteById(concert.getId());
                                    })
                                    .replaceWith(Void.TYPE)
                        )
                        .collect().asList()
                        .replaceWith(concertList.stream().map(c -> c.getId().toHexString()).toList())
            )
            .onFailure().transform(throwable -> new HttpException(AppError.ACTION_FAILED,
                Response.Status.NOT_IMPLEMENTED, throwable, "Seed", "concerts"
            ));
    }

    @Override
    public Uni<String> create(ConcertRequest.Body request) {
        Concert entity = concertMapper.toEntity(request);
        entity.setRegion(Region.valueOf(locale.getCountry()));
        entity.setCreatedBy(identityContext.getClaim("sub"));

        return concertRepository.persist(entity)
            .flatMap(saved ->
                seatService.generateSeatsForConcert(saved.getId())
                    .replaceWith(saved.getId().toHexString())
                    .onFailure().call(throwable ->
                        concertRepository.deleteById(saved.getId()).onItem().transform(result -> {
                            if (!result) {
                                log.error("Rollback create concert failed !!!");
                                throw new HttpException(AppError.ACTION_FAILED,
                                    Response.Status.NOT_IMPLEMENTED, null, "Delete", "concert"
                                );
                            }
                            return saved.getId();
                        }))
            )
            .onFailure().transform(throwable -> new HttpException(AppError.ACTION_FAILED,
                Response.Status.NOT_IMPLEMENTED, throwable, "Create", "concert"
            ));
    }

    @Override
    public Uni<String> update(String id, ConcertRequest.Body request) {
        return findById(id).onItem().ifNotNull().transformToUni(concert -> {
            Concert entity = concertMapper.toEntity(request);
            entity.setUpdatedAt(Instant.now());
            entity.setUpdatedBy(identityContext.getClaim("sub"));
            return concertRepository.persist(concertMapper.toEntity(request))
                .onItem().transform(result -> result.getId().toHexString())
                .onFailure().transform(throwable -> new HttpException(
                    AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED, throwable, "Update", "concert"));
        });
    }

    @Override
    public Uni<String> approve(String id) {
        return findById(id).onItem().ifNotNull().transformToUni(concert ->
            concertRepository.persist(Concert.builder().id(new ObjectId(concert.getId()))
                    .isApproved(true)
                    .updatedAt(Instant.now())
                    .updatedBy(identityContext.getClaim("sub")).build())
                .onItem().transform(result -> result.getId().toHexString())
                .onFailure().transform(throwable -> new HttpException(
                    AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED, throwable, "Approve", "concert")));
    }

    @Override
    public Uni<String> softDelete(String id) {
        return findById(id).onItem().ifNotNull().transformToUni(concert ->
            concertRepository.persist(Concert.builder()
                    .id(new ObjectId(concert.getId()))
                    .isDeleted(true)
                    .updatedAt(Instant.now())
                    .updatedBy(identityContext.getClaim("sub")).build())
                .onItem().transform(result -> result.getId().toHexString())
                .onFailure().transform(throwable -> new HttpException(
                    AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED, throwable, "Soft delete", "concert")));
    }

    @Override
    public Uni<ConcertResponse.Detail> findById(String id) {
        return concertRepository.findById(new ObjectId(id))
            .onItem().ifNotNull().transform(concertMapper::toDetailDto)
            .onItem().ifNotNull().transformToUni(detail ->
                seatService.findByConcertId(detail.getId())
                    .invoke(detail::setSeats)
                    .replaceWith(detail))
            .onFailure().recoverWithUni(Uni.createFrom().failure(
                new HttpException(AppError.RESOURCE_NOT_FOUND,
                    Response.Status.NOT_FOUND, new NullPointerException(), "concert")));
    }

    @Override
    public Uni<PageResponse<ConcertResponse.Preview>> findAll(int index, int limit) {
        return Uni.combine().all().unis(
            concertRepository.find("is_deleted", false).page(Page.of(index, limit)).list(),
            concertRepository.count()).asTuple().map(tuple ->
            PageResponse.of(
                tuple.getItem1().stream().map(concertMapper::toPreviewDto).toList(),
                index, limit, tuple.getItem2()));
    }

    private Concert createRandomConcert() {
        Instant now = Instant.now();

        Instant startTime = now.plus(faker.number().numberBetween(1, 24 * 60 * 60), ChronoUnit.SECONDS);
        Instant endTime = startTime.plus(faker.number().numberBetween(1, 7 * 24 * 60 * 60), ChronoUnit.SECONDS);

        return Concert.builder()
            .id(new ObjectId())
            .title(faker.book().title())
            .location(faker.location().building())
            .region(Region.valueOf(faker.languageCode().iso639().toUpperCase()))
            .startTime(startTime)
            .endTime(endTime)
            .build();
    }

}
