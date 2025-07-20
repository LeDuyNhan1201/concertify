package org.tma.intern.booking.service.impl;

import com.mongodb.client.model.Filters;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.tma.intern.booking.dto.request.BookingItemRequest;
import org.tma.intern.booking.dto.request.BookingRequest;
import org.tma.intern.booking.dto.response.BookingItemResponse;
import org.tma.intern.booking.dto.response.BookingResponse;
import org.tma.intern.booking.entity.Booking;
import org.tma.intern.booking.entity.BookingItem;
import org.tma.intern.booking.mapper.BookingItemMapper;
import org.tma.intern.booking.mapper.BookingMapper;
import org.tma.intern.booking.repository.BookingRepository;
import org.tma.intern.booking.service.BookingItemService;
import org.tma.intern.booking.service.BookingService;
import org.tma.intern.common.base.BaseService;
import org.tma.intern.common.contract.event.*;
import org.tma.intern.common.dto.PageResponse;
import org.tma.intern.common.exception.AppError;
import org.tma.intern.common.exception.HttpException;
import org.tma.intern.common.helper.StringHelper;
import org.tma.intern.common.type.BookingStatus;

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

    BookingItemMapper bookingItemMapper;

    @NonFinal
    @Inject
    @Channel("booking.updated-out")
    private Emitter<BookingUpdated> bookingUpdatedEventBus;

    @NonFinal
    @Inject
    @Channel("booking.deleted-out")
    private Emitter<BookingDeleted> bookingDeletedEventBus;

    Faker faker;

    static public String ROLLBACK_FAILED_MESSAGE = "Rollback failed: could not {} {}";

//    @Override
//    public Uni<String> create(BookingRequest.Body request) {
//        Booking booking = bookingMapper.toEntity(request);
//        booking.setCreatedAt(Instant.now());
//        booking.setCreatedBy(identityContext.getPrincipleName());
//
//        List<BookingItem> bookingItems = request.items().stream().distinct().map(bookingItemMapper::toEntity).toList();
//        return bookingItemService.isAnyExisted(request.items().stream().map(BookingItemRequest.Body::seatId).toList())
//            .flatMap(isExisted -> {
//                if (isExisted) throw new HttpException(AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED,
//                    new IllegalAccessException("Any item is already existed !!!"), "Create", "booking");
//
//                return bookingRepository.persist(booking)
//                    .flatMap(createdBooking -> bookingItemService.create(createdBooking.getId(), bookingItems)
//                        .onFailure().call(createdError -> rollbackCreateBooking(createdBooking.getId(), createdError))
//                        .replaceWith(createdBooking.getId().toHexString())
//                    );
//            });
//
//    }

    @Override
    public Uni<String> create(BookingCreated event) {
        Booking booking = Booking.builder()
            .concertId(event.getConcertId())
            .concertOwnerId(event.getConcertOwnerId())
            .createdAt(Instant.now())
            .createdBy(event.getCreatedBy())
            .build();

        List<BookingItem> bookingItems = event.getItems().stream().distinct()
            .map(bookingItemCreated -> BookingItem.builder()
                .seatId(bookingItemCreated.getSeatId())
                .seatCode(bookingItemCreated.getSeatCode())
                .price(bookingItemCreated.getPrice())
                .build()
            ).toList();
        return bookingItemService.isAnyExisted(event.getItems().stream().map(BookingItemCreated::getSeatId).toList())
            .flatMap(isExisted -> {
                if (isExisted) throw new RuntimeException("Any item is already existed !!!");

                return bookingRepository.persist(booking)
                    .flatMap(createdBooking -> bookingItemService.create(createdBooking.getId(), bookingItems)
                        .onFailure().call(createdError -> rollbackCreateBooking(createdBooking.getId(), createdError))
                        .replaceWith(createdBooking.getId().toHexString())
                    );
            });

    }

    @Override
    public Uni<String> update(String id, BookingRequest.Update request) {
        return bookingRepository.findById(new ObjectId(id))
            .onItem().ifNull().failWith(() ->
                new HttpException(AppError.RESOURCE_NOT_FOUND, Response.Status.NOT_FOUND, new NullPointerException(), "booking")
            ).invoke(booking -> checkOwner(booking.getCreatedBy()))
            .flatMap(existingBooking -> {
                // Step 1: Create new booking items
                return createNewItems(existingBooking, request).flatMap(createdItemIds ->
                    // Step 2: Delete requested items
                    deleteOldItems(request, createdItemIds).chain(deletedItems -> {
                        updateAuditing(existingBooking);
                        return bookingRepository.update(existingBooking)
                            .chain(() -> sendBookingUpdatedEvent(
                                existingBooking.getConcertId(),
                                deletedItems.stream().map(BookingItem::getSeatId).toList(),
                                request.newItems().stream().map(BookingItemRequest.Body::seatId).toList())
                            ).replaceWith(id)
                            .onFailure().call(updateFailure ->
                                rollbackUpdate(existingBooking.getId(), deletedItems, createdItemIds, updateFailure)
                            );
                    })
                );
            });
    }

    @Override
    public Uni<String> update(String id, BookingStatus status) {
        return bookingRepository.findById(StringHelper.safeParse(id))
            .onItem().ifNull().failWith(() ->
                new HttpException(AppError.RESOURCE_NOT_FOUND, Response.Status.NOT_FOUND, new NullPointerException(), "booking")
            ).invoke(concert -> checkOwner(concert.getConcertOwnerId()))
            .flatMap(existingBooking -> {
                existingBooking.setStatus(status);
                updateAuditing(existingBooking);
                return bookingRepository.persist(existingBooking).map(saved -> saved.getId().toHexString());
            }).onFailure().transform(error ->
                new HttpException(AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED, error, "Update", "booking stats")
            );
    }

    @Override
    public Uni<String> softDelete(String id) {
        return bookingRepository.findById(StringHelper.safeParse(id))
            .onItem().ifNull().failWith(() ->
                new HttpException(AppError.RESOURCE_NOT_FOUND, Response.Status.NOT_FOUND, new NullPointerException(), "booking")
            ).invoke(concert -> checkOwner(concert.getCreatedBy()))
            .flatMap(existingBooking -> {
                existingBooking.setDeleted(!existingBooking.isDeleted());
                updateAuditing(existingBooking);
                return bookingRepository.persist(existingBooking).map(saved -> saved.getId().toHexString());
            }).onFailure().transform(error ->
                new HttpException(AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED, error, "Soft delete", "booking")
            );
    }

    @Override
    public Uni<String> delete(String id) {
        return bookingRepository.findById(StringHelper.safeParse(id))
            .onItem().ifNull().failWith(() ->
                new HttpException(AppError.RESOURCE_NOT_FOUND, Response.Status.NOT_FOUND, null, "booking")
            ).invoke(concert -> checkOwner(concert.getCreatedBy()))
            .flatMap(booking -> deleteBookingAndItems(StringHelper.safeParse(id), booking));
    }

    @Override
    public Uni<BookingResponse.Details> details(String id) {
        return bookingRepository.findById(new ObjectId(id))
            .onItem().ifNull().failWith(() ->
                new HttpException(AppError.RESOURCE_NOT_FOUND, Response.Status.NOT_FOUND, new NullPointerException(), "booking")
            ).invoke(booking -> checkOwner(booking.getConcertOwnerId()))
            .map(bookingMapper::toDetailsDto).flatMap(dto ->
                bookingItemService.findAllByBookingId(id).invoke(dto::setItems).replaceWith(dto)
            ).onFailure().transform(error ->
                new HttpException(AppError.RESOURCE_NOT_FOUND, Response.Status.NOT_FOUND, error, "Booking")
            );
    }

    @Override
    public Uni<BookingResponse.Preview> preview(String id) {
        return bookingRepository.findById(new ObjectId(id))
            .onItem().ifNull().failWith(() ->
                new HttpException(AppError.RESOURCE_NOT_FOUND, Response.Status.NOT_FOUND, new NullPointerException(), "booking")
            ).invoke(booking -> checkOwner(booking.getConcertOwnerId()))
            .map(bookingMapper::toPreviewDto).flatMap(dto ->
                bookingItemService.findAllByBookingId(id).invoke(dto::setItems).replaceWith(dto)
            ).onFailure().transform(error ->
                new HttpException(AppError.RESOURCE_NOT_FOUND, Response.Status.NOT_FOUND, error, "Booking")
            );
    }

    @Override
    public Uni<PageResponse<BookingResponse.Details>> search(int index, int limit) {
        return Uni.combine().all().unis(
            bookingRepository.find("is_deleted", false).page(Page.of(index, limit)).list(),
            bookingRepository.count("is_deleted", false)
        ).asTuple().flatMap(tuple -> {
            List<Booking> bookings = tuple.getItem1();
            Long total = tuple.getItem2();
            return toDetailResponses(bookings).map(details -> PageResponse.of(details, index, limit, total));
        }).replaceWith(PageResponse.of(new ArrayList<>(), index, limit, 0));
    }

    @Override
    public Uni<PageResponse<BookingResponse.Details>> bookingsOfMyConcerts(int index, int limit) {
        var query = Filters.and(
            Filters.eq("concert_owner_id", identityContext.getPrincipleName()),
            Filters.eq("deleted", false)
        );
        return Uni.combine().all().unis(
            bookingRepository.find(query).page(Page.of(index, limit)).list(),
            bookingRepository.count(query)
        ).asTuple().flatMap(tuple -> {
            List<Booking> bookings = tuple.getItem1();
            Long total = tuple.getItem2();
            return toDetailResponses(bookings).map(details -> PageResponse.of(details, index, limit, total));
        }).replaceWith(PageResponse.of(new ArrayList<>(), index, limit, 0));
    }

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

    /* --------- Private methods for [CREATE] --------- */
    private Uni<Void> rollbackCreateBooking(ObjectId bookingId, Throwable createdError) {
        return bookingRepository.deleteById(bookingId)
            .onItem().invoke(isDeleted -> {
                if (!isDeleted) log.warn(ROLLBACK_FAILED_MESSAGE, "delete", "created booking");
            }).onFailure().invoke(rollbackError ->
                log.error("Error: {}", rollbackError.getMessage(), rollbackError)
            ).replaceWithVoid().invoke(() -> {
                throw new HttpException(AppError.ACTION_FAILED,
                    Response.Status.NOT_IMPLEMENTED, createdError, "Create", "booking");
            });
    }

    private Booking createRandom() {
        return Booking.builder()
            .id(new ObjectId())
            .build();
    }

    /* --------- Private methods for [UPDATE] --------- */
    private Uni<Void> sendBookingUpdatedEvent(String concertId, List<String> oldItems, List<String> newItems) {
        BookingUpdated event = BookingUpdated.newBuilder()
            .setConcertId(concertId)
            .setOldItems(oldItems.stream().map(seatId -> BookingItemChanged.newBuilder()
                .setSeatId(seatId)
                .build()).toList())
            .setNewItems(newItems.stream().map(seatId -> BookingItemChanged.newBuilder()
                .setSeatId(seatId)
                .build()).toList())
            .build();

        return Uni.createFrom().completionStage(() -> bookingUpdatedEventBus.send(event))
            .invoke(() -> log.info("Booking updated event sent successfully!"));
    }

    private HttpException wrapHttpException(Throwable cause, String action, String target) {
        return (cause instanceof HttpException httpEx) ? httpEx
            : new HttpException(AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED, cause, action, target);
    }

    private Uni<List<String>> createNewItems(Booking booking, BookingRequest.Update request) {
        List<BookingItem> newItems = request.newItems().stream().distinct().map(bookingItemMapper::toEntity).toList();
        return bookingItemService.create(booking.getId(), newItems)
            .onFailure().transform(error -> wrapHttpException(error, "Create", "booking items"));
    }

    private Uni<List<BookingItem>> deleteOldItems(BookingRequest.Update request, List<String> createdItemIds) {
        return bookingItemService.delete(request.oldItems())
            .onFailure().call(deleteError -> rollbackCreateItems(createdItemIds)
                .onItem().failWith(() -> {
                    throw wrapHttpException(deleteError, "Delete", "booking items");
                })
            ).flatMap(deletedIds -> bookingItemService.findByIds(deletedIds).collect().asList()
                .invoke(items ->
                    items.forEach(item -> log.info("Deleted booking item: {}", item.getId()))
                )
            );
    }

    private Uni<Void> rollbackCreateItems(List<String> createdItemIds) {
        return bookingItemService.delete(createdItemIds)
            .onFailure().invoke(rollbackError -> {
                log.warn(ROLLBACK_FAILED_MESSAGE, "delete", "created booking items");
                log.error("Rollback create items Error: {}", rollbackError.getMessage(), rollbackError);
            }).replaceWithVoid();
    }

    private Uni<Void> rollbackUpdate(ObjectId bookingId, List<BookingItem> deletedItems, List<String> createdItemIds, Throwable updateFailure) {
        return bookingItemService.delete(createdItemIds)
            .onFailure().invoke(rollbackError -> {
                log.warn(ROLLBACK_FAILED_MESSAGE, "delete", "created booking items");
                log.error("Rollback create items Error: {}", rollbackError.getMessage(), rollbackError);
            }).chain(() -> bookingItemService.create(bookingId, deletedItems)
                .onItem().failWith(() -> {
                    throw wrapHttpException(updateFailure, "Update", "booking");
                })
                .onFailure().invoke(rollbackError -> {
                        log.warn(ROLLBACK_FAILED_MESSAGE, "create", "deleted booking items");
                        log.error("Rollback create items Error: {}", rollbackError.getMessage(), rollbackError);
                    }
                )
            ).replaceWithVoid();
    }

    public void updateAuditing(Booking booking) {
        booking.setUpdatedAt(Instant.now());
        booking.setUpdatedBy(identityContext.getPrincipleName());
    }

    /* --------- Private methods for [DELETE] --------- */
    private Uni<Void> sendBookingDeletedEvent(String concertId, List<String> items) {
        BookingDeleted event = BookingDeleted.newBuilder()
            .setConcertId(concertId)
            .setItems(items.stream().map(seatId -> BookingItemChanged.newBuilder()
                .setSeatId(seatId)
                .build()).toList())
            .build();

        return Uni.createFrom().completionStage(() -> bookingDeletedEventBus.send(event))
            .invoke(() -> log.info("Booking deleted event sent successfully!"));
    }

    private Uni<String> deleteBookingAndItems(ObjectId bookingId, Booking booking) {
        return bookingRepository.deleteById(bookingId).flatMap(isDeleted -> {
            if (!isDeleted)
                throw new HttpException(AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED, null, "Delete", "booking");

            return bookingItemService.findAllByBookingId(bookingId.toHexString())
                .flatMap(items -> {
                    List<String> itemIds = items.stream().map(BookingItemResponse.Detail::getId).distinct().toList();
                    return bookingItemService.delete(itemIds)
                        .chain(() -> sendBookingDeletedEvent(
                            booking.getConcertId(),
                            items.stream().map(BookingItemResponse.Detail::getSeatId).toList())
                        ).replaceWith(bookingId.toHexString())
                        .onFailure().call(error -> {
                            log.error("Error: {}", error.getMessage(), error);
                            return rollbackDeleteBooking(booking);
                        });
                });
        }).onFailure().transform(error ->
            new HttpException(AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED, error, "Delete", "booking")
        );
    }

    private Uni<Void> rollbackDeleteBooking(Booking booking) {
        return bookingRepository.persist(booking).onFailure().invoke(error -> {
                log.warn(ROLLBACK_FAILED_MESSAGE, "create", "deleted booking");
                log.error("Error: {}", error.getMessage(), error);
            }
        ).replaceWithVoid();
    }

    /* --------- Private methods for [FIND] --------- */
    private Uni<List<BookingResponse.Details>> toDetailResponses(List<Booking> bookings) {
        List<Uni<BookingResponse.Details>> detailUnis = bookings.stream().map(this::toDetailResponse).toList();
        return Uni.combine().all().unis(detailUnis).with(list ->
            list.stream().map(BookingResponse.Details.class::cast).toList()
        );
    }

    private Uni<BookingResponse.Details> toDetailResponse(Booking booking) {
        return bookingItemService.findAllByBookingId(booking.getId().toHexString()).map(items -> {
            BookingResponse.Details dto = bookingMapper.toDetailsDto(booking);
            dto.setItems(items);
            return dto;
        });
    }

}
