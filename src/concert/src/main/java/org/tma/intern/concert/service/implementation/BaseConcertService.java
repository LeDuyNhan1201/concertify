package org.tma.intern.concert.service.implementation;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.tma.intern.common.base.BaseService;
import org.tma.intern.common.dto.PageResponse;
import org.tma.intern.common.exception.AppException;
import org.tma.intern.common.exception.AppError;
import org.tma.intern.common.helper.StringHelper;
import org.tma.intern.common.helper.TimeHelper;
import org.tma.intern.common.type.Action;
import org.tma.intern.common.type.Region;
import org.tma.intern.common.type.identity.IdentityGroup;
import org.tma.intern.concert.model.Concert;
import org.tma.intern.concert.model.Seat;
import org.tma.intern.concert.dto.ConcertMapper;
import org.tma.intern.concert.dto.ConcertRequest;
import org.tma.intern.concert.dto.ConcertResponse;
import org.tma.intern.concert.repository.AuthRestClient;
import org.tma.intern.concert.repository.ConcertRepository;
import org.tma.intern.concert.service.ConcertService;
import org.tma.intern.concert.service.SeatService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BaseConcertService extends BaseService implements ConcertService {

    @Inject
    SeatService seatService;

    @Inject
    ConcertRepository concertRepository;

    @Inject
    ConcertMapper concertMapper;

    @Inject
    AuthRestClient authRestClient;

    Faker faker;

    @Override
    public Uni<String> create(ConcertRequest.Info request) {
        Concert concert = concertMapper.toEntity(request);
        concert.setRegion(identityContext.getRegion());
        concert.setCreatedAt(Instant.now());
        concert.setCreatedBy(identityContext.getPrincipleName());

        var uniActionCombine = UniActionCombine.<String>builder()
            .uni(
                concertRepository.persist(concert)
                    .flatMap(this::generateSeatsOrRollback)
            )
            .action(Action.CREATE)
            .entityType(Concert.class)
            .build();
        return super.assertActionFail(uniActionCombine);
    }

    private Uni<String> generateSeatsOrRollback(Concert concert) {
        var uniActionCombine = UniActionCombine.<String>builder()
            .uni(
                seatService.generateForConcert(concert.getId())
                    .replaceWith(concert.getId().toHexString())
            )
            .action(Action.CREATE)
            .entityType(Seat.class)
            .build();

        return super.actionWithRollback(
            uniActionCombine,
            concertRepository.deleteById(concert.getId())
        );
    }

    @Override
    public Uni<String> update(String id, ConcertRequest.Info request) {
        var uniActionCombine = UniActionCombine.<String>builder()
            .uni(findByIdAndCheckOwner(id)
                .flatMap(existingConcert -> {
                        existingConcert.setTitle(request.title());
                        existingConcert.setLocation(request.location());
                        existingConcert.setStartTime(TimeHelper.toInstant(request.startTime()));
                        existingConcert.setEndTime(TimeHelper.toInstant(request.endTime()));

                        updateAuditing(existingConcert);
                        return concertRepository.persist(existingConcert)
                            .map(saved ->
                                saved.getId().toHexString()
                            );
                    }
                )
            )
            .action(Action.UPDATE)
            .entityType(Concert.class)
            .build();

        return super.assertActionFail(uniActionCombine);
    }

    @Override
    public Uni<String> approve(String id) {
        var uniActionCombine = UniActionCombine.<String>builder()
            .uni(super.assertNotFound(
                        concertRepository.findById(StringHelper.safeParse(id)),
                        Concert.class
                    )
                    .flatMap(existingConcert -> {
                            existingConcert.setApproved(true);
                            updateAuditing(existingConcert);

                            return concertRepository.persist(existingConcert)
                                .map(Concert::getIdHexString);
                        }
                    )
            )
            .action(Action.UPDATE)
            .entityType(Concert.class)
            .build();

        return super.assertActionFail(uniActionCombine);
    }

    @Override
    public Uni<String> softDelete(String id) {
        var uniActionCombine = UniActionCombine.<String>builder()
            .uni(findByIdAndCheckOwner(id)
                .flatMap(existingConcert -> {
                        existingConcert.setDeleted(!existingConcert.isDeleted());
                        updateAuditing(existingConcert);
                        return concertRepository.persist(existingConcert)
                            .map(saved ->
                                saved.getId().toHexString()
                            );
                    }
                )
            )
            .action(Action.UPDATE)
            .entityType(Concert.class)
            .build();

        return super.assertActionFail(uniActionCombine);
    }

    @Override
    public Uni<ConcertResponse.Preview> findById(String id) {
        return findByIdAndCheckRegion(id).map(concertMapper::toPreviewDto);
    }

    @Override
    public Uni<ConcertResponse.PreviewWithSeats> preview(String id) {
        return super.assertNotFound(
            findByIdAndCheckRegion(id)
                .map(concertMapper::toPreviewWithSeatsDto)
                .flatMap(detail ->
                    seatService.findByConcertId(detail.getId())
                        .invoke(detail::setSeats)
                        .replaceWith(detail)
                ),
            Concert.class
        );
    }

    @Override
    public Uni<ConcertResponse.DetailsWithSeats> details(String id) {
        return super.assertNotFound(
            findByIdAndCheckOwner(id)
                .map(concertMapper::toDetailsWithSeatsDto)
                .flatMap(detail ->
                    seatService.findByConcertId(detail.getId())
                        .invoke(detail::setSeats)
                        .replaceWith(detail)
                ),
            Concert.class
        );
    }

    @Override
    public Uni<PageResponse<ConcertResponse.Preview>> search(
        ConcertRequest.SearchQuery query,
        int offset,
        int limit,
        boolean isOrganizer
    ) {
        Bson filters = buildSearchFilters(query, isOrganizer);
        return Uni.combine().all().unis(
            concertRepository.find(
                    filters,
                    Indexes.ascending(Concert.FIELD_START_TIME)
                )
                .page(Page.of(offset, limit))
                .project(ConcertResponse.Preview.class).list(),
            concertRepository.count(filters)

        ).asTuple().map(tuple ->
            PageResponse.of(
                tuple.getItem1(),
                offset,
                limit,
                tuple.getItem2()
            )
        );
    }

    @Override
    public Uni<List<String>> seedData(int count, Region region) {
        return authRestClient.getAllUserEmail(IdentityGroup.ORGANIZERS, region)
            .map(userEmails -> {
                if (userEmails == null || userEmails.isEmpty())
                    throw new AppException(
                        AppError.NotFound.Resource,
                        new NullPointerException("No user ids found"), Response.Status.NOT_FOUND,
                        "User ids"
                    );
                return IntStream.range(0, count).mapToObj(index ->
                    createRandomConcert(userEmails.get(faker.number().numberBetween(0, userEmails.size())), region)
                ).toList();
            }).flatMap(concerts -> concertRepository.persist(concerts)
                .onFailure().transform(error ->
                    new AppException(
                        AppError.Failure.Action,
                        error, Response.Status.NOT_IMPLEMENTED,
                        Action.CREATE.message,
                        Concert.class.getSimpleName()
                    )
                ).replaceWith(concerts).flatMap(concertList ->
                    Multi.createFrom().iterable(concertList)
                        .onItem().transformToUniAndMerge(concert ->
                            seatService.generateForConcert(
                                concert.getId()
                            ).onFailure().transform(error ->
                                new AppException(
                                    AppError.Failure.Action,
                                    error, Response.Status.NOT_IMPLEMENTED,
                                    Action.CREATE.message,
                                    Concert.class.getSimpleName()
                                )
                            )
                        )
                        .collect().asList()
                        .replaceWith(concertList.stream().map(
                            concert -> concert.getId().toHexString()
                        ).toList())
                )
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

    /* --------- Private methods for [READ] --------- */
    private Uni<Concert> findByIdAndCheckOwner(String id) {
        return super.assertNotFound(
            concertRepository.findById(StringHelper.safeParse(id)),
            Concert.class
        ).invoke(concert -> checkOwner(concert.getCreatedBy()));
    }

    private Uni<Concert> findByIdAndCheckRegion(String id) {
        return super.assertNotFound(
            concertRepository.findById(StringHelper.safeParse(id)),
            Concert.class
        ).invoke(concert -> checkRegion(concert.getRegion()));
    }

    public Bson buildSearchFilters(ConcertRequest.SearchQuery query, boolean isMine) {
        List<Bson> filters = new ArrayList<>();

        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            filters.add(Filters.or(
                Filters.regex(Concert.FIELD_TITLE, query.getKeyword(), "i"),
                Filters.regex(Concert.FIELD_LOCATION, query.getKeyword(), "i")
            ));
        }

        if (query.getFrom() != null) {
            filters.add(Filters.gte(Concert.FIELD_START_TIME, query.getFrom()));
        }

        if (query.getTo() != null) {
            filters.add(Filters.lte(Concert.FIELD_START_TIME, query.getTo()));
        }

        filters.add(Filters.eq(Concert.FIELD_IS_DELETED, false));
        filters.add(Filters.eq(Concert.FIELD_REGION, identityContext.getRegion()));

        if (isMine) {
            filters.add(Filters.eq(Concert.FIELD_CREATED_BY, identityContext.getPrincipleName()));
        }

        return Filters.and(filters);
    }

}
