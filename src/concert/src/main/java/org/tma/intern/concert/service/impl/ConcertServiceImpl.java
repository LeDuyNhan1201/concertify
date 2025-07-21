package org.tma.intern.concert.service.impl;

import com.mongodb.client.model.Filters;
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
import org.tma.intern.common.exception.AppError;
import org.tma.intern.common.exception.HttpException;
import org.tma.intern.common.helper.StringHelper;
import org.tma.intern.common.helper.TimeHelper;
import org.tma.intern.common.type.Region;
import org.tma.intern.common.type.identity.IdentityGroup;
import org.tma.intern.concert.data.Concert;
import org.tma.intern.concert.dto.ConcertMapper;
import org.tma.intern.concert.dto.ConcertRequest;
import org.tma.intern.concert.dto.ConcertResponse;
import org.tma.intern.concert.repository.AuthRestClient;
import org.tma.intern.concert.repository.ConcertRepository;
import org.tma.intern.concert.service.ConcertService;
import org.tma.intern.concert.service.SeatService;

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

    AuthRestClient authRestClient;

    Faker faker;

    static public String ROLLBACK_FAILED_MESSAGE = "Rollback failed: could not {} {}";

    @Override
    public Uni<String> create(ConcertRequest.Info request) {
        Concert concert = concertMapper.toEntity(request);
        concert.setRegion(identityContext.getRegion());
        concert.setCreatedAt(Instant.now());
        concert.setCreatedBy(identityContext.getPrincipleName());
        return concertRepository.persist(concert).flatMap(this::generateSeatsOrRollback)
            .onFailure().transform(error ->
                new HttpException(AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED, error, "Create", "concert")
            );
    }

    @Override
    public Uni<String> update(String id, ConcertRequest.Info request) {
        return concertRepository.findById(StringHelper.safeParse(id))
            .onItem().ifNull().failWith(() ->
                new HttpException(AppError.RESOURCE_NOT_FOUND, Response.Status.NOT_FOUND, new NullPointerException(), "concert")
            ).invoke(concert -> checkOwner(concert.getCreatedBy()))
            .flatMap(existingConcert -> {
                existingConcert.setTitle(request.title());
                existingConcert.setLocation(request.location());
                existingConcert.setStartTime(TimeHelper.toInstant(request.startTime()));
                existingConcert.setEndTime(TimeHelper.toInstant(request.endTime()));
                updateAuditing(existingConcert);
                return concertRepository.persist(existingConcert).map(saved -> saved.getId().toHexString());
            }).onFailure().transform(error ->
                new HttpException(AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED, error, "Update", "concert")
            );
    }

    @Override
    public Uni<String> approve(String id) {
        return concertRepository.findById(StringHelper.safeParse(id))
            .onItem().ifNull().failWith(() ->
                new HttpException(AppError.RESOURCE_NOT_FOUND, Response.Status.NOT_FOUND, new NullPointerException(), "concert")
            ).flatMap(existingConcert -> {
                existingConcert.setApproved(true);
                updateAuditing(existingConcert);
                return concertRepository.persist(existingConcert).map(saved -> saved.getId().toHexString());
            }).onFailure().transform(error ->
                new HttpException(AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED, error, "Approve", "concert")
            );
    }

    @Override
    public Uni<String> softDelete(String id) {
        return concertRepository.findById(StringHelper.safeParse(id))
            .onItem().ifNull().failWith(() ->
                new HttpException(AppError.RESOURCE_NOT_FOUND, Response.Status.NOT_FOUND, new NullPointerException(), "concert")
            ).invoke(concert -> checkOwner(concert.getCreatedBy()))
            .flatMap(existingConcert -> {
                existingConcert.setDeleted(!existingConcert.isDeleted());
                updateAuditing(existingConcert);
                return concertRepository.persist(existingConcert).map(saved -> saved.getId().toHexString());
            }).onFailure().transform(error ->
                new HttpException(AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED, error, "Soft delete", "concert")
            );
    }

    @Override
    public Uni<ConcertResponse.Preview> findById(String id) {
        return concertRepository.findById(StringHelper.safeParse(id))
            .onItem().ifNull().failWith(() ->
                new HttpException(AppError.RESOURCE_NOT_FOUND, Response.Status.NOT_FOUND, new NullPointerException(), "Concert")
            ).invoke(concert -> checkRegion(concert.getRegion()))
            .map(concertMapper::toPreviewDto).onFailure().transform(error ->
                new HttpException(AppError.RESOURCE_NOT_FOUND, Response.Status.NOT_FOUND, error, "Concert")
            );
    }

    @Override
    public Uni<ConcertResponse.PreviewWithSeats> preview(String id) {
        return concertRepository.findById(StringHelper.safeParse(id))
            .onItem().ifNull().failWith(() ->
                new HttpException(AppError.RESOURCE_NOT_FOUND, Response.Status.NOT_FOUND, new NullPointerException(), "Concert")
            ).invoke(concert -> checkRegion(concert.getRegion()))
            .map(concertMapper::toPreviewWithSeatsDto).flatMap(detail ->
                seatService.findByConcertId(detail.getId()).invoke(detail::setSeats).replaceWith(detail)
            ).onFailure().transform(error ->
                new HttpException(AppError.RESOURCE_NOT_FOUND, Response.Status.NOT_FOUND, error, "Concert")
            );
    }

    @Override
    public Uni<ConcertResponse.DetailsWithSeats> details(String id) {
        return concertRepository.findById(StringHelper.safeParse(id))
            .onItem().ifNull().failWith(() ->
                new HttpException(AppError.RESOURCE_NOT_FOUND, Response.Status.NOT_FOUND, new NullPointerException(), "Concert")
            ).invoke(concert -> checkOwner(concert.getCreatedBy()))
            .map(concertMapper::toDetailsWithSeatsDto).flatMap(detail ->
                seatService.findByConcertId(detail.getId()).invoke(detail::setSeats).replaceWith(detail)
            ).onFailure().transform(error ->
                new HttpException(AppError.RESOURCE_NOT_FOUND, Response.Status.NOT_FOUND, error, "Concert")
            );
    }

    @Override
    public Uni<PageResponse<ConcertResponse.Preview>> search(int offset, int limit) {
        return Uni.combine().all().unis(
            concertRepository.find("deleted", false).page(Page.of(offset, limit)).list(),
            concertRepository.count("deleted", false)
        ).asTuple().map(tuple -> PageResponse.of(
            tuple.getItem1().stream().map(concertMapper::toPreviewDto).toList(), offset, limit, tuple.getItem2()
        ));
    }

    @Override
    public Uni<PageResponse<ConcertResponse.Preview>> myConcerts(int offset, int limit) {
        var query = Filters.and(
            Filters.eq("created_by", identityContext.getPrincipleName()),
            Filters.eq("deleted", false)
        );
        return Uni.combine().all().unis(
            concertRepository.find(query).page(Page.of(offset, limit)).list(),
            concertRepository.count(query)
        ).asTuple().map(tuple -> PageResponse.of(
            tuple.getItem1().stream().map(concertMapper::toPreviewDto).toList(), offset, limit, tuple.getItem2()
        ));
    }

    @Override
    public Uni<List<String>> seedData(int count, Region region) {
        return authRestClient.getAllUserEmail(IdentityGroup.ORGANIZERS, region)
            .map(userEmails -> {
                if (userEmails == null || userEmails.isEmpty())
                    throw new HttpException(AppError.RESOURCE_NOT_FOUND, Response.Status.NOT_FOUND, new NullPointerException("No user ids found"), "User ids");
                return IntStream.range(0, count).mapToObj(index ->
                    createRandomConcert(userEmails.get(faker.number().numberBetween(0, userEmails.size())), region)
                ).toList();
            }).flatMap(concerts -> concertRepository.persist(concerts)
                .onFailure().transform(error ->
                    new HttpException(AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED, error, "Seed", "concerts")
                ).replaceWith(concerts).flatMap(concertList ->
                    Multi.createFrom().iterable(concertList)
                        .onItem().transformToUniAndMerge(concert ->
                            seatService.generateForConcert(concert.getId()).onFailure().transform(error ->
                                new HttpException(AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED, error, "Create", "Seats")
                            ))
                        .collect().asList()
                        .replaceWith(concertList.stream().map(concert -> concert.getId().toHexString()).toList())
                )
            );
    }

    /* --------- Private methods for [CREATE] --------- */
    private Uni<String> generateSeatsOrRollback(Concert concert) {
        return seatService.generateForConcert(concert.getId()).replaceWith(concert.getId().toHexString())
            .onFailure().call(error -> concertRepository.deleteById(concert.getId())
                .onFailure().invoke(rollbackError ->
                    log.error("Rollback create seats Error: {}", rollbackError.getMessage(), rollbackError)
                )
                .invoke(isDeleted -> {
                    if (!isDeleted) log.warn(ROLLBACK_FAILED_MESSAGE, "delete", "created concert");
                })
            );
    }

    private Concert createRandomConcert(String userId, Region region) {
        Instant now = Instant.now();

        Instant startTime = now.plus(faker.number().numberBetween(1, 24 * 60 * 60), ChronoUnit.SECONDS);
        Instant endTime = startTime.plus(faker.number().numberBetween(1, 7 * 24 * 60 * 60), ChronoUnit.SECONDS);

        return Concert.builder()
            .id(new ObjectId())
            .title(faker.book().title())
            .location(faker.location().building())
            .region(region)
            .startTime(startTime)
            .endTime(endTime)
            .createdBy(userId)
            .build();
    }

    /* --------- Private methods for [UPDATE] --------- */
    public void updateAuditing(Concert concert) {
        concert.setUpdatedAt(Instant.now());
        concert.setUpdatedBy(identityContext.getPrincipleName());
    }

}
