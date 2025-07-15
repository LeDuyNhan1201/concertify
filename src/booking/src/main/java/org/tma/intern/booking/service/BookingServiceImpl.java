package org.tma.intern.booking.service;

import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.bson.types.ObjectId;
import org.tma.intern.booking.dto.BookingResponse;
import org.tma.intern.booking.entity.Booking;
import org.tma.intern.booking.repository.BookingRepository;
import org.tma.intern.booking.dto.BookingMapper;
import org.tma.intern.booking.dto.BookingRequest;
import org.tma.intern.common.base.BaseService;
import org.tma.intern.common.dto.BookingStatus;
import org.tma.intern.common.dto.PageResponse;
import org.tma.intern.common.exception.AppError;
import org.tma.intern.common.exception.HttpException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingServiceImpl extends BaseService implements BookingService {

    BookingItemService bookingItemService;

    BookingRepository bookingRepository;

    BookingMapper bookingMapper;

    Faker faker;

    @Override
    public Uni<List<String>> seedData(int count) {
//        List<Booking> bookings = IntStream.range(0, count)
//            .mapToObj(i -> createRandom())
//            .toList();
//
//        return bookingRepository.persist(bookings)
//            .replaceWith(bookings)
//            .flatMap(concertList ->
//                Multi.createFrom().iterable(concertList)
//                    .onItem().transformToUniAndMerge(concert ->
//                        bookingItemService.generateSeatsForConcert(concert.getId())
//                            .onFailure().call(throwable -> {
//                                log.error("Failed to generate seats for concert {}", concert.getId(), throwable);
//                                return bookingRepository.deleteById(concert.getId());
//                            })
//                            .replaceWith(Void.TYPE)
//                    )
//                    .collect().asList()
//                    .replaceWith(concertList.stream().map(c -> c.getId().toHexString()).toList())
//            )
//            .onFailure().transform(throwable -> new HttpException(AppError.ACTION_FAILED,
//                Response.Status.NOT_IMPLEMENTED, throwable, "Seed", "concerts"
//            ));
        return null;
    }

    @Override
    public Uni<String> create(BookingRequest.Body request) {
        return bookingRepository.persist(Booking.builder().createdBy(identityContext.getClaim("sub")).build())
            .flatMap(saved ->
                bookingItemService.create(saved.getId().toHexString(), request.items())
                    .replaceWith(saved.getId().toHexString())
                    .onFailure().call(throwable -> bookingRepository.deleteById(saved.getId())
                        .onItem().transform(result -> {
                            if (!result) {
                                log.error("Rollback create booking failed !!!");
                                throw new HttpException(AppError.ACTION_FAILED,
                                    Response.Status.NOT_IMPLEMENTED, null, "Delete", "booking"
                                );
                            }
                            return saved.getId();
                        }))
            )
            .onFailure().transform(throwable -> new HttpException(AppError.ACTION_FAILED,
                Response.Status.NOT_IMPLEMENTED, throwable, "Create", "booking"
            ));
    }

    @Override
    public Uni<String> update(String id, BookingRequest.Update request) {
        return findById(id).onItem().ifNotNull().transformToUni(booking ->
            bookingRepository.persist(Booking.builder()
                    .updatedAt(Instant.now())
                    .updatedBy(identityContext.getClaim("sub"))
                    .build())
                .flatMap(savedEntity ->
                    bookingItemService.create(savedEntity.getId().toHexString(), request.newItems())
                        .replaceWith(savedEntity.getId().toHexString())
                        .onFailure().transform(throwable -> new HttpException(AppError.ACTION_FAILED,
                            Response.Status.NOT_IMPLEMENTED, throwable, "Create", "booking item"
                        ))
                )
                .flatMap(existedEntityId -> bookingItemService.delete(request.deletedItems())
                    .onFailure().transform(throwable -> new HttpException(AppError.ACTION_FAILED,
                        Response.Status.NOT_IMPLEMENTED, throwable, "Delete", "booking item"
                    ))
                )
                .replaceWith(id)
                .onFailure().transform(throwable -> new HttpException(AppError.ACTION_FAILED,
                    Response.Status.NOT_IMPLEMENTED, throwable, "Update", "booking"
                )));
    }

    @Override
    public Uni<String> update(String id, BookingStatus status) {
        return findById(id).onItem().ifNotNull().transformToUni(concert ->
            bookingRepository.persist(Booking.builder()
                    .id(new ObjectId(concert.getId()))
                    .status(status)
                    .updatedAt(Instant.now())
                    .updatedBy(identityContext.getClaim("sub")).build())
                .onItem().transform(result -> result.getId().toHexString())
                .onFailure().transform(throwable -> new HttpException(
                    AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED, throwable, "Update", "booking")));
    }

    @Override
    public Uni<String> softDelete(String id) {
        return findById(id).onItem().ifNotNull().transformToUni(concert ->
            bookingRepository.persist(Booking.builder()
                    .id(new ObjectId(concert.getId()))
                    .isDeleted(true)
                    .updatedAt(Instant.now())
                    .updatedBy(identityContext.getClaim("sub")).build())
                .onItem().transform(result -> result.getId().toHexString())
                .onFailure().transform(throwable -> new HttpException(
                    AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED, throwable, "Soft delete", "booking")));
    }

    @Override
    public Uni<String> delete(String id) {
        return findById(id).onItem().ifNotNull().transformToUni(booking ->
            bookingRepository.deleteById(new ObjectId(id))
                .onItem().transform(result -> {
                    if (!result) throw new HttpException(AppError.ACTION_FAILED,
                        Response.Status.NOT_IMPLEMENTED, null, "Delete", "booking"
                    );
                    return id;
                })
                .onFailure().transform(throwable -> new HttpException(AppError.ACTION_FAILED,
                    Response.Status.NOT_IMPLEMENTED, throwable, "Delete", "booking"
                )));
    }

    @Override
    public Uni<BookingResponse.Detail> findById(String id) {
        return bookingRepository.findById(new ObjectId(id))
            .onItem().ifNull().failWith(() ->
                new HttpException(AppError.RESOURCE_NOT_FOUND,
                    Response.Status.NOT_FOUND, new NullPointerException(), "booking"))
            .flatMap(entity ->
                bookingItemService.findAllByBookingId(id)
                    .map(details -> {
                        BookingResponse.Detail dto = bookingMapper.toDto(entity);
                        dto.setItems(details);
                        return dto;
                    })
            )
            .onFailure().transform(throwable -> new HttpException(AppError.RESOURCE_NOT_FOUND,
                Response.Status.NOT_FOUND, throwable, "Booking"
            ));
    }

    @Override
    public Uni<PageResponse<BookingResponse.Detail>> findAll(int index, int limit) {
        return Uni.combine().all().unis(
                bookingRepository.find("is_deleted", false).page(Page.of(index, limit)).list(),
                bookingRepository.count()
            ).asTuple().flatMap(tuple -> {
                List<Booking> bookings = tuple.getItem1();
                Long total = tuple.getItem2();

                List<Uni<BookingResponse.Detail>> detailUnis = bookings.stream()
                    .map(booking -> bookingItemService.findAllByBookingId(booking.getId().toHexString())
                        .map(items -> {
                            BookingResponse.Detail dto = bookingMapper.toDto(booking);
                            dto.setItems(items);
                            return dto;
                        })
                    ).toList();

                return Uni.combine().all().unis(detailUnis).with(BookingServiceImpl::castDetails)
                    .map(details -> PageResponse.of(details, index, limit, total));
            })
            .onFailure().invoke(throwable -> log.error("Fail to fetch bookings !!!"))
            .replaceWith(PageResponse.of(new ArrayList<>(), index, limit, 0));
    }

    private static List<BookingResponse.Detail> castDetails(List<?> results) {
        return results.stream()
            .map(BookingResponse.Detail.class::cast)
            .toList();
    }

    private Booking createRandom() {
        return Booking.builder()
            .id(new ObjectId())
            .build();
    }

}
